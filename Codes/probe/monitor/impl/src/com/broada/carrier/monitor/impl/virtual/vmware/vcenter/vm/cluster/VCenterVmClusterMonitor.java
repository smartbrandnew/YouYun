package com.broada.carrier.monitor.impl.virtual.vmware.vcenter.vm.cluster;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.MonitorUtil;
import com.broada.carrier.monitor.impl.virtual.vmware.VSphereMonitor;
import com.broada.carrier.monitor.method.vmware.VSphereConnection;
import com.broada.carrier.monitor.method.vmware.VSphereException;
import com.broada.carrier.monitor.method.vmware.VSphereMonitorMethodOption;
import com.broada.carrier.monitor.method.vmware.VSphereUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.ManagedObjectReference;

public class VCenterVmClusterMonitor extends VSphereMonitor {

	private static final Log LOG = LogFactory.getLog(VCenterVmClusterMonitor.class);
	private static final String DC_HOSTFOLDER = "hostFolder";

	private static final String PROPERTY_VM_UUID = "summary.config.uuid";
	private static final String PROPERTY_VM_NAME = "summary.config.name";
	private static final String PROPERTY_VM_OS = "summary.config.guestFullName";
	private static final String PROPERTY_VM_IP_ADDRESS = "summary.guest.ipAddress";

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult monitorResult = new MonitorResult(MonitorState.FAILED);
		VSphereMonitorMethodOption option = getOption(context);
		try {
			return collectClusterInfo(context.getNode().getIp(), option.getUsername(), option.getPassword());
		} catch (CollectException err) {
			LOG.warn(String.format("获取VCenter 集群性能数据失败。错误：%s", err));
			LOG.debug("堆栈：", err);
			monitorResult.setResultDesc("获取VCenter 集群性能数据失败");
			monitorResult.setState(MonitorState.FAILED);
			return monitorResult;
		}
	}

	private MonitorResult collectClusterInfo(String ip, String username, String password){
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		VSphereConnection connection = null;
		Map<ManagedObjectReference, Map<String, Object>> infoMap = null;
		try {
			connection = connectVSphereSDK(ip, username, password);
			String[][] infoType = new String[][] { new String[] { "Datacenter", DC_HOSTFOLDER},
					new String[] { "VirtualMachine", PROPERTY_VM_UUID, PROPERTY_VM_NAME, 
					PROPERTY_VM_OS, PROPERTY_VM_IP_ADDRESS }};
			infoMap = VSphereUtil.retrieveProperties(connection, infoType);
			if(infoMap != null && !infoMap.isEmpty()){
				// 处理集群资源指标
				Map<String, ManagedObjectReference> cluster_map = new HashMap<String, ManagedObjectReference>();
				Map<String, Object> vm = new HashMap<String, Object>();
				for(ManagedObjectReference key:infoMap.keySet()){
					if (key.getType().equalsIgnoreCase("VirtualMachine")) {
						// 处理对应管理节点
						Map<String, Object> vmInfo = infoMap.get(key);
						// 非本Ip的过滤掉
						if(ip.equalsIgnoreCase(MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_IP_ADDRESS)))){
							vm.put("ip", MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_IP_ADDRESS)));
							vm.put("os", MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_OS)));
							vm.put("id", MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_UUID)) + MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_NAME)));
							vm.put("name", vmInfo.get(PROPERTY_VM_NAME) == null ? "未知" : MonitorUtil.getAvailableStringValue(vmInfo.get(PROPERTY_VM_IP_ADDRESS)));
						}
					} else{
						Map<String, Object> dc_perf = infoMap.get(key);
						if(dc_perf != null){
							Map<String, Object> folder_info = VSphereUtil.retrieveProperties(connection, (ManagedObjectReference)dc_perf.get(DC_HOSTFOLDER), new String[]{"childEntity"});
							for(String ds_attr:folder_info.keySet()){
								ArrayOfManagedObjectReference clusters = (ArrayOfManagedObjectReference) folder_info.get(ds_attr);
								for(ManagedObjectReference cl:clusters.getManagedObjectReference())
									cluster_map.put(cl.get_value(), cl);
							}
						}
					}
				}
				if(!cluster_map.isEmpty() && !vm.isEmpty()){
					for(ManagedObjectReference cl:cluster_map.values()){
						double memory = 0d;
						double capacity = 0d;
						Map<String, Object> cl_info = VSphereUtil.retrieveProperties(connection, cl, new String[]{"summary.totalMemory", "datastore"});
						for(String attr:cl_info.keySet()){
							if(attr.equals("summary.totalMemory"))
								// 集群内存信息
								memory = Double.valueOf(cl_info.get(attr).toString())*1.0/1024/1024/1024;
							else{
								// 集群存储信息
								ArrayOfManagedObjectReference dss = (ArrayOfManagedObjectReference) cl_info.get(attr);
								for(ManagedObjectReference ds:dss.getManagedObjectReference()){
									Map<String, Object> ds_info = VSphereUtil.retrieveProperties(connection, ds, new String[]{"summary.capacity"});
									capacity += Long.valueOf(ds_info.values().toArray()[0].toString())*1.0/1024/1024/1024;
								}
							}
						}
						MonitorResultRow row = new MonitorResultRow(vm.get("id").toString(), vm.get("name").toString());
						row.setIndicator("esx-vm-net-ip-address", vm.get("ip"));
						row.setIndicator("esx-vm-guest-full-name", vm.get("os"));
						row.setIndicator("ESX-VM-CLUSTER-1", memory);
						row.setIndicator("ESX-VM-CLUSTER-2", capacity);
						row.addTag("cluster:" + cl.get_value());
						result.addRow(row);
					}
					result.setState(MonitorState.SUCCESSED);
				}
			}
		} catch (Throwable e) {
			result.setResultDesc("采集cluster集群信息异常");
			LOG.error("采集cluster集群信息异常," + e.getMessage());
		} finally{
			try {
				if (connection != null) {
					connection.disconnect();
				}
			} catch (VSphereException e) {
				LOG.error("关闭sdk连接异常," + e.getMessage());
			}
		}
		return result;
	}

}
