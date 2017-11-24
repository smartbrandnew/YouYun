package com.broada.carrier.monitor.impl.virtual.vmware.vcenter.hypervisor.cpu;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.MonitorUtil;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.virtual.vmware.VSphereMonitor;
import com.broada.carrier.monitor.method.vmware.VSphereConnection;
import com.broada.carrier.monitor.method.vmware.VSphereException;
import com.broada.carrier.monitor.method.vmware.VSphereMonitorMethodOption;
import com.broada.carrier.monitor.method.vmware.VSphereUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.vmware.vim25.ManagedObjectReference;

/**
 * 监测vcenter中配置的各个hypervisor的cpu情况
 * @author Panhk
 * @version 1.0
 * @created 28-九月-2012 16:13:51
 */
public class VCenterHypervisorCPUMonitor extends VSphereMonitor {

	private static final Log logger = LogFactory.getLog(VCenterHypervisorCPUMonitor.class);

	private static final String PROPERTY_HOST_UUID = "summary.hardware.uuid";
	private static final String PROPERTY_HOST_NAME = "summary.config.name";
	private static final String PROPERTY_HOST_VNIC = "config.network.vnic";
	private static final String PROPERTY_HOST_CONSOLE_VNIC = "config.network.consoleVnic";
	private static final String PROPERTY_CPU_CORE_HZ = "summary.hardware.cpuMhz";
	private static final String PROPERTY_CPU_CORE_NUM = "summary.hardware.numCpuCores";
	private static final String PROPERTY_CPU_USED = "summary.quickStats.overallCpuUsage";

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult monitorResult = new MonitorResult();
		VSphereMonitorMethodOption option = getOption(context);
		try {
			return collect(context.getNode().getIp(), option.getUsername(), option.getPassword());
		} catch (CollectException err) {
			logger.warn(String.format("获取Hypervisor CPU性能数据失败。错误：%s", err));
			logger.debug("堆栈：", err);
			monitorResult.setResultDesc("获取Hypervisor CPU性能数据失败");
			monitorResult.setState(MonitorConstant.MONITORSTATE_FAILING);
			return monitorResult;
		}
	}

	private MonitorResult collect(String ipAddress, String userName, String password) throws CollectException {
		// 获取uuid
		String[][] infoType = new String[][] { new String[] { "HostSystem", PROPERTY_HOST_UUID, PROPERTY_HOST_NAME,
				PROPERTY_HOST_VNIC, PROPERTY_HOST_CONSOLE_VNIC, PROPERTY_CPU_USED, PROPERTY_CPU_CORE_HZ, PROPERTY_CPU_CORE_NUM } };
		Map<ManagedObjectReference, Map<String, Object>> hostInfoMap = null;

		VSphereConnection connection = null;
		try {
			connection = connectVSphereSDK(ipAddress, userName, password);
			hostInfoMap = VSphereUtil.retrieveProperties(connection, infoType);
		} catch (VSphereException e) {
			throw new CollectException("获取Hypervisor基本信息[uuid]失败", e);
		} finally {
			try {
				if (connection != null) {
					connection.disconnect();
				}
			} catch (VSphereException e) {
				throw new CollectException("断开vSphere连接失败", e);
			}
		}

		// 整理最终结果
		// 整理监测实例和性能数据
		// 为了应对无法获取实例的情况，所以设置为List
		MonitorResult result = new MonitorResult();
		for (ManagedObjectReference hostMor : hostInfoMap.keySet()) {
			Map<String, Object> hostInfo = hostInfoMap.get(hostMor);

			// 需要考虑无法获取数据的情况
			if (hostInfo.get(PROPERTY_HOST_UUID) == null) {
				continue;
			}

			String name = hostInfo.get(PROPERTY_HOST_NAME) == null ? "未知" : hostInfo.get(PROPERTY_HOST_NAME).toString();
			Double used = MonitorUtil.getAvailableDoubleValue(hostInfo.get(PROPERTY_CPU_USED));
			Double cpuCoreHz = MonitorUtil.getAvailableDoubleValue(hostInfo.get(PROPERTY_CPU_CORE_HZ));
			Double cpuCoreNum = MonitorUtil.getAvailableDoubleValue(hostInfo.get(PROPERTY_CPU_CORE_NUM));
			Double usage = new Double(0.0d);
			if (used != null && cpuCoreHz != null && cpuCoreNum != null) {
				if (cpuCoreHz * cpuCoreNum == 0) {
					usage = 0.0;
				} else {
					usage = 100.0 * used / (cpuCoreHz * cpuCoreNum);
				}
			}
			String ipAddr = getIpAdress(hostInfo, PROPERTY_HOST_VNIC, PROPERTY_HOST_CONSOLE_VNIC);
			//PROD-21409  修复VCenterHypervisor客户端展现监测数据时，获取设备主机主板uuid可能存在相同的设备，造成有些设备能监测到，但是展现监测数据时，会有缺少，改成设备ip
			MonitorResultRow row = new MonitorResultRow(ipAddr, name);
			row.setIndicator("HYPERVISOR-VCENTER-CPU-1", ipAddr);
			if(usage > 100)
				logger.error("ESX主机名称为:" + name + "的cpu使用率为:" + usage);
			row.setIndicator("HYPERVISOR-VCENTER-CPU-2", usage > 100 ? Double.valueOf(100.00d):usage);
			row.setIndicator("HYPERVISOR-VCENTER-CPU-3", used);
			if(usage > 100)
				logger.error("ESX主机名称为:" + name + "的cpu使用率为:" + usage);
			row.setIndicator("VCENTER-HYPERVISOR-CPU-2", usage > 100 ? Double.valueOf(100d):usage);
			
			
			result.addRow(row);
		}
		return result;
	}
}