package com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.cpu;

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
 * VCenter VM CPU监测器，用于取代ESX VM CPU监测器
 */
public class VCenterVmCPUMonitor extends VSphereMonitor {
	private static final Log logger = LogFactory.getLog(VCenterVmCPUMonitor.class);
	
	public static final String ITEM_VM_OS = "esx-vm-guest-full-name";
	public static final String ITEM_VM_NET_IP_ADDRESS = "esx-vm-net-ip-address";
	
	private static final String PROPERTY_VM_UUID = "summary.config.uuid";
	private static final String PROPERTY_VM_NAME = "summary.config.name";

	private static final String PROPERTY_CPU_NUM = "summary.config.numCpu";                 // 虚拟机cpu数量
	private static final String PROPERTY_CPU_FREQUENCY = "summary.runtime.maxCpuUsage";     // 虚拟机cpu上限Mhz
	private static final String PROPERTY_CPU_USED = "summary.quickStats.overallCpuUsage";   // 消耗的cpu使用
	
	// 虚拟机的ip和操作系统
	private static final String PROPERTY_VM_OS = "summary.config.guestFullName";
	private static final String PROPERTY_VM_IP_ADDRESS = "summary.guest.ipAddress";
	
	private static final String PROPERTY_VM_HYPER_MOR = "summary.runtime.host";
	
	private static final String PROPERTY_CPU_CORE_HZ = "summary.hardware.cpuMhz";    // cpu频率(多核的话是平均值)

	private static final String[][] INFO_TYPE = new String[][] {
			new String[] { "VirtualMachine", PROPERTY_VM_UUID, PROPERTY_VM_NAME, PROPERTY_CPU_NUM, PROPERTY_CPU_FREQUENCY,
					PROPERTY_CPU_USED, PROPERTY_VM_HYPER_MOR, PROPERTY_VM_OS, PROPERTY_VM_IP_ADDRESS}, new String[] { "HostSystem", PROPERTY_CPU_CORE_HZ } };

	public static int ITEM_NUM = 4;// "name", "num", "frequency", "utilized"

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult monitorResult = new MonitorResult();
		VSphereMonitorMethodOption option = getOption(context);

		try {
			return collect(context.getNode().getIp(), option.getUsername(), option.getPassword());
		} catch (CollectException err) {
			logger.warn(String.format("获取虚拟机CPU数据失败。错误：%s", err));
			logger.debug("堆栈：", err);
			monitorResult.setResultDesc("获取虚拟机CPU性能数据失败");
			monitorResult.setState(MonitorConstant.MONITORSTATE_FAILING);
			return monitorResult;
		}
	}

	private MonitorResult collect(String ipAddress, String userName, String password) throws CollectException {
		// 获取虚拟机信息
		Map<ManagedObjectReference, Map<String, Object>> infoMap = null;

		VSphereConnection connection = null;
		try {
			connection = connectVSphereSDK(ipAddress, userName, password);
			infoMap = VSphereUtil.retrieveProperties(connection, INFO_TYPE);
		} catch (VSphereException e) {
			throw new CollectException("获取虚拟机基本信息[uuid]失败", e);
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
		for (ManagedObjectReference mor : infoMap.keySet()) {
			if (!mor.getType().equals("VirtualMachine")) {
				continue;
			}
			
			Map<String, Object> vmInfo = infoMap.get(mor);
			
			// 为了兼容前一个版本，以vmName作为instKey
			
			// 需要考虑无法获取数据的情况
			if (vmInfo.get(PROPERTY_VM_NAME) == null) {
				continue;
			}
			Double cpuNum = MonitorUtil.getAvailableDoubleValue(vmInfo.get(PROPERTY_CPU_NUM));
			Double cpuFrequency = 0d;
			if (!MonitorUtil.isUnknownDoubleValue(cpuNum)) {
				Map<String, Object> hostInfo = infoMap.get(vmInfo.get(PROPERTY_VM_HYPER_MOR));
				if (hostInfo != null) {
					Double cpuCoreFrequency = MonitorUtil.getAvailableDoubleValue(hostInfo.get(PROPERTY_CPU_CORE_HZ));
					if (!MonitorUtil.isUnknownDoubleValue(cpuCoreFrequency)) {
						cpuFrequency = cpuNum * cpuCoreFrequency;
					}
				}
			}
			Double cpuUsed = MonitorUtil.getAvailableDoubleValue(vmInfo.get(PROPERTY_CPU_USED));
			Double cpuUtilized = new Double(0.0d);
			if (!MonitorUtil.isUnknownDoubleValue(cpuUsed) && !MonitorUtil.isUnknownDoubleValue(cpuFrequency)) {
				if (cpuFrequency == 0) {
					cpuUtilized = 0.0;
				} else {
					cpuUtilized = 100 * cpuUsed / cpuFrequency;
				}
			}

			if (vmInfo.get(PROPERTY_VM_UUID) == null)
				continue;
			MonitorResultRow row = new MonitorResultRow(vmInfo.get(PROPERTY_VM_UUID).toString() + vmInfo.get(PROPERTY_VM_NAME).toString(), vmInfo.get(PROPERTY_VM_NAME).toString());
			row.setIndicator(ITEM_VM_NET_IP_ADDRESS, MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_IP_ADDRESS)));
			row.setIndicator(ITEM_VM_OS, MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_OS)));
			row.setIndicator("ESX-VM-CPU-2", cpuNum);
			row.setIndicator("ESX-VM-CPU-3", cpuFrequency);
			if(cpuUtilized > 100)
				logger.error("虚拟机名称为:" + vmInfo.get(PROPERTY_VM_NAME).toString() + "的cpu使用率为:" + cpuUtilized);
			row.setIndicator("ESX-VM-CPU-4", cpuUtilized > 100 ? Double.valueOf(100d):cpuUtilized);
			row.setIndicator("ESX-VM-CPU-4", cpuUtilized > 100 ? Double.valueOf(100.00d):cpuUtilized);
			result.addRow(row);
		}
		return result;
	}
}