package com.broada.carrier.monitor.impl.virtual.vmware.vcenter.hypervisor.info;

import java.io.Serializable;
import java.util.HashMap;
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
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.ArrayOfScsiLun;
import com.vmware.vim25.HostScsiDisk;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfSummaryType;
import com.vmware.vim25.ScsiLun;

/**
 * 监测vcenter中配置的各个hypervisor的cpu情况
 * 
 * @author Panhk
 * @version 1.0
 * @created 28-九月-2012 16:13:51
 */
public class VCenterHypervisorInfoMonitor extends VSphereMonitor {

	private static final Log logger = LogFactory.getLog(VCenterHypervisorInfoMonitor.class);

	private static final String PROPERTY_HOST_UUID = "summary.hardware.uuid";
	private static final String PROPERTY_HOST_NAME = "summary.config.name";
	private static final String PROPERTY_VM_NAME = "summary.config.name";
	private static final String PROPERTY_HOST_VNIC = "config.network.vnic";
	private static final String PROPERTY_HOST_PRODUCT = "summary.config.product.fullName";
	private static final String PROPERTY_HOST_CPU_NUM = "summary.hardware.numCpuPkgs";
	private static final String PROPERTY_HOST_CPU_CORE_HZ = "summary.hardware.cpuMhz";
	private static final String PROPERTY_HOST_CPU_CORE_NUM = "summary.hardware.numCpuCores";
	private static final String PROPERTY_HOST_RAM_SIZE = "summary.hardware.memorySize";
	private static final String PROPERTY_HOST_NIC_NUM = "summary.hardware.numNics";
	private static final String PROPERTY_HOST_CONSOLE_VNIC = "config.network.consoleVnic";
	private static final String PROPERTY_HOST_STORAGE_SCSILUN = "config.storageDevice.scsiLun";
	private static final String PROPERTY_HOST_OVERALL_STATUS = "summary.overallStatus";
	private static final String PROPERTY_HOST_VM_MOR_ARRAY = "vm";
	private static final String PROPERTY_HOST_CLUSTER_NAME = "parent";

	// Datacenter属性
	private static final String PROPERTY_DATACENTER_MOR_NAME = "name";
	private static final String PROPERTY_DATACENTER_MOR_PARENT = "parent";

