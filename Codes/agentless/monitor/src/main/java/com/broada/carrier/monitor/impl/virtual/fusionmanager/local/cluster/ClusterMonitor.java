package com.broada.carrier.monitor.impl.virtual.fusionmanager.local.cluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.virtual.fusionmanager.local.FMLocalServiceManager;
import com.broada.carrier.monitor.method.fusionmanager.FusionManagerMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.huawei.esdk.fm.local.local.model.ClientProviderBean;
import com.huawei.esdk.fm.local.local.model.compute.QueryResourceClustersResp;
import com.huawei.esdk.fm.local.local.model.compute.ResourceCluster;
import com.huawei.esdk.fm.local.local.model.system.PerfMonitorBasicInfo;
import com.huawei.esdk.fm.local.local.model.system.QueryCapacityDataReq;
import com.huawei.esdk.fm.local.local.model.system.RealtimeMonitorReq;
import com.huawei.esdk.fm.local.local.model.system.RealtimeMonitorResp;
import com.huawei.esdk.fm.local.local.model.system.ResourceCapacityData;
import com.huawei.esdk.fm.local.local.model.system.ResourceCapacityResp;

public class ClusterMonitor extends BaseMonitor {
	public static final String CLUSTER_CPU_USAGE = "cpu_usage";
	public static final String CLUSTER_MEM_USAGE = "mem_usage";
	public static final String CLUSTER_STORAGE__USAGE = "storage_usage";
	public static final String CLUSTER_CPU_CAPACITY_USEDCAPACITY = "cpu_capacity.usedCapacity";
	public static final String CLUSTER_CPU_CAPACITY_ALLOCATED = "cpu_capacity_allocatedCapacity";
	public static final String CLUSTER_CPU_CAPACITY_AVALIABLE = "cpu_capacity_availableCapacity";
	public static final String CLUSTER_CPU_CAPACITY_OTALCAPACITY = "cpu_capacity_otalCapacity";
	public static final String CLUSTER_CPU_CAPACITY_RESERVECAPACITY = "mem_capacity_reserveCapacity";
	public static final String CLUSTER_MEM_CAPACITY_USEDCAPACITY = "mem_capacity.usedCapacity";
	public static final String CLUSTER_MEM_CAPACITY_ALLOCATED = "mem_capacity_allocatedCapacity";
	public static final String CLUSTER_MEM_CAPACITY_AVALIABLE = "mem_capacity_availableCapacity";
	public static final String CLUSTER_MEM_CAPACITY_OTALCAPACITY = "mem_capacity_otalCapacity";
	public static final String CLUSTER_MEM_CAPACITY_RESERVECAPACITY = "mem_capacity_reserveCapacity";
	public static final String CLUSTER_STORAGE_CAPACITY_USEDCAPACITY = "storage_capacity.usedCapacity";
	public static final String CLUSTER_STORAGE_CAPACITY_ALLOCATED = "storage_capacity_allocatedCapacity";
	public static final String CLUSTER_STORAGE_CAPACITY_AVALIABLE = "storage_capacity_availableCapacity";
	public static final String CLUSTER_STORAGE_CAPACITY_OTALCAPACITY = "storage_capacity_otalCapacity";
	public static final String CLUSTER_STORAGE_CAPACITY_RESERVECAPACITY = "storage_capacity_reserveCapacity";
	private static final String LOCAL_VDCID = "1";
	private static List<String> metricList = new ArrayList<String>();
	private static List<String> capacityList = new ArrayList<String>();
	static {
		metricList.add("cpu_usage");
		metricList.add("mem_usage");
		metricList.add("storage_usage");
		capacityList.add("cpu_capacity");
		capacityList.add("mem_capacity");
		capacityList.add("storage_capacity");
	}

	@Override
	public Serializable collect(CollectContext context) {
		FusionManagerMethod method = new FusionManagerMethod(context.getMethod());
		String serverIp = context.getNode().getIp();
		String username = method.getUsername();
		int port = method.getPort();
		ClientProviderBean bean = new ClientProviderBean();
		bean.setServerIp(serverIp);
		bean.setServerPort(String.valueOf(port));
		bean.setUserName(username);
		return getMonitorResult(bean);
	}

