package com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.network;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.MonitorUtil;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.vmware.VSphereConnection;
import com.broada.carrier.monitor.method.vmware.VSphereException;
import com.broada.carrier.monitor.method.vmware.VSphereMonitorMethodOption;
import com.broada.carrier.monitor.method.vmware.VSphereUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.PerfCounterInfo;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 * 	vSphere网络流量监测器
 * 	监测指标:
 * 		bcc监测间隔时间内平均上下行速率bytesRx/bytesTx
 * 		bcc监测间隔时间内平均上下行端口转发率packetsRx/packetsTx
 * 		bcc监测间隔时间内平均上下行丢包率droppedRx/droppedTx
 * 		bcc监测间隔时间内平均上下行误包率errorsRx/errorsTx
 * 	如果指标存在但是获取不到数据,则按0处理
 * 	其中平均上下行速率因为vSphere SDK在流量小于1K bytes/s的情况下返回0,故
 * 	计算出来的数据并不是非常精确
 * </pre>
 * Created by hu on 2015/5/28.
 */
public class VCenterVmNetworkMonitor extends BaseMonitor {

	public static final String ITEM_VM_OS = "esx-vm-guest-full-name";
	public static final String ITEM_VM_NET_IP_ADDRESS = "esx-vm-net-ip-address";

	private static final String PROPERTY_VM_UUID = "summary.config.uuid";
	private static final String PROPERTY_VM_NAME = "summary.config.name";

	private static final String GROUP_NETWORK = "net";
	private static final String PROPERTY_NETWORK_BYTES_RX = "bytesRx";
	private static final String PROPERTY_NETWORK_BYTES_TX = "bytesTx";
	private static final String PROPERTY_NETWORK_PACKETS_RX = "packetsRx";
	private static final String PROPERTY_NETWORK_PACKETS_TX = "packetsTx";
	private static final String PROPERTY_NETWORK_DROPPED_RX = "droppedRx";
	private static final String PROPERTY_NETWORK_DROPPED_TX = "droppedRx";
	private static final String PROPERTY_NETWORK_ERRORS_RX = "errorsRx";
	private static final String PROPERTY_NETWORK_ERROES_TX = "errorsRx";

	private static final String ITEM_BYTES_RX = "ESX-VM-NET-1";
	private static final String ITEM_BYTES_TX = "ESX-VM-NET-2";
	private static final String ITEM_PACKETS_RX = "ESX-VM-NET-3";
	private static final String ITEM_PACKETS_TX = "ESX-VM-NET-4";
	private static final String ITEM_DROPPED_PACKETS_TX = "ESX-VM-NET-5";
	private static final String ITEM_DROPPED_PACKETS_RX = "ESX-VM-NET-6";
	private static final String ITEM_ERROR_PACKETS_TX = "ESX-VM-NET-7";
	private static final String ITEM_ERROR_PACKETS_RX = "ESX-VM-NET-8";

	// 虚拟机的ip和操作系统
	private static final String PROPERTY_VM_OS = "summary.config.guestFullName";
	private static final String PROPERTY_VM_IP_ADDRESS = "summary.guest.ipAddress";

	private static final String[][] INFO_TYPE = new String[][] {
		new String[] { "VirtualMachine", PROPERTY_VM_UUID, PROPERTY_VM_NAME, PROPERTY_VM_OS, PROPERTY_VM_IP_ADDRESS } };

