package com.broada.carrier.monitor.impl.virtual.fusionmanager.local.host;

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
import com.huawei.esdk.fm.local.local.model.compute.Host;
import com.huawei.esdk.fm.local.local.model.compute.QueryHostReq;
import com.huawei.esdk.fm.local.local.model.compute.QueryHostResp;
import com.huawei.esdk.fm.local.local.model.compute.QueryResourceClustersResp;
import com.huawei.esdk.fm.local.local.model.compute.ResourceCluster;
import com.huawei.esdk.fm.local.local.model.system.PerfMonitorBasicInfo;
import com.huawei.esdk.fm.local.local.model.system.RealtimeMonitorReq;
import com.huawei.esdk.fm.local.local.model.system.RealtimeMonitorResp;

public class HostMonitor extends BaseMonitor {
	public static final String HOST_CPU_USAGE = "cpu_usage";
	public static final String HOST_MEM_USAGE = "mem_usage";
	public static final String HOST_DISK_IO_IN = "disk_io_in";
	public static final String HOST_DISK_IO_OUT = "disk_io_out";
	public static final String HOST_NIC_BYTE_IN = "nic_byte_in";
	public static final String HOST_NIC_BYTE_OUT = "nic_byte_out";
	public static final String HOST_IPADDR = "ip";
	public static final String HOST_DISK_USAGE = "disk_usage";
	public static final String HOST_NET_RECEIVE_PKG_RATE = "net_receive_pkg_rate";
	public static final String HOST_NET_SEND_PKG_RATE = "net_send_pkg_rate";
	private static final String LOCAL_VDCID = "1";
	private static List<String> metricList = new ArrayList<String>();
	static {
		metricList.add("cpu_usage");
		metricList.add("mem_usage");
		metricList.add("disk_io_in");
		metricList.add("disk_io_out");
		metricList.add("nic_byte_in");
		metricList.add("nic_byte_out");
		metricList.add("disk_usage");
		metricList.add("net_receive_pkg_rate");
		metricList.add("net_send_pkg_rate");
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
		List<HostPerf> list = getAllHostPerfs(bean);
		for (HostPerf perf : list) {
			MonitorResultRow row = new MonitorResultRow(perf.getId(), perf.getHostname());
			row.setIndicator(HOST_CPU_USAGE, perf.getCpu_usage());
			row.setIndicator(HOST_DISK_IO_IN, perf.getDisk_io_in());
			row.setIndicator(HOST_DISK_IO_OUT, perf.getDisk_io_out());
			row.setIndicator(HOST_DISK_USAGE, perf.getDisk_usage());
			row.setIndicator(HOST_IPADDR, perf.getIp());
			row.setIndicator(HOST_MEM_USAGE, perf.getMem_usage());
			row.setIndicator(HOST_NET_RECEIVE_PKG_RATE, perf.getNet_receive_pkg_rate());
			row.setIndicator(HOST_NET_SEND_PKG_RATE, perf.getNet_send_pkg_rate());
			row.setIndicator(HOST_NIC_BYTE_IN, perf.getNic_byte_in());
			row.setIndicator(HOST_NIC_BYTE_OUT, perf.getNic_byte_out());
			result.addRow(row);
		}
		return result;
	}

	private List<HostPerf> getAllHostPerfs(ClientProviderBean bean) {
		List<HostPerf> results = new ArrayList<HostPerf>();
		QueryResourceClustersResp clusterResp = FMLocalServiceManager.getClusterResource(bean).queryAll(LOCAL_VDCID);
		if (clusterResp != null && clusterResp.getResourceClusters() != null) {
			for (ResourceCluster resCluster : clusterResp.getResourceClusters()) {
				HostPerf perf = new HostPerf();
				results.add(perf);
				String id = resCluster.getId();
				QueryHostReq hostReq = new QueryHostReq();
				hostReq.setClusterId(id);
				hostReq.setLimit(Integer.MAX_VALUE);
				QueryHostResp hostResp = FMLocalServiceManager.getClusterResource(bean).queryHostList(LOCAL_VDCID,
						hostReq);
				if (hostResp != null && hostResp.getHosts() != null) {
					for (Host host : hostResp.getHosts()) {
						String hostId = host.getId();
						RealtimeMonitorReq monitorReq = new RealtimeMonitorReq();
						PerfMonitorBasicInfo monitorBasicInfo = new PerfMonitorBasicInfo();
						monitorBasicInfo.setObjectId(hostId);
						monitorBasicInfo.setObjectType("host");
						monitorBasicInfo.setMetrics(metricList);
						monitorReq.setPerfMonitorBasicInfo(monitorBasicInfo);
						RealtimeMonitorResp monitorResp = FMLocalServiceManager.getMonitorResource(bean)
								.queryRealtimeMonitorData(monitorReq);
						generateMonitorData(perf, monitorResp, host);
					}
				}
			}
		}
		return results;
	}

	private void generateMonitorData(HostPerf perf, RealtimeMonitorResp monitorResp, Host host) {
		if (monitorResp != null && monitorResp.getRealTimeMonitorMap() != null) {
			perf.setHostname(host.getName());
			perf.setId(host.getId());
			perf.setIp(host.getHostIp());
			for (Entry<String, String> entry : monitorResp.getRealTimeMonitorMap().entrySet()) {
				String metricId = entry.getKey();
				String value = entry.getValue();
				if (value == null)
					continue;
				double val = Double.parseDouble(value);
				if ("cpu_usage".equalsIgnoreCase(metricId))
					perf.setCpu_usage(val);
				else if ("mem_usage".equalsIgnoreCase(metricId))
					perf.setMem_usage(val);
				else if ("disk_io_in".equalsIgnoreCase(metricId))
					perf.setDisk_io_in(val);
				else if ("disk_io_out".equalsIgnoreCase(metricId))
					perf.setDisk_io_out(val);
				else if ("nic_byte_in".equalsIgnoreCase(metricId))
					perf.setNic_byte_in(val);
				else if ("nic_byte_out".equalsIgnoreCase(metricId))
					perf.setNic_byte_in(val);
				else if ("disk_usage".equalsIgnoreCase(metricId))
					perf.setDisk_usage(val);
				else if ("net_receive_pkg_rate".equalsIgnoreCase(metricId))
					perf.setNet_receive_pkg_rate(val);
				else if ("net_send_pkg_rate".equalsIgnoreCase(metricId))
					perf.setNet_send_pkg_rate(val);

			}
		}
	}
}