	private MonitorResult getMonitorResult(ClientProviderBean bean) {
		MonitorResult result = new MonitorResult();
		List<ClusterPerf> list = getAllClusterPerfs(bean);
		for (ClusterPerf perf : list) {
			MonitorResultRow row = new MonitorResultRow(perf.getId(), perf.getClusterName());
			row.setIndicator(CLUSTER_CPU_CAPACITY_ALLOCATED, perf.getCpu_capacity_allocatedCapacity());
			row.setIndicator(CLUSTER_CPU_CAPACITY_AVALIABLE, perf.getCpu_capacity_availableCapacity());
			row.setIndicator(CLUSTER_CPU_CAPACITY_OTALCAPACITY, perf.getCpu_capacity_otalCapacity());
			row.setIndicator(CLUSTER_CPU_CAPACITY_RESERVECAPACITY, perf.getCpu_capacity_reserveCapacity());
			row.setIndicator(CLUSTER_CPU_CAPACITY_USEDCAPACITY, perf.getCpu_capacity_usedCapacity());
			row.setIndicator(CLUSTER_MEM_CAPACITY_ALLOCATED, perf.getMem_capacity_allocatedCapacity());
			row.setIndicator(CLUSTER_MEM_CAPACITY_AVALIABLE, perf.getMem_capacity_availableCapacity());
			row.setIndicator(CLUSTER_MEM_CAPACITY_OTALCAPACITY, perf.getMem_capacity_otalCapacity());
			row.setIndicator(CLUSTER_MEM_CAPACITY_RESERVECAPACITY, perf.getMem_capacity_reserveCapacity());
			row.setIndicator(CLUSTER_MEM_CAPACITY_USEDCAPACITY, perf.getMem_capacity_usedCapacity());
			row.setIndicator(CLUSTER_STORAGE_CAPACITY_ALLOCATED, perf.getStorage_capacity_allocatedCapacity());
			row.setIndicator(CLUSTER_STORAGE_CAPACITY_AVALIABLE, perf.getStorage_capacity_availableCapacity());
			row.setIndicator(CLUSTER_STORAGE_CAPACITY_OTALCAPACITY, perf.getStorage_capacity_otalCapacity());
			row.setIndicator(CLUSTER_STORAGE_CAPACITY_RESERVECAPACITY, perf.getStorage_capacity_reserveCapacity());
			row.setIndicator(CLUSTER_STORAGE_CAPACITY_USEDCAPACITY, perf.getStorage_capacity_usedCapacity());
			row.setIndicator(CLUSTER_CPU_USAGE, perf.getCpu_usage());
			row.setIndicator(CLUSTER_MEM_USAGE, perf.getMem_usage());
			row.setIndicator(CLUSTER_STORAGE__USAGE, perf.getStorage_usage());
			result.addRow(row);
		}
		return result;
	}

	private List<ClusterPerf> getAllClusterPerfs(ClientProviderBean bean) {
		List<ClusterPerf> results = new ArrayList<ClusterPerf>();
		QueryResourceClustersResp clusterResp = FMLocalServiceManager.getClusterResource(bean).queryAll(LOCAL_VDCID);
		if (clusterResp != null && clusterResp.getResourceClusters() != null) {
			for (ResourceCluster resCluster : clusterResp.getResourceClusters()) {
				ClusterPerf clusterPerf = new ClusterPerf();
				results.add(clusterPerf);
				String id = resCluster.getId();
				QueryCapacityDataReq capacityReq = new QueryCapacityDataReq();
				capacityReq.setMetrics(capacityList);
				capacityReq.setObjectId(id);
				capacityReq.setObjectType("cluster");
				ResourceCapacityResp capacityResp = FMLocalServiceManager.getMonitorResource(bean).queryCapacityData(
						capacityReq);
				generateCapacityData(clusterPerf, capacityResp, resCluster);
				RealtimeMonitorReq monitorReq = new RealtimeMonitorReq();
				PerfMonitorBasicInfo monitorBasicInfo = new PerfMonitorBasicInfo();
				monitorBasicInfo.setObjectId(id);
				monitorBasicInfo.setObjectType("cluster");
				monitorBasicInfo.setMetrics(metricList);
				monitorReq.setPerfMonitorBasicInfo(monitorBasicInfo);
				RealtimeMonitorResp monitorResp = FMLocalServiceManager.getMonitorResource(bean)
						.queryRealtimeMonitorData(monitorReq);
				generateMonitorData(clusterPerf, monitorResp, resCluster);
			}
		}
		return results;
	}

