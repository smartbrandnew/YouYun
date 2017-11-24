package com.broada.carrier.monitor.impl.virtual.vmware.vcenter.hypervisor.ram;
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
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfSummaryType;

/**
 * 监测vcenter中配置的各个hypervisor的ram情况
 * @author Panhk
 * @version 1.0
 * @created 28-九月-2012 16:13:51
 */
public class VCenterHypervisorRAMMonitor extends VSphereMonitor {
	
	private static final Log logger = LogFactory.getLog(VCenterHypervisorRAMMonitor.class);

	private static final String PROPERTY_HOST_UUID = "summary.hardware.uuid";
	private static final String PROPERTY_HOST_NAME = "summary.config.name";
	private static final String PROPERTY_HOST_VNIC = "config.network.vnic";
	private static final String PROPERTY_HOST_CONSOLE_VNIC = "config.network.consoleVnic";
	private static final String PROPERTY_RAM_SIZE = "summary.hardware.memorySize";
	private static final String PROPERTY_RAM_USED = "summary.quickStats.overallMemoryUsage";
	
	private static final String PERF_GOURP_MEM = "mem";
	private static final String PERF_NAME_SYS_USED = "sysUsage";
		
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult monitorResult = new MonitorResult();
		VSphereMonitorMethodOption option = getOption(context);
		
		try {
			return collect(context.getNode().getIp(), option.getUsername(), option.getPassword());
		} catch (CollectException err) {
			logger.warn(String.format("获取Hypervisor内存数据失败。错误：%s", err));
			logger.debug("堆栈：", err);
			monitorResult.setResultDesc("获取Hypervisor内存性能数据失败");
			monitorResult.setState(MonitorConstant.MONITORSTATE_FAILING);
			return monitorResult;
		}
	}
	

	private MonitorResult collect(String ipAddress, String userName, String password) throws CollectException {
		String[][] infoType = new String[][] { new String[] { "HostSystem", PROPERTY_HOST_UUID, PROPERTY_HOST_NAME,
				PROPERTY_HOST_VNIC, PROPERTY_HOST_CONSOLE_VNIC, PROPERTY_RAM_USED, PROPERTY_RAM_SIZE } };
		// 获取主机信息
		Map<ManagedObjectReference, Map<String, Object>> hostInfoMap = null;
		Map<ManagedObjectReference, Map<Integer, Long>> perfValues = null;
		int counterId = 0; // 获取级别为1（平均值）的性能数据：预留量

		VSphereConnection connection = null;
		try {
			connection = connectVSphereSDK(ipAddress, userName, password);
			hostInfoMap = VSphereUtil.retrieveProperties(connection, infoType);

			Map<Integer, PerfCounterInfo> infos = VSphereUtil.queryAllPerfCounterInfo(connection);
			for (PerfCounterInfo info : infos.values()) {
				// 只取平均值类型，为了提高代码的可读性，所以单独设置一个if语句
				if (!PerfSummaryType.average.getValue().equals(info.getRollupType().getValue())) {
					continue;
				}
				if (PERF_GOURP_MEM.equals(info.getGroupInfo().getKey())
						&& PERF_NAME_SYS_USED.equals(info.getNameInfo().getKey())) {
					counterId = info.getKey();
					perfValues = VSphereUtil.getIntPerfValues(connection,
							hostInfoMap.keySet().toArray(new ManagedObjectReference[0]), new int[] { counterId });
					break;
				}
			}
		} catch (VSphereException e) {
			throw new CollectException("获取Hypervisor基本信息失败", e);
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
			
			Double used = MonitorUtil.getAvailableDoubleValue(hostInfo.get(PROPERTY_RAM_USED));
			Double ramSize = MonitorUtil.getAvailableDoubleValue(hostInfo.get(PROPERTY_RAM_SIZE));
			Double usage = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
			if(!MonitorUtil.isUnknownDoubleValue(used) && !MonitorUtil.isUnknownDoubleValue(ramSize)) {
				if (ramSize == 0) {
					usage = 0.0;
				} else {
					usage = 100 * used / (ramSize / 1024 / 1024);
				}
			}
			Double vmUsed = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
			if (!MonitorUtil.isUnknownDoubleValue(used) && perfValues != null && perfValues.get(hostMor) != null) {
				Double sysUsed = perfValues.get(hostMor).get(counterId) / 1024.0;
				vmUsed = used - sysUsed;
			}
			// 获取ip地址
			String ipAddr = getIpAdress(hostInfo, PROPERTY_HOST_VNIC, PROPERTY_HOST_CONSOLE_VNIC);
			//PROD-21409  修复VCenterHypervisor客户端展现监测数据时，获取设备主机主板uuid可能存在相同的设备，造成有些设备能监测到，但是展现监测数据时，会有缺少，改成设备ip
			MonitorResultRow row = new MonitorResultRow(ipAddr, hostInfo.get(PROPERTY_HOST_NAME) == null ? "未知" : hostInfo.get(PROPERTY_HOST_NAME)
					.toString());
			row.setIndicator("HYPERVISOR-VCENTER-RAM-1", ipAddr);
			row.setIndicator("HYPERVISOR-VCENTER-RAM-2", usage);
			row.setIndicator("HYPERVISOR-VCENTER-RAM-3", used);
			row.setIndicator("HYPERVISOR-VCENTER-RAM-4", vmUsed);			
			result.addRow(row);
		}
	return result;
	}
}