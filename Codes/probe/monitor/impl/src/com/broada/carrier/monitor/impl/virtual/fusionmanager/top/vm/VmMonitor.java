package com.broada.carrier.monitor.impl.virtual.fusionmanager.top.vm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.virtual.fusionmanager.top.FMTopServiceManager;
import com.broada.carrier.monitor.method.fusionmanager.FusionManagerMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.huawei.esdk.fusionmanager.local.model.ClientProviderBean;
import com.huawei.esdk.fusionmanager.local.model.common.CloudInfraBaseInfo;
import com.huawei.esdk.fusionmanager.local.model.common.GetVdcListReqEx;
import com.huawei.esdk.fusionmanager.local.model.common.GetVdcListResp;
import com.huawei.esdk.fusionmanager.local.model.common.ListCloudInfraReqEx;
import com.huawei.esdk.fusionmanager.local.model.common.ListCloudInfraResp;
import com.huawei.esdk.fusionmanager.local.model.common.VdcInfo;
import com.huawei.esdk.fusionmanager.local.model.net.ListVPCReqEx;
import com.huawei.esdk.fusionmanager.local.model.net.ListVPCResp;
import com.huawei.esdk.fusionmanager.local.model.net.VPC;
import com.huawei.esdk.fusionmanager.local.model.system.PerfMonitorBasicInfo;
import com.huawei.esdk.fusionmanager.local.model.system.RealtimeMonitorReq;
import com.huawei.esdk.fusionmanager.local.model.system.RealtimeMonitorResp;
import com.huawei.esdk.fusionmanager.local.model.vm.ListVmInfoReq;
import com.huawei.esdk.fusionmanager.local.model.vm.ListVmInfoResp;
import com.huawei.esdk.fusionmanager.local.model.vm.VmBaseInfo;

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
	public static final String VM_OS = "os";
	private static List<String> metricList = new ArrayList<String>();
	static {
		metricList.add("cpu_usage");
		metricList.add("mem_usage");
		metricList.add("disk_io_in");
		metricList.add("disk_io_out");
		metricList.add("nic_byte_in");
		metricList.add("nic_byte_out");
		metricList.add("disk_usage");
		metricList.add("disk_out_ps");
		metricList.add("disk_in_ps");
		metricList.add("cpu_ready_time");
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
		List<VmPerf> list = getAllHostPerfs(bean);
		for (VmPerf perf : list) {
			MonitorResultRow row = new MonitorResultRow(perf.getId(), perf.getName());
			row.setIndicator(VM_OS, perf.getOs());
			row.setIndicator(VM_CPU_READY_TIME, perf.getCpu_ready_time());
			row.setIndicator(VM_CPU_USAGE, perf.getCpu_usage());
			row.setIndicator(VM_DISK_IN_PS, perf.getDisk_in_ps());
			row.setIndicator(VM_DISK_IO_IN, perf.getDisk_io_in());
			row.setIndicator(VM_DISK_IO_OUT, perf.getDisk_io_out());
			row.setIndicator(VM_DISK_OUT_PS, perf.getDisk_out_ps());
			row.setIndicator(VM_DISK_READ_DELAY, perf.getDisk_read_delay());
			row.setIndicator(VM_DISK_USAGE, perf.getDisk_usage());
			row.setIndicator(VM_DISK_WRITE_DELAY, perf.getDisk_write_delay());
			row.setIndicator(VM_MEM_USAGE, perf.getMem_usage());
			row.setIndicator(VM_NIC_BYTE_IN, perf.getNic_byte_in());
			row.setIndicator(VM_NIC_BYTE_OUT, perf.getNic_byte_out());
			result.addRow(row);
		}
		return result;
	}

	private List<VmPerf> getAllHostPerfs(ClientProviderBean bean) {
		List<VmPerf> results = new ArrayList<VmPerf>();
		GetVdcListReqEx get = new GetVdcListReqEx();
		get.setLimit(Integer.MAX_VALUE);
		get.setStart(0);
		GetVdcListResp vdcResp = FMTopServiceManager.getVdcResource(bean).list(get);
		if (vdcResp != null && vdcResp.getVdcList() != null) {
			for (VdcInfo vdcInfo : vdcResp.getVdcList()) {
				String vdcId = vdcInfo.getId();
				ListCloudInfraReqEx cloudReq = new ListCloudInfraReqEx();
				cloudReq.setVdcId(vdcId);
				cloudReq.setStart(0);
				cloudReq.setLimit(Integer.MAX_VALUE);
				ListCloudInfraResp cloudResp = FMTopServiceManager.getCloudInfraBatchResource(bean).list(cloudReq);
				if (cloudResp != null && cloudResp.getCloudInfras() != null) {
					for (CloudInfraBaseInfo cloudInfo : cloudResp.getCloudInfras()) {
						String cloudId = cloudInfo.getId();
						ListVPCReqEx vpcReq = new ListVPCReqEx();
						vpcReq.setCloudInfraId(cloudId);
						vpcReq.setVdcId(vdcId);
						vpcReq.setLimit(Integer.MAX_VALUE);
						vpcReq.setStart(0);
						ListVPCResp vpcResp = FMTopServiceManager.getVpcResource(bean).list(vpcReq);
						if (vpcResp != null && vpcResp.getVpcs() != null) {
							for (VPC vpc : vpcResp.getVpcs()) {
								String vpcId = vpc.getVpcID();
								ListVmInfoReq vmInfoReq = new ListVmInfoReq();
								vmInfoReq.setStart(0);
								vmInfoReq.setLimit(Integer.MAX_VALUE);
								ListVmInfoResp vmResp = FMTopServiceManager.getVmResource(bean).list(vmInfoReq, vdcId,
										vpcId, cloudId);
								if (vmResp != null && vmResp.getVms() != null) {
									for (VmBaseInfo vmInfo : vmResp.getVms()) {
										VmPerf vmPerf = new VmPerf();
										results.add(vmPerf);
										String vmId = vmInfo.getId();
										RealtimeMonitorReq monitorReq = new RealtimeMonitorReq();
										PerfMonitorBasicInfo basicInfo = new PerfMonitorBasicInfo();
										basicInfo.setMetrics(metricList);
										basicInfo.setObjectId(vmId);
										basicInfo.setObjectType("vm");
										monitorReq.setPerfMonitorBasicInfo(basicInfo);
										RealtimeMonitorResp monitorResp = FMTopServiceManager.getMonitorResource(bean)
												.queryRealtimeMonitorData(monitorReq, vdcId, vpcId, cloudId);
										generateMonitorData(vmPerf, monitorResp, vmInfo);
									}
								}
							}
						}
					}
				}
			}
		}
		return results;
	}

	private void generateMonitorData(VmPerf perf, RealtimeMonitorResp monitorResp, VmBaseInfo vmInfo) {
		if (monitorResp != null && monitorResp.getRealTimeMonitorMap() != null) {
			perf.setName(vmInfo.getName());
			perf.setId(vmInfo.getId());
			String osVersion = vmInfo.getOsVersion();
			String os = "linux";
			if (osVersion != null && osVersion.toUpperCase().contains("WINDOWS"))
				os = "windows";
			perf.setOs(os);
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
				else if ("disk_out_ps".equalsIgnoreCase(metricId))
					perf.setDisk_out_ps(val);
				else if ("disk_in_ps".equalsIgnoreCase(metricId))
					perf.setDisk_in_ps(val);
				else if ("cpu_ready_time".equalsIgnoreCase(metricId))
					perf.setCpu_ready_time(val);
				else if ("disk_read_delay".equalsIgnoreCase(metricId))
					perf.setDisk_read_delay(val);
				else if ("disk_write_delay".equalsIgnoreCase(metricId))
					perf.setDisk_write_delay(val);

			}
		}
	}

}