	private static final String PROPERTY_VM_POWER_STATE = "summary.runtime.powerState";
	private static final String PERF_GOURP_CPU = "cpu";
	private static final String PERF_CPU_NAME_RESERVED_CAPACITY = "reservedCapacity";
	private static final String PERF_GOURP_RAM = "mem";
	private static final String PERF_RAM_NAME_RESERVED_CAPACITY = "reservedCapacity";
	private static final String PERF_RAM_NAME_GRANTED = "granted";

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult monitorResult = new MonitorResult();
		VSphereMonitorMethodOption option = getOption(context);
		try {
			return collect(context.getNode().getIp(), option.getUsername(), option.getPassword());
		} catch (CollectException err) {
			logger.warn(String.format("获取Hypervisor基本信息失败。错误：%s", err));
			logger.debug("堆栈：", err);
			monitorResult.setResultDesc("获取Hypervisor基本信息失败");
			monitorResult.setState(MonitorConstant.MONITORSTATE_FAILING);
			return monitorResult;
		}
	}

	private MonitorResult collect(String ipAddress, String userName, String password) throws CollectException {
		String[][] infoType = new String[][] {
				new String[] { "HostSystem", PROPERTY_HOST_UUID, PROPERTY_HOST_NAME, PROPERTY_HOST_VNIC,
						PROPERTY_HOST_CONSOLE_VNIC, PROPERTY_HOST_PRODUCT, PROPERTY_HOST_CPU_CORE_HZ, PROPERTY_HOST_CPU_CORE_NUM,
						PROPERTY_HOST_CPU_NUM, PROPERTY_HOST_RAM_SIZE, PROPERTY_HOST_NIC_NUM, PROPERTY_HOST_STORAGE_SCSILUN,
						PROPERTY_HOST_OVERALL_STATUS, PROPERTY_HOST_VM_MOR_ARRAY, PROPERTY_HOST_CLUSTER_NAME },
					new String[] { "Datacenter", PROPERTY_DATACENTER_MOR_NAME, PROPERTY_DATACENTER_MOR_PARENT },
				new String[] { "VirtualMachine", PROPERTY_VM_POWER_STATE, PROPERTY_VM_NAME } };

		Map<ManagedObjectReference, Map<String, Object>> infoMap = null;
		Map<ManagedObjectReference, Map<Integer, Long>> perfValues = null;
		int[] counterIds = new int[3]; // 第一个元素是cpu.reservedCapacity，第二个元素是mem.reservedCapacity，第三个为mem.granted

		VSphereConnection connection = null;
		String clusterName = null;
		String dataCenterName = null;
		Map<String, String> clusters = new HashMap<String, String>();
		Map<String, String> dataCenter = new HashMap<String, String>();
		try {
			connection = connectVSphereSDK(ipAddress, userName, password);
			infoMap = VSphereUtil.retrieveProperties(connection, infoType);

			// 获取级别为1（平均值）的性能数据：预留量
			Map<Integer, PerfCounterInfo> infos = VSphereUtil.queryAllPerfCounterInfo(connection);
			// 计数，便于提前结束遍历，提高效率
			int cnt = 0;
			for (PerfCounterInfo info : infos.values()) {
				// 只取平均值类型
				if (!PerfSummaryType.average.getValue().equals(info.getRollupType().getValue())) {
					continue;
				}
				if (PERF_GOURP_CPU.equals(info.getGroupInfo().getKey())
						&& PERF_CPU_NAME_RESERVED_CAPACITY.equals(info.getNameInfo().getKey())) {
					counterIds[0] = info.getKey();
					cnt++;
				} else if (PERF_GOURP_RAM.equals(info.getGroupInfo().getKey())) {
					if (PERF_RAM_NAME_RESERVED_CAPACITY.equals(info.getNameInfo().getKey())) {
						counterIds[1] = info.getKey();
						cnt += 10;
					} else if (PERF_RAM_NAME_GRANTED.equals(info.getNameInfo().getKey())) {
						counterIds[2] = info.getKey();
						cnt += 100;
					}
				}
				if (cnt == 111) {
					break;
				}
			}
			perfValues = VSphereUtil.getIntPerfValues(connection, infoMap.keySet().toArray(new ManagedObjectReference[0]),
					counterIds);
			for (ManagedObjectReference mor : infoMap.keySet()) {
				if (!mor.getType().equals("HostSystem")) {
					continue;
				}
				Map<String, Object> hostInfo = infoMap.get(mor);

				// 需要考虑无法获取数据的情况
				if (hostInfo.get(PROPERTY_HOST_UUID) == null) {
					continue;
				}
				ManagedObjectReference parentPath = (ManagedObjectReference) hostInfo.get("parent");
				Map<String, Object> obj1 = VSphereUtil.retrieveProperties(connection, parentPath, new String[] { "name",
						"parent" });
				while (obj1 != null && !"数据中心".equals(obj1.get("name").toString())) {
					dataCenterName = obj1.get("name").toString();
					parentPath = (ManagedObjectReference) obj1.get("parent");
					obj1 = VSphereUtil.retrieveProperties(connection, parentPath, new String[] { "name", "parent" });
				}
				dataCenter.put(mor.get_value(), dataCenterName);

				ManagedObjectReference parentId = (ManagedObjectReference) hostInfo.get("parent");
				if ("ClusterComputeResource".equalsIgnoreCase(parentId.getType().trim())) {
					Map<String, Object> obj = VSphereUtil.retrieveProperties(connection, parentId, new String[] { "name",
							"parent" });
					clusterName = obj.get("name").toString();
					clusters.put(mor.get_value(), clusterName);

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
		for (ManagedObjectReference mor : infoMap.keySet()) {
			if (!mor.getType().equals("HostSystem")) {
				continue;
			}
			Map<String, Object> hostInfo = infoMap.get(mor);

			// 需要考虑无法获取数据的情况
			if (hostInfo.get(PROPERTY_HOST_UUID) == null) {
				continue;
			}
			if (clusters.size() > 0)
				clusterName = clusters.get(mor.get_value());
			if (dataCenter.size() > 0)
				dataCenterName = dataCenter.get(mor.get_value());
			String product = hostInfo.get(PROPERTY_HOST_PRODUCT).toString();
			// 获取ip地址
			String ipAddr = getIpAdress(hostInfo, PROPERTY_HOST_VNIC, PROPERTY_HOST_CONSOLE_VNIC);
			Double cpuNum = MonitorUtil.getAvailableDoubleValue(hostInfo.get(PROPERTY_HOST_CPU_NUM));
			Double cpuCoreHz = MonitorUtil.getAvailableDoubleValue(hostInfo.get(PROPERTY_HOST_CPU_CORE_HZ));
			Double cpuCoreNum = MonitorUtil.getAvailableDoubleValue(hostInfo.get(PROPERTY_HOST_CPU_CORE_NUM));
			Double cpuFrequency = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
			if (!MonitorUtil.isUnknownDoubleValue(cpuCoreHz) && !MonitorUtil.isUnknownDoubleValue(cpuCoreNum)) {
				cpuFrequency = cpuCoreHz * cpuCoreNum;
			}
			Double cpuReservedCapacity = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
			Double ramReservedCapacity = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
			Double ramGranted = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
			if (perfValues != null && perfValues.get(mor) != null) {
				cpuReservedCapacity = MonitorUtil.getAvailableDoubleValue(perfValues.get(mor).get(
						Integer.valueOf(counterIds[0])));
				ramReservedCapacity = MonitorUtil.getAvailableDoubleValue(perfValues.get(mor).get(
						Integer.valueOf(counterIds[1])));
				ramGranted = MonitorUtil.getAvailableDoubleValue(perfValues.get(mor).get(Integer.valueOf(counterIds[2])));
				if (!MonitorUtil.isUnknownDoubleValue(ramGranted)) {
					ramGranted = ramGranted / 1024;
				}
			}
			Double ramCapacity = MonitorUtil.getAvailableDoubleValue(hostInfo.get(PROPERTY_HOST_RAM_SIZE));
			if (!MonitorUtil.isUnknownDoubleValue(ramCapacity)) {
				ramCapacity = ramCapacity / 1024 / 1024;
			}
			Double nicNum = MonitorUtil.getAvailableDoubleValue(hostInfo.get(PROPERTY_HOST_NIC_NUM));
			Double storageCapacity = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
			if (hostInfo.get(PROPERTY_HOST_STORAGE_SCSILUN) != null) {
				ArrayOfScsiLun luns = (ArrayOfScsiLun) hostInfo.get(PROPERTY_HOST_STORAGE_SCSILUN);
				storageCapacity = 0.0;
				if (luns.getScsiLun() != null) {
					for (ScsiLun lun : luns.getScsiLun()) {
						if (lun instanceof HostScsiDisk) {
							storageCapacity += ((HostScsiDisk) lun).getCapacity().getBlock()
									* ((HostScsiDisk) lun).getCapacity().getBlockSize() / 1024 / 1024;
						}
					}
				}
			}
			String runState = MonitorUtil.getAvailableStringValue(hostInfo.get(PROPERTY_HOST_OVERALL_STATUS));
			StringBuffer name = new StringBuffer();
			/** 计算运行(powerOn)的虚拟机数目 */
			Double runVmNum = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
			if (hostInfo.get(PROPERTY_HOST_VM_MOR_ARRAY) != null) {
				Double num = 0.0;
				ArrayOfManagedObjectReference vmMors = (ArrayOfManagedObjectReference) hostInfo.get(PROPERTY_HOST_VM_MOR_ARRAY);
				if (vmMors.getManagedObjectReference() != null) {
					for (ManagedObjectReference vmMor : vmMors.getManagedObjectReference()) {
						if (infoMap.get(vmMor) != null && infoMap.get(vmMor).get(PROPERTY_VM_POWER_STATE) != null) {
							Map<String, Object> vmInfo = infoMap.get(vmMor);
							String name1 = vmInfo.get(PROPERTY_VM_NAME).toString();
							if (name1 == null || "".equals(name1))
								name1 = "未知";
							name = name.append(name1 + ",");
							String state = infoMap.get(vmMor).get(PROPERTY_VM_POWER_STATE).toString();
							if (state.equals("poweredOn")) {
								num += 1;
							}

						} else {
							num = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
							break;
						}
					}
				}
				runVmNum = num;
			}
			//PROD-21409  修复VCenterHypervisor客户端展现监测数据时，获取设备主机主板uuid可能存在相同的设备，造成有些设备能监测到，但是展现监测数据时，会有缺少，改成设备ip
			MonitorResultRow row = new MonitorResultRow(ipAddr,
					hostInfo.get(PROPERTY_HOST_NAME) == null ? "未知" : hostInfo.get(PROPERTY_HOST_NAME)
							.toString());
			row.setIndicator("HYPERVISOR-VCENTER-INFO-1", ipAddr);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-2", product);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-3", cpuNum);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-4", cpuFrequency);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-5", cpuReservedCapacity);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-6", ramCapacity);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-7", ramGranted);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-8", ramReservedCapacity);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-9", nicNum);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-10", storageCapacity);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-11", runState);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-12", runVmNum);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-13", clusterName);
			row.setIndicator("HYPERVISOR-VCENTER-INFO-14", dataCenterName);
			result.addRow(row);
		}
		return result;
	}
}