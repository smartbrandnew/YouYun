package com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.info;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.MonitorUtil;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.common.entity.RunState;
import com.broada.carrier.monitor.impl.virtual.vmware.VSphereMonitor;
import com.broada.carrier.monitor.method.vmware.VSphereConnection;
import com.broada.carrier.monitor.method.vmware.VSphereException;
import com.broada.carrier.monitor.method.vmware.VSphereMonitorMethodOption;
import com.broada.carrier.monitor.method.vmware.VSphereUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineConfigInfo;

/**
 * VCenter VM 基本信息监测器，用于取代ESX VM 基本信息监测器
 */
public class VCenterVmInfoMonitor extends VSphereMonitor {
	private static final Log logger = LogFactory.getLog(VCenterVmInfoMonitor.class);

	// itemCode兼容esx-vm监测器
	public static final String ITEM_VM_PATH = "esx-vm-path";
	public static final String ITEM_VM_OS = "esx-vm-guest-full-name";
	public static final String ITEM_VM_PATH_NAME = "esx-vm-path-name";
	public static final String ITEM_VM_POWER_STATE = "esx-vm-power-state";
	public static final String ITEM_VM_OVERALL_STATUS = "esx-vm-over-all-status";
	public static final String ITEM_VM_HYPER_UUID = "esx-vm-hyper-uuid";
	public static final String ITEM_VM_NET_IP_ADDRESS = "esx-vm-net-ip-address";
	public static final String ITEM_COMPUTER_RESOURCE_PATH = "esx-vm-compute-resource-path";
	public static final String ITEM_COMPUTER_RESOURCE_NAME = "esx-vm-compute-resource-name";
	public static final String ITEM_COMPUTER_RESOURCE_TYPE = "esx-vm-compute-resource-type";
	public static final String ITEM_COMPUTER_IS_TEMPLATE = "esx-vm-is-template";
	public static final String ITEM_VM_CPU_NUM = "esx-vm-cpu-num";

	public static final int ITEM_NUM = 9;

	// VirtualMachine属性
	private static final String PROPERTY_VM_CPU_NUM = "summary.config.numCpu";
	private static final String PROPERTY_VM_UUID = "summary.config.uuid";
	private static final String PROPERTY_VM_NAME = "summary.config.name";
	private static final String PROPERTY_VM_OS = "summary.config.guestFullName";
	private static final String PROPERTY_VM_PATH_NAME = "summary.config.vmPathName";
	private static final String PROPERTY_VM_POWER_STATE = "summary.runtime.powerState";
	private static final String PROPERTY_VM_OVERALL_STATUS = "summary.overallStatus";
	private static final String PROPERTY_VM_HYPER_MOR = "summary.runtime.host";
	private static final String PROPERTY_VM_RESOURCE_POOL_MOR = "resourcePool";
	private static final String PROPERTY_VM_IP_ADDRESS = "summary.guest.ipAddress";
	private static final String PROPERTY_VM_CONFIGINFO = "config";

	private static final String PROPERTY_VM_MOR_NAME = "name";
	private static final String PROPERTY_VM_MOR_PARENT = "parent";

	// Datacenter属性
	private static final String PROPERTY_DATACENTER_MOR_NAME = "name";
	private static final String PROPERTY_DATACENTER_MOR_PARENT = "parent";

	// Folder属性
	private static final String PROPERTY_FOLDER_MOR_NAME = "name";
	private static final String PROPERTY_FOLDER_MOR_PARENT = "parent";

	// ClusterComputeResource资源属性
	private static final String PROPERTY_CLUSTER_COMPUTE_RESOURCE_MOR_NAME = "name";
	private static final String PROPERTY_CLUSTER_COMPUTE_RESOURCE_MOR_PARENT = "parent";

	// ComputeResource属性
	private static final String PROPERTY_COMPUTE_RESOURCE_MOR_NAME = "name";
	private static final String PROPERTY_COMPUTE_RESOURCE_MOR_PARENT = "parent";