	public MonitorResult collect(MonitorResult result, String ipAddress, String userName, String password, int interval)
	throws CollectException {
		// 获取虚拟机信息
		Map<ManagedObjectReference, Map<Integer, long[]>> perfValues;
		VSphereConnection connection;
		connection = connectVSphereSDK(ipAddress, userName, password);
		try {
			Map<ManagedObjectReference, Map<String, Object>> infoMap = VSphereUtil.retrieveProperties(connection, INFO_TYPE);
			Map<Integer, PerfCounterInfo> map = VSphereUtil.queryPerfCounterInfosByLevel(connection, 3);
			int[] counterIds = new int[8];
			for (PerfCounterInfo info : map.values()) {
				if (info.getGroupInfo().getKey().equals(GROUP_NETWORK)) {
					if (info.getNameInfo().getKey().equals(PROPERTY_NETWORK_BYTES_RX)) {
						counterIds[0] = info.getKey();
					}
					if (info.getNameInfo().getKey().equals(PROPERTY_NETWORK_BYTES_TX)) {
						counterIds[1] = info.getKey();
					}
					if (info.getNameInfo().getKey().equals(PROPERTY_NETWORK_PACKETS_RX)) {
						counterIds[2] = info.getKey();
					}
					if (info.getNameInfo().getKey().equals(PROPERTY_NETWORK_PACKETS_TX)) {
						counterIds[3] = info.getKey();
					}
					if (info.getNameInfo().getKey().equals(PROPERTY_NETWORK_DROPPED_RX)) {
						counterIds[4] = info.getKey();
					}
					if (info.getNameInfo().getKey().equals(PROPERTY_NETWORK_DROPPED_TX)) {
						counterIds[5] = info.getKey();
					}
					if (info.getNameInfo().getKey().equals(PROPERTY_NETWORK_ERROES_TX)) {
						counterIds[6] = info.getKey();
					}
					if (info.getNameInfo().getKey().equals(PROPERTY_NETWORK_ERRORS_RX)) {
						counterIds[7] = info.getKey();
					}
				}
			}
			Calendar endTime = Calendar.getInstance();
			Calendar startTime = (Calendar) endTime.clone();
			startTime.add(Calendar.SECOND, -interval);
			Set<ManagedObjectReference> managedObjectReferences = infoMap.keySet();
			perfValues = VSphereUtil.getIntPerfValues(connection,
					managedObjectReferences.toArray(new ManagedObjectReference[managedObjectReferences.size()]), counterIds,
					startTime, endTime);
			for (ManagedObjectReference mor : infoMap.keySet()) {
				if (perfValues != null && perfValues.get(mor) != null) {
					long receiveVal = 0l;
					long transmitVal = 0l;
					long receivePacketsVal = 0l;
					long transmitPacketsVal = 0l;
					long receiveDroppedPacketsVal = 0l;
					long transmitDroppedPacketsVal = 0l;
					long receiveErrorPacketsVal = 0l;
					long transmitErrorPacketsVal = 0l;

					if (perfValues.get(mor).get(counterIds[0]) != null) {
						for (long i : perfValues.get(mor).get(counterIds[0])) {
							if (i < 0)
								i = 0l;
							receiveVal += (i * 20);
						}
					}
					if (perfValues.get(mor).get(counterIds[1]) != null) {
						for (long i : perfValues.get(mor).get(counterIds[1])) {
							if (i < 0)
								i = 0l;
							transmitVal += (i * 20);
						}
					}
					if (perfValues.get(mor).get(counterIds[2]) != null) {
						for (long i : perfValues.get(mor).get(counterIds[2])) {
							if (i < 0)
								i = 0l;
							receivePacketsVal += i;
						}
					}
					if (perfValues.get(mor).get(counterIds[3]) != null) {
						for (long i : perfValues.get(mor).get(counterIds[3])) {
							if (i < 0)
								i = 0l;
							transmitPacketsVal += i;
						}
					}
					if (perfValues.get(mor).get(counterIds[4]) != null) {
						for (long i : perfValues.get(mor).get(counterIds[4])) {
							if (i < 0)
								i = 0l;
							receiveDroppedPacketsVal += i;
						}
					}
					if (perfValues.get(mor).get(counterIds[5]) != null) {
						for (long i : perfValues.get(mor).get(counterIds[5])) {
							if (i < 0)
								i = 0l;
							transmitDroppedPacketsVal += i;
						}
					}

					if (perfValues.get(mor).get(counterIds[6]) != null) {
						for (long i : perfValues.get(mor).get(counterIds[6])) {
							if (i < 0)
								i = 0l;
							receiveErrorPacketsVal += i;
						}
					}

					if (perfValues.get(mor).get(counterIds[7]) != null) {
						for (long i : perfValues.get(mor).get(counterIds[7])) {
							if (i < 0)
								i = 0l;
							transmitErrorPacketsVal += i;
						}
					}

					DecimalFormat format = new DecimalFormat("0.00");
					MonitorResultRow row = new MonitorResultRow(infoMap.get(mor).get(PROPERTY_VM_UUID).toString() + infoMap.get(mor).get(PROPERTY_VM_NAME).toString(),
							infoMap.get(mor).get(PROPERTY_VM_NAME).toString());
					double byteRx = receiveVal / (double) interval * 8.0;
					double byteTx = (transmitVal / (double) interval) * 8.0;
					double packetsRx = receivePacketsVal / (double) interval;
					double packetsTx = transmitPacketsVal / (double) interval;
					double droppedPacketsRx = receiveDroppedPacketsVal / (double) interval;
					double droppedPacketsTx = transmitDroppedPacketsVal / (double) interval;
					double errorPacketsRx = receiveErrorPacketsVal / (double) interval;
					double errorPacketsTx = transmitErrorPacketsVal / (double) interval;

					row.setIndicator(ITEM_BYTES_RX, format.format(byteRx));
					row.setIndicator(ITEM_BYTES_TX, format.format(byteTx));
					row.setIndicator(ITEM_PACKETS_RX, format.format(packetsRx));
					row.setIndicator(ITEM_PACKETS_TX, format.format(packetsTx));
					row.setIndicator(ITEM_DROPPED_PACKETS_RX, format.format(droppedPacketsRx));
					row.setIndicator(ITEM_DROPPED_PACKETS_TX, format.format(droppedPacketsTx));
					row.setIndicator(ITEM_ERROR_PACKETS_RX, format.format(errorPacketsRx));
					row.setIndicator(ITEM_ERROR_PACKETS_TX, format.format(errorPacketsTx));

					row.setIndicator(ITEM_VM_NET_IP_ADDRESS, MonitorUtil.getAvailableStringValue(infoMap.get(mor).get(PROPERTY_VM_IP_ADDRESS)));
					row.setIndicator(ITEM_VM_OS, MonitorUtil.getAvailableStringValue(infoMap.get(mor).get(PROPERTY_VM_OS)));

					result.addRow(row);
				}
			}
		} catch (VSphereException e) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(e.getMessage());
		} finally {
			try {
				if (connection != null) {
					connection.disconnect();
				}
			} catch (VSphereException e) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc("断开vSphere连接失败\n" + e.getMessage());
			}
		}
		return result;
	}

	/**
	 * 连接VSphere SDK
	 * 返回VSphere SDK，该连接的状态为：已连接
	 *
	 * @param ipAddress
	 * @param userName
	 * @param password
	 * @return
	 * @throws CollectException
	 */
	protected VSphereConnection connectVSphereSDK(String ipAddress, String userName, String password)
	throws CollectException {
		VSphereConnection connection = VSphereConnection
		.getVSphereConnection(VSphereConnection.getVSphereSDKUrl(ipAddress));
		try {
			connection.connect(userName, password);
		} catch (VSphereException e) {
			throw new CollectException("连接vSphere sdk失败", e);
		}
		return connection;
	}

	@Override
	public MonitorResult monitor(MonitorContext context) {
		VSphereMonitorMethodOption option = new VSphereMonitorMethodOption(context.getMethod());
		MonitorRecord record = context.getRecord();
		Date lastMonitorTime = null;
		if (record != null) {
			lastMonitorTime = context.getRecord().getTime();
		}
		Date currentTime = new Date();
		int defaultInterval = 10 * 60;
		int maxInterval = 60 * 60;
		int minInterval = 60;

		//如果上次监测时间为空,按10分钟进行计算
		if (lastMonitorTime == null) {
			lastMonitorTime = new Date(currentTime.getTime() - defaultInterval * 1000);
		}

		int interval = (int) (currentTime.getTime() - lastMonitorTime.getTime()) / 1000;
		//如果上次监测时间距现在超出1个小时 按一个小时计算
		if (interval > maxInterval) {
			interval = maxInterval;
		}
		//如果上次监测时间距现在小于60秒,按60秒进行计算
		if (interval < maxInterval) {
			interval = minInterval;
		}

		MonitorResult result = new MonitorResult();
		String host = context.getNode().getIp();
		String user = option.getUsername();
		String password = option.getPassword();
		return collect(result, host, user, password, interval);
	}

	@Override
	public Serializable collect(CollectContext context) {
		VSphereMonitorMethodOption option = new VSphereMonitorMethodOption(context.getMethod());
		MonitorResult result = new MonitorResult();
		String host = context.getNode().getIp();
		String user = option.getUsername();
		String password = option.getPassword();
		return collect(result, host, user, password, 600);
	}

	public static void main(String args[]) {
		VCenterVmNetworkMonitor monitor = new VCenterVmNetworkMonitor();
		monitor.collect(new MonitorResult(), "192.168.0.230", "monitor", "monitor", 6000);

	}
}
