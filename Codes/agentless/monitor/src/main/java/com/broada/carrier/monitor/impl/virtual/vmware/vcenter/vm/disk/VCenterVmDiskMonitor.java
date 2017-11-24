package com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.disk;

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
 * VCenter VM Disk监测器，用于取代ESX VM Disk监测器
 */
public class VCenterVmDiskMonitor extends VSphereMonitor {
	private static final Log logger = LogFactory.getLog(VCenterVmDiskMonitor.class);

	public static final String ITEM_VM_OS = "esx-vm-guest-full-name";
	public static final String ITEM_VM_NET_IP_ADDRESS = "esx-vm-net-ip-address";

	private static final String PROPERTY_VM_UUID = "summary.config.uuid";
	private static final String PROPERTY_VM_NAME = "summary.config.name";

	private static final String PROPERTY_DISK_COMMITTED = "summary.storage.committed";
	private static final String PROPERTY_DISK_UNCOMMITTED = "summary.storage.uncommitted";
	private static final String PROPERTY_DISK_UNSHARED = "summary.storage.unshared";

	public static final int ITEM_NUM = 5;// "name", "uncommitAndCommited","commited", "unshare", "utilized"

	// 虚拟机的ip和操作系统
	private static final String PROPERTY_VM_OS = "summary.config.guestFullName";
	private static final String PROPERTY_VM_IP_ADDRESS = "summary.guest.ipAddress";

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult monitorResult = new MonitorResult();
		VSphereMonitorMethodOption option = getOption(context);

		try {
			return collect(context.getNode().getIp(), option.getUsername(), option.getPassword());
		} catch (CollectException err) {
			logger.warn(String.format("获取虚拟机磁盘数据失败。错误：%s", err));
			logger.debug("堆栈：", err);
			monitorResult.setResultDesc("获取虚拟机磁盘性能数据失败");
			monitorResult.setState(MonitorConstant.MONITORSTATE_FAILING);
			return monitorResult;
		}
	}

	private MonitorResult collect(String ipAddress, String userName, String password) throws CollectException {
		String[][] infoType = new String[][] { new String[] { "VirtualMachine", PROPERTY_VM_UUID, PROPERTY_VM_NAME,
				PROPERTY_DISK_COMMITTED, PROPERTY_DISK_UNCOMMITTED, PROPERTY_DISK_UNSHARED, PROPERTY_VM_OS, PROPERTY_VM_IP_ADDRESS } };

		// 获取虚拟机信息
		Map<ManagedObjectReference, Map<String, Object>> vmInfoMap = null;

		VSphereConnection connection = null;
		try {
			connection = connectVSphereSDK(ipAddress, userName, password);
			vmInfoMap = VSphereUtil.retrieveProperties(connection, infoType);
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
		for (ManagedObjectReference vmMor : vmInfoMap.keySet()) {
			Map<String, Object> vmInfo = vmInfoMap.get(vmMor);
			// 为了兼容前一个版本，以vmName作为instKey

			// 需要考虑无法获取数据的情况
			if (vmInfo.get(PROPERTY_VM_NAME) == null) {
				continue;
			}

			Double diskCommitted = MonitorUtil.getAvailableDoubleValue(vmInfo.get(PROPERTY_DISK_COMMITTED));
			diskCommitted = !MonitorUtil.isUnknownDoubleValue(diskCommitted) ? diskCommitted / 1024 / 1024 : diskCommitted;
			Double diskUncommitted = MonitorUtil.getAvailableDoubleValue(vmInfo.get(PROPERTY_DISK_UNCOMMITTED));
			diskUncommitted = !MonitorUtil.isUnknownDoubleValue(diskUncommitted) ? diskUncommitted / 1024 / 1024
					: diskUncommitted;
			Double diskUnshared = MonitorUtil.getAvailableDoubleValue(vmInfo.get(PROPERTY_DISK_UNSHARED));
			diskUnshared = !MonitorUtil.isUnknownDoubleValue(diskUnshared) ? diskUnshared / 1024 / 1024 : diskUnshared;
			Double diskUtilized = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
			Double diskSize = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
			if (!MonitorUtil.isUnknownDoubleValue(diskCommitted) && !MonitorUtil.isUnknownDoubleValue(diskUncommitted)) {
				diskSize = diskCommitted + diskUncommitted;
				if (diskSize == 0) {
					diskUtilized = 0.0;
				} else {
					diskUtilized = 100 * diskCommitted / diskSize;
				}
			}

			if (vmInfo.get(PROPERTY_VM_UUID) == null)
				continue;
			MonitorResultRow row = new MonitorResultRow(vmInfo.get(PROPERTY_VM_UUID).toString() + vmInfo.get(PROPERTY_VM_NAME).toString(), vmInfo.get(PROPERTY_VM_NAME).toString());
			row.setIndicator(ITEM_VM_NET_IP_ADDRESS, MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_IP_ADDRESS)));
			row.setIndicator(ITEM_VM_OS, MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_OS)));
			row.setIndicator("ESX-VM-DISK-2", diskSize);
			row.setIndicator("ESX-VM-DISK-3", diskCommitted);
			row.setIndicator("ESX-VM-DISK-4", diskUnshared);
			row.setIndicator("ESX-VM-DISK-5", diskUtilized);
			result.addRow(row);
		}
		return result;
	}
}