	// ResourcePool属性
	private static final String PROPERTY_RESOURCE_POOL_MOR_NAME = "name";
	private static final String PROPERTY_RESOURCE_POOL_MOR_PARENT = "parent";
	private static final String PROPERTY_RESOURCE_POOL_OWNER_MOR = "owner";

	// HostSystem属性
	private static final String PROPERTY_HYPER_UUID = "summary.hardware.uuid";

	private static final String[][] INFO_TYPE = new String[][] {
			new String[] { "VirtualMachine", PROPERTY_VM_UUID, PROPERTY_VM_NAME, PROPERTY_VM_OS, PROPERTY_VM_PATH_NAME,
					PROPERTY_VM_POWER_STATE, PROPERTY_VM_OVERALL_STATUS, PROPERTY_VM_HYPER_MOR,
					PROPERTY_VM_RESOURCE_POOL_MOR, PROPERTY_VM_MOR_NAME, PROPERTY_VM_RESOURCE_POOL_MOR,
					PROPERTY_VM_MOR_PARENT, PROPERTY_VM_IP_ADDRESS, PROPERTY_VM_CONFIGINFO, PROPERTY_VM_CPU_NUM },
			new String[] { "HostSystem", PROPERTY_HYPER_UUID },
			new String[] { "Datacenter", PROPERTY_DATACENTER_MOR_NAME, PROPERTY_DATACENTER_MOR_PARENT },
			new String[] { "Folder", PROPERTY_FOLDER_MOR_NAME, PROPERTY_FOLDER_MOR_PARENT },
			new String[] { "ClusterComputeResource", PROPERTY_CLUSTER_COMPUTE_RESOURCE_MOR_NAME,
					PROPERTY_CLUSTER_COMPUTE_RESOURCE_MOR_PARENT },
			new String[] { "ComputeResource", PROPERTY_COMPUTE_RESOURCE_MOR_NAME, PROPERTY_COMPUTE_RESOURCE_MOR_PARENT },
			new String[] { "ResourcePool", PROPERTY_RESOURCE_POOL_MOR_NAME, PROPERTY_RESOURCE_POOL_MOR_PARENT,
					PROPERTY_RESOURCE_POOL_OWNER_MOR } };

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult monitorResult = new MonitorResult();
		VSphereMonitorMethodOption option = getOption(context);

