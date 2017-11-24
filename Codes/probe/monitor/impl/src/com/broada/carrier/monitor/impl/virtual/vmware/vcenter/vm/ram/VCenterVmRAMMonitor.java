package com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.ram;

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
 * VCenter VM 内存监测器，用于取代ESX VM 内存监测器 
 */
public class VCenterVmRAMMonitor extends VSphereMonitor {
	private static final Log logger = LogFactory.getLog(VCenterVmRAMMonitor.class);

	public static final String ITEM_VM_OS = "esx-vm-guest-full-name";
	public static final String ITEM_VM_NET_IP_ADDRESS = "esx-vm-net-ip-address";

	private static final String PROPERTY_VM_UUID = "summary.config.uuid";
	private static final String PROPERTY_VM_NAME = "summary.config.name";

	private static final String PROPERTY_RAM_SIZE = "summary.config.memorySizeMB";
	private static final String PROPERTY_RAM_USED = "summary.quickStats.guestMemoryUsage";

	public static int ITEM_NUM = 4;// "name", "num", "frequency", "utilized"

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
			logger.warn(String.format("获取虚拟机内存数据失败。错误：%s", err));
			logger.debug("堆栈：", err);
			monitorResult.setResultDesc("获取虚拟机内存性能数据失败");
			monitorResult.setState(MonitorConstant.MONITORSTATE_FAILING);
			return monitorResult;
		}
	}

	private MonitorResult collect(String ipAddress, String userName, String password) throws CollectException {
		String[][] infoType = new String[][] { new String[] { "VirtualMachine", PROPERTY_VM_UUID, PROPERTY_VM_NAME,
				PROPERTY_RAM_USED, PROPERTY_RAM_SIZE, PROPERTY_VM_OS, PROPERTY_VM_IP_ADDRESS } };

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

			Double ramUsed = MonitorUtil.getAvailableDoubleValue(vmInfo.get(PROPERTY_RAM_USED));
			Double ramSize = MonitorUtil.getAvailableDoubleValue(vmInfo.get(PROPERTY_RAM_SIZE));

			Double ramUtilized = MonitorConstant.UNKNOWN_DOUBLE_VALUE;
			if (!MonitorUtil.isUnknownDoubleValue(ramUsed) && !MonitorUtil.isUnknownDoubleValue(ramSize)) {
				if (ramSize == 0) {
					ramUtilized = 0.0;
				} else {
					ramUtilized = 100 * ramUsed / ramSize;
				}
			}

			if (vmInfo.get(PROPERTY_VM_UUID) == null)
				continue;
			MonitorResultRow row = new MonitorResultRow(vmInfo.get(PROPERTY_VM_UUID).toString() + vmInfo.get(PROPERTY_VM_NAME).toString(), vmInfo.get(PROPERTY_VM_NAME).toString());
			row.setIndicator(ITEM_VM_NET_IP_ADDRESS, MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_IP_ADDRESS)));
			row.setIndicator(ITEM_VM_OS, MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_OS)));
			row.setIndicator("ESX-VM-RAM-2", ramUsed);
			row.setIndicator("ESX-VM-RAM-3", ramSize);
			row.setIndicator("ESX-VM-RAM-4", ramUtilized);
			result.addRow(row);
		}
		return result;
	}
}