	private void generateCapacityData(ClusterPerf clusterPerf, ResourceCapacityResp capacityResp,
			ResourceCluster resCluster) {
		if (capacityResp != null && capacityResp.getResourceCapacityMap() != null) {
			clusterPerf.setClusterName(resCluster.getName());
			clusterPerf.setId(resCluster.getId());
			for (Entry<String, ResourceCapacityData> entry : capacityResp.getResourceCapacityMap().entrySet()) {
				String metricId = entry.getKey();
				ResourceCapacityData data = entry.getValue();
				if (data != null) {
					if ("cpu_capacity".equalsIgnoreCase(metricId)) {
						if (data.getAllocatedCapacity() != null)
							clusterPerf.setCpu_capacity_allocatedCapacity(data.getAllocatedCapacity());
						if (data.getAvailableCapacity() != null)
							clusterPerf.setCpu_capacity_availableCapacity(data.getAvailableCapacity());
						if (data.getOtalCapacity() != null)
							clusterPerf.setCpu_capacity_otalCapacity(data.getOtalCapacity());
						if (data.getReserveCapacity() != null)
							clusterPerf.setCpu_capacity_reserveCapacity(data.getReserveCapacity());
						if (data.getUsedCapacity() != null)
							clusterPerf.setCpu_capacity_usedCapacity(data.getUsedCapacity());
					} else if ("mem_capacity".equalsIgnoreCase(metricId)) {
						if (data.getAllocatedCapacity() != null)
							clusterPerf.setMem_capacity_allocatedCapacity(data.getAllocatedCapacity());
						if (data.getAvailableCapacity() != null)
							clusterPerf.setMem_capacity_availableCapacity(data.getAvailableCapacity());
						if (data.getOtalCapacity() != null)
							clusterPerf.setMem_capacity_otalCapacity(data.getOtalCapacity());
						if (data.getReserveCapacity() != null)
							clusterPerf.setMem_capacity_reserveCapacity(data.getReserveCapacity());
						if (data.getUsedCapacity() != null)
							clusterPerf.setMem_capacity_usedCapacity(data.getUsedCapacity());
					} else if ("storage_capacity".equalsIgnoreCase(metricId)) {
						if (data.getAllocatedCapacity() != null)
							clusterPerf.setStorage_capacity_allocatedCapacity(data.getAllocatedCapacity());
						if (data.getAvailableCapacity() != null)
							clusterPerf.setStorage_capacity_availableCapacity(data.getAvailableCapacity());
						if (data.getOtalCapacity() != null)
							clusterPerf.setStorage_capacity_otalCapacity(data.getOtalCapacity());
						if (data.getReserveCapacity() != null)
							clusterPerf.setStorage_capacity_reserveCapacity(data.getReserveCapacity());
						if (data.getUsedCapacity() != null)
							clusterPerf.setStorage_capacity_usedCapacity(data.getUsedCapacity());
					}
				}

			}
		}
	}

	private void generateMonitorData(ClusterPerf clusterPerf, RealtimeMonitorResp monitorResp,
			ResourceCluster resCluster) {
		if (monitorResp != null && monitorResp.getRealTimeMonitorMap() != null) {
			clusterPerf.setClusterName(resCluster.getName());
			clusterPerf.setId(resCluster.getId());
			for (Entry<String, String> entry : monitorResp.getRealTimeMonitorMap().entrySet()) {
				String metricId = entry.getKey();
				String value = entry.getValue();
				if (value == null)
					continue;
				double val = Double.parseDouble(value);
				if ("cpu_usage".equalsIgnoreCase(metricId))
					clusterPerf.setCpu_usage(val);
				else if ("mem_usage".equalsIgnoreCase(metricId))
					clusterPerf.setMem_usage(val);
				else if ("storage_usage".equalsIgnoreCase(metricId))
					clusterPerf.setStorage_usage(val);
			}
		}
	}

}