		try {
			return collect(context.getNode().getIp(), option.getUsername(), option.getPassword());
		} catch (CollectException err) {
			logger.warn(String.format("获取虚拟机基本信息失败。错误：%s", err));
			logger.debug("堆栈：", err);
			monitorResult.setResultDesc("获取虚拟机基本信息失败");
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
			// 需要考虑无法获取数据的情况
			if (vmInfo.get(PROPERTY_VM_UUID) == null) {
				continue;
			}
			VirtualMachineConfigInfo configInfo = (VirtualMachineConfigInfo) vmInfo.get(PROPERTY_VM_CONFIGINFO);
			String isTemplate = configInfo.isTemplate() ? "是" : "否";
			String vmOs = MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_OS));
			String vmPathName = MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_PATH_NAME));
			String vmPowerState = MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_POWER_STATE));
			String vmOverallStatus = MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_OVERALL_STATUS));
			String hyperUUID = MonitorConstant.UNKNOWN_STRING_VALUE;
			if (vmInfo.get(PROPERTY_VM_HYPER_MOR) != null && infoMap.get(vmInfo.get(PROPERTY_VM_HYPER_MOR)) != null) {
				hyperUUID = MonitorUtil.getAvailableStringValue(infoMap.get(vmInfo.get(PROPERTY_VM_HYPER_MOR)).get(
						PROPERTY_HYPER_UUID));
			}

			String computeResourceName = MonitorConstant.UNKNOWN_STRING_VALUE;
			String computeResourceType = MonitorConstant.UNKNOWN_STRING_VALUE;
			String computeResourcePath = MonitorConstant.UNKNOWN_STRING_VALUE;

			// 获取计算资源名称、类型、路径
			Object resourcePoolMor = vmInfo.get(PROPERTY_VM_RESOURCE_POOL_MOR);
			if (resourcePoolMor != null) {
				Map<String, Object> resourcePoolInfoMap = infoMap.get(resourcePoolMor);
				if (resourcePoolInfoMap != null) {
					Object resourceOwnerMor = resourcePoolInfoMap.get(PROPERTY_RESOURCE_POOL_OWNER_MOR);
					if (resourceOwnerMor != null) {
						// 获取计算资源类型
						computeResourceType = VSphereUtil.getComputeResourceTypeDescr(MonitorUtil
								.getAvailableStringValue(((ManagedObjectReference) resourceOwnerMor).getType()));

						// 获取计算资源名称
						Map<String, Object> pathTreeMap = infoMap.get(resourceOwnerMor);
						if (pathTreeMap != null) {
							computeResourceName = MonitorUtil.getAvailableStringValue(pathTreeMap.get("name"));
						}

						// 获取计算资源路径
						// 目前由于路径非常简单，而且hypervisor数量少，所以直接循环查找，
						// 以后也可以建立缓存，即每个节点保存全路径名称以提高效率。
						StringBuilder tempPath = new StringBuilder();
						while (true) {
							if (pathTreeMap == null) {
								break;
							}
							tempPath.insert(0, pathTreeMap.get("name")).insert(0, "/");
							pathTreeMap = infoMap.get(pathTreeMap.get("parent"));
						}
						if (tempPath.length() > 0) {
							computeResourcePath = tempPath.substring(1);
						}
					}
				}
			}

			String vmIpAddress = MonitorConstant.UNKNOWN_STRING_VALUE;

			// 获取虚拟机所有网卡参数中的ip地址,需要虚拟机安装vmware tools
			Object netMor = vmInfo.get(PROPERTY_VM_IP_ADDRESS);
			if (netMor != null) {
				vmIpAddress = netMor.toString();
			}
			Double cpuNum = MonitorUtil.getAvailableDoubleValue(vmInfo.get(PROPERTY_VM_CPU_NUM));
			MonitorResultRow row = new MonitorResultRow(vmInfo.get(PROPERTY_VM_UUID).toString()
					+ vmInfo.get(PROPERTY_VM_NAME).toString(), vmInfo.get(PROPERTY_VM_NAME) == null ? "未知" : vmInfo
					.get(PROPERTY_VM_NAME).toString());
			row.setIndicator(ITEM_VM_PATH, vmPathName);
			row.setIndicator(ITEM_VM_OS, vmOs);
			row.setIndicator(ITEM_VM_PATH_NAME, vmPathName);
			row.setIndicator(ITEM_VM_POWER_STATE, getRunState(vmPowerState));
			row.setIndicator(ITEM_VM_OVERALL_STATUS, vmOverallStatus);
			row.setIndicator(ITEM_VM_HYPER_UUID, hyperUUID);
			row.setIndicator(ITEM_VM_NET_IP_ADDRESS, vmIpAddress);
			row.setIndicator(ITEM_COMPUTER_RESOURCE_NAME, computeResourceName);
			row.setIndicator(ITEM_COMPUTER_RESOURCE_TYPE, computeResourceType);
			row.setIndicator(ITEM_COMPUTER_RESOURCE_PATH, computeResourcePath);
			row.setIndicator(ITEM_COMPUTER_IS_TEMPLATE, isTemplate);
			row.setIndicator(ITEM_VM_CPU_NUM, cpuNum);
			result.addRow(row);
		}
		return result;
	}

	private RunState getRunState(String vmPowerState) {
		if ("poweredOff".equalsIgnoreCase(vmPowerState))
			return RunState.STOP;
		else
			return RunState.RUNNING;
	}
}