package com.broada.carrier.monitor.impl.virtual.fusionmanager.local.vm;

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
import com.huawei.esdk.fm.local.local.model.system.RealtimeMonitorReq;
import com.huawei.esdk.fm.local.local.model.system.RealtimeMonitorResp;
import com.huawei.esdk.fm.local.local.model.vm.QueryLocalVMListReq;
import com.huawei.esdk.fm.local.local.model.vm.QueryLocalVMListResp;
import com.huawei.esdk.fm.local.local.model.vm.VmInfo;

public class VmMonitor extends BaseMonitor {
	public static final String VM_CPU_USAGE = "cpu_usage";
	public static final String VM_CPU_READY_TIME = "cpu_ready_time";
	public static final String VM_MEM_USAGE = "mem_usage";
	public static final String VM_DISK_IO_IN = "disk_io_in";
	public static final String VM_DISK_IO_OUT = "disk_io_out";
	public static final String VM_NIC_BYTE_IN = "nic_byte_in";
	public static final String VM_NIC_BYTE_OUT = "nic_byte_out";
	public static final String VM_DISK_USAGE = "disk_usage";
	public static final String VM_DISK_OUT_PS = "disk_out_ps";
	public static final String VM_DISK_IN_PS = "disk_in_ps";
	public static final String VM_DISK_READ_DELAY = "disk_read_delay";
	public static final String VM_DISK_WRITE_DELAY = "disk_write_delay";
	public static final String VM_IPADDR = "ip";
	private static final String LOCAL_VDCID = "1";
	public static final String VM_OS = "os";
	private static List<String> metricList = new ArrayList<String>();
	static {
		metricList.add("cpu_usage");
		metricList.add("cpu_ready_time");
		metricList.add("mem_usage");
		metricList.add("disk_io_in");
		metricList.add("disk_io_out");
		metricList.add("nic_byte_in");
		metricList.add("nic_byte_out");
		metricList.add("disk_usage");
		metricList.add("disk_out_ps");
		metricList.add("disk_in_ps");
		metricList.add("disk_read_delay");
		metricList.add("disk_write_delay");
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
		List<VmPerf> list = getAllVmPerfs(bean);
		for (VmPerf perf : list) {
			MonitorResultRow row = new MonitorResultRow(perf.getId(), perf.getHostname());
			row.setIndicator(VM_CPU_READY_TIME, perf.getCpu_ready_time());
			row.setIndicator(VM_CPU_USAGE, perf.getCpu_usage());
			row.setIndicator(VM_DISK_IN_PS, perf.getCpu_usage());
			row.setIndicator(VM_DISK_IO_IN, perf.getDisk_io_in());
			row.setIndicator(VM_DISK_IO_OUT, perf.getDisk_io_out());
			row.setIndicator(VM_DISK_OUT_PS, perf.getDisk_out_ps());
			row.setIndicator(VM_DISK_READ_DELAY, perf.getDisk_read_delay());
			row.setIndicator(VM_DISK_USAGE, perf.getDisk_usage());
			row.setIndicator(VM_DISK_WRITE_DELAY, perf.getDisk_write_delay());
			row.setIndicator(VM_OS, perf.getOs());
			row.setIndicator(VM_MEM_USAGE, perf.getMem_usage());
			row.setIndicator(VM_NIC_BYTE_IN, perf.getNic_byte_in());
			row.setIndicator(VM_NIC_BYTE_OUT, perf.getNic_byte_out());
			result.addRow(row);
		}
		return result;
	}

	private List<VmPerf> getAllVmPerfs(ClientProviderBean bean) {
		List<VmPerf> results = new ArrayList<VmPerf>();
		QueryResourceClustersResp clusterResp = FMLocalServiceManager.getClusterResource(bean).queryAll(LOCAL_VDCID);
		if (clusterResp != null && clusterResp.getResourceClusters() != null) {
			for (ResourceCluster resCluster : clusterResp.getResourceClusters()) {
				VmPerf perf = new VmPerf();
				results.add(perf);
				String id = resCluster.getId();
				QueryLocalVMListReq vmReq = new QueryLocalVMListReq();
				vmReq.setClusterId(id);
				vmReq.setLimit(Integer.MAX_VALUE);
				QueryLocalVMListResp vmListResp = FMLocalServiceManager.getVmResource(bean).queryLocalVMList(vmReq,
						LOCAL_VDCID);
				if (vmListResp != null && vmListResp.getVmInfoList() != null) {
					for (VmInfo vmInfo : vmListResp.getVmInfoList()) {
						String vmId = vmInfo.getId();
						RealtimeMonitorReq monitorReq = new RealtimeMonitorReq();
						PerfMonitorBasicInfo monitorBasicInfo = new PerfMonitorBasicInfo();
						monitorBasicInfo.setObjectId(vmId);
						monitorBasicInfo.setObjectType("host");
						monitorBasicInfo.setMetrics(metricList);
						monitorReq.setPerfMonitorBasicInfo(monitorBasicInfo);
						RealtimeMonitorResp monitorResp = FMLocalServiceManager.getMonitorResource(bean)
								.queryRealtimeMonitorData(monitorReq);
						generateMonitorData(perf, monitorResp, vmInfo);
					}
				}
			}
		}
		return results;
	}

	private void generateMonitorData(VmPerf perf, RealtimeMonitorResp monitorResp, VmInfo vmInfo) {
		if (monitorResp != null && monitorResp.getRealTimeMonitorMap() != null) {
			perf.setHostname(vmInfo.getName());
			perf.setId(vmInfo.getId());
			String os = "linux";
			if (vmInfo.getOs() != null) {
				String osType = vmInfo.getOs().getOsType();
				if (osType != null && osType.toUpperCase().contains("WINDOWS"))
					os = "windows";
			}
			perf.setOs(os);
			for (Entry<String, String> entry : monitorResp.getRealTimeMonitorMap().entrySet()) {
				String metricId = entry.getKey();
				String value = entry.getValue();
				if (value == null)
					continue;
				double val = Double.parseDouble(value);

				metricList.add("cpu_usage");
				metricList.add("cpu_ready_time");
				metricList.add("mem_usage");
				metricList.add("disk_io_in");
				metricList.add("disk_io_out");
				metricList.add("nic_byte_in");
				metricList.add("nic_byte_out");
				metricList.add("disk_usage");
				metricList.add("disk_out_ps");
				metricList.add("disk_in_ps");
				metricList.add("disk_read_delay");
				metricList.add("disk_write_delay");

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
				else if ("disk_read_delay".equalsIgnoreCase(metricId))
					perf.setDisk_read_delay(val);
				else if ("disk_write_delay".equalsIgnoreCase(metricId))
					perf.setDisk_write_delay(val);
				else if ("cpu_ready_time".equalsIgnoreCase(metricId))
					perf.setCpu_ready_time(val);
				else if ("disk_out_ps".equalsIgnoreCase(metricId))
					perf.setDisk_out_ps(val);
				else if ("disk_in_ps".equalsIgnoreCase(metricId))
					perf.setDisk_in_ps(val);
			}
		}
	}
}
