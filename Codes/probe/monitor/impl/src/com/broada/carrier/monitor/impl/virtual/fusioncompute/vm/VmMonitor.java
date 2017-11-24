package com.broada.carrier.monitor.impl.virtual.fusioncompute.vm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.virtual.fusioncompute.FCServiceManager;
import com.broada.carrier.monitor.method.fusioncompute.FusionComputeMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.utils.StringUtil;
import com.huawei.esdk.fusioncompute.local.model.ClientProviderBean;
import com.huawei.esdk.fusioncompute.local.model.FCSDKResponse;
import com.huawei.esdk.fusioncompute.local.model.PageList;
import com.huawei.esdk.fusioncompute.local.model.common.LoginResp;
import com.huawei.esdk.fusioncompute.local.model.common.Metric;
import com.huawei.esdk.fusioncompute.local.model.common.Objectmetric;
import com.huawei.esdk.fusioncompute.local.model.common.QueryObjectmetricReq;
import com.huawei.esdk.fusioncompute.local.model.common.QueryObjectmetricResp;
import com.huawei.esdk.fusioncompute.local.model.site.SiteBasicInfo;
import com.huawei.esdk.fusioncompute.local.model.vm.Nic;
import com.huawei.esdk.fusioncompute.local.model.vm.QueryVmsReq;
import com.huawei.esdk.fusioncompute.local.model.vm.VmConfig;
import com.huawei.esdk.fusioncompute.local.model.vm.VmInfo;
import com.huawei.esdk.fusioncompute.local.resources.common.AuthenticateResource;
import com.huawei.esdk.fusioncompute.local.resources.site.SiteResource;

/**
 * 监测Fusion 虚拟机信息
 * 
 * @author WIN
 * 
 */
public class VmMonitor extends BaseMonitor {
	public static final String VM_CPU_USAGE = "cpu_usage";
	public static final String VM_CPU_READY_TIME = "cpu_ready_time";
	public static final String VM_MEM_USAGE = "mem_usage";
	public static final String VM_DISK_IO_IN = "disk_io_in";
	public static final String VM_DISK_IO_OUT = "disk_io_out";
	public static final String VM_NIC_BYTE_IN = "nic_byte_in";
	public static final String VM_NIC_BYTE_OUT = "nic_byte_out";
	public static final String VM_DISK_IOWR_TICKS = "disk_iowr_ticks";
	public static final String VM_DISK_USAGE = "disk_usage";
	public static final String VM_DISK_REQ_IN = "disk_req_in";
	public static final String VM_DISK_REQ_OUT = "disk_req_out";
	public static final String VM_DISK_IORD_TICKS = "disk_iord_ticks";
	public static final String VM_IPADDR = "ip";
	public static final String VM_OS = "os";
	private static List<String> metricList = new ArrayList<String>();
	
	private static final Logger LOG = LoggerFactory.getLogger(VmMonitor.class);
	
	static {
		metricList.add("cpu_usage");
		metricList.add("cpu_ready_time");
		metricList.add("mem_usage");
		metricList.add("disk_io_in");
		metricList.add("disk_io_out");
		metricList.add("nic_byte_in");
		metricList.add("nic_byte_out");
		metricList.add("disk_iowr_ticks");
		metricList.add("disk_usage");
		metricList.add("disk_req_in");
		metricList.add("disk_req_out");
		metricList.add("disk_iord_ticks");
	}

	@Override
	public Serializable collect(CollectContext context) {
		FusionComputeMethod method = new FusionComputeMethod(context.getMethod());
		String serverIp = context.getNode().getIp();
		int port = method.getPort();
		String username = method.getUsername();
		ClientProviderBean bean = new ClientProviderBean();
		bean.setServerIp(serverIp);
		bean.setServerPort(String.valueOf(port));
		bean.setUserName(username);
		String password = method.getPassword();
		return getResult(bean, password);
	}

	private List<VmPerf> getAllVmPerfs(ClientProviderBean bean, String password) {
		List<VmPerf> results = new ArrayList<VmPerf>();
		AuthenticateResource authenticateResource = FCServiceManager.getUserService(bean);
		if(authenticateResource == null){
			LOG.error("Failed to get authenticateResource object!");
			return results;
		}
		FCSDKResponse<LoginResp> loginResp = authenticateResource.login(bean.getUserName(), password);
        if (!"00000000".equals(loginResp.getErrorCode())) {
        	// 鉴权失败
        	LOG.error("Failed to Login FC System!");
        	return results;
        }
        LOG.info("Login Success!");
		SiteResource siteResource = FCServiceManager.getSiteResource(bean);
		if(siteResource == null){
			LOG.info("查询虚拟机性能数据时没找到站点!");
			return results;
		}
		FCSDKResponse<List<SiteBasicInfo>> siteResponse = siteResource.querySites();
		List<SiteBasicInfo> siteList = siteResponse.getResult();
		if (siteList == null){
			LOG.info("查询虚拟机性能数据时没找到站点!");
			return results;
		}
		for (SiteBasicInfo siteInfo : siteList) {
			String siteUri = siteInfo.getUri();
			FCSDKResponse<PageList<VmInfo>> vmResp = FCServiceManager.getVmResource(bean).queryVMs(new QueryVmsReq(), siteUri);
			if (vmResp != null) {
				if (vmResp.getResult() != null && vmResp.getResult().getList() != null) {
					for (VmInfo vm : vmResp.getResult().getList()) {
						String urn = vm.getUrn();
						String ip = getFusionComputeVmIp(vm);
						if(StringUtil.isNullOrBlank(ip)) continue;
						VmPerf perf = new VmPerf();
						perf.setIp(ip);   //  取不到ip就不管了
						QueryObjectmetricReq req = new QueryObjectmetricReq();
						List<QueryObjectmetricReq> queryList = new ArrayList<QueryObjectmetricReq>();
						queryList.add(req);
						req.setUrn(urn);
						req.setMetricId(metricList);
						FCSDKResponse<QueryObjectmetricResp> response = FCServiceManager.getMonitorResource(bean)
								.queryObjectmetricRealtimedata(siteUri, queryList);
						QueryObjectmetricResp result = response.getResult();
						results.add(perf);
						if (result != null && result.getItems() != null) {
							for (Objectmetric metric : result.getItems()) {
								List<Metric> metricList = metric.getValue();
								if (metricList != null) {
									for (Metric m : metricList) {
										String uuid = vm.getUuid();
										if (uuid == null)
											uuid = vm.getUrn();
										if (uuid == null)
											continue;
										String os = "linux";
										if (vm.getOsOptions() != null)
											os = vm.getOsOptions().getOsType();
										generatePerf(perf, m, uuid, vm.getName(), os);
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

	private MonitorResult getResult(ClientProviderBean bean, String password) {
		MonitorResult result = new MonitorResult();
		List<VmPerf> list = getAllVmPerfs(bean, password);
		if(list.isEmpty()){
			result.setMessage("未查询到虚拟机性能数据!");
			return result;
		}
		for (VmPerf perf : list) {
			MonitorResultRow row = new MonitorResultRow(perf.getUuid(), perf.getHostname());
			row.setIndicator(VM_OS, perf.getOs());
			row.setIndicator(VM_CPU_READY_TIME, perf.getCpu_ready_time());
			row.setIndicator(VM_CPU_USAGE, perf.getCpu_usage());
			row.setIndicator(VM_DISK_IO_IN, perf.getDisk_io_in());
			row.setIndicator(VM_DISK_IO_OUT, perf.getDisk_io_out());
			row.setIndicator(VM_DISK_IORD_TICKS, perf.getDisk_iord_ticks());
			row.setIndicator(VM_DISK_IOWR_TICKS, perf.getDisk_iowr_ticks());
			row.setIndicator(VM_DISK_REQ_IN, perf.getDisk_req_in());
			row.setIndicator(VM_DISK_REQ_OUT, perf.getDisk_req_out());
			row.setIndicator(VM_DISK_USAGE, perf.getDisk_usage());
			// 暂时取不到IP
			row.setIndicator(VM_IPADDR, perf.getIp());
			row.setIndicator(VM_MEM_USAGE, perf.getMem_usage());
			row.setIndicator(VM_NIC_BYTE_IN, perf.getNic_byte_in());
			row.setIndicator(VM_NIC_BYTE_OUT, perf.getNic_byte_out());
			result.addRow(row);
		}
		result.setState(MonitorState.SUCCESSED);
		return result;
	}

	private VmPerf generatePerf(VmPerf perf, Metric metric, String uuid, String hostName, String os) {
//		String name = metric.getMetricId();
		String val = metric.getMetricValue();
		if (StringUtil.isNullOrBlank(val))
			return perf;
//		String unit = metric.getUnit();
//		MetricMetaDataGenerator.getInstance().addMetaData("vm", name, unit);
		double v = Double.parseDouble(val);
		perf.setHostname(hostName);
		perf.setUuid(uuid);
		if (os != null && os.toUpperCase().contains("WINDOWS"))
			os = "windows";
		else
			os = "linux";
		perf.setOs(os);
		if ("cpu_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setCpu_usage(v);
		} else if ("cpu_ready_time".equalsIgnoreCase(metric.getMetricId())) {
			perf.setCpu_ready_time(v);
		} else if ("mem_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setMem_usage(v);
		} else if ("disk_io_in".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDisk_io_in(v);
		} else if ("disk_io_out".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDisk_io_out(v);
		} else if ("nic_byte_in".equalsIgnoreCase(metric.getMetricId())) {
			perf.setNic_byte_in(v);
		} else if ("nic_byte_out".equalsIgnoreCase(metric.getMetricId())) {
			perf.setNic_byte_out(v);
		} else if ("disk_iowr_ticks".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDisk_iowr_ticks(v);
		} else if ("disk_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDisk_usage(v);
		} else if ("disk_req_in".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDisk_req_in(v);
		} else if ("disk_req_out".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDisk_req_out(v);
		} else if ("disk_iord_ticks".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDisk_iord_ticks(v);
		}
		return perf;
	}
	
	/**
	 * 获取虚拟机Ip
	 * @return
	 */
	private String getFusionComputeVmIp(VmInfo vmInfo){
		String[] ips = null;
		VmConfig cfg = vmInfo.getVmConfig();
		if(cfg != null){
			List<Nic> nics = cfg.getNics();
			if(nics != null && !nics.isEmpty()){
				ips = new String[nics.size()];
				for(int i=0;i<nics.size();i++)
					ips[i] = nics.get(i).getIp();
			}
		}
		// 虚拟机有ip
		if(ips == null)
			return null;
		if(ips.length == 1)
			return ips[0];
		else
			return getMinIp(ips);
	}
	
	/**
	 * 返回最小ip
	 * @param ips
	 * @return
	 */
	private String getMinIp(String[] ips){
		String ipMin = ips[0];
		String[] ipArray = ipMin.split("\\.");
		for(int i=1;i<ips.length;i++){
			String[] currArray = ips[i].split("\\.");
			for(int j=0;j<4;j++){
				if(Integer.valueOf(ipArray[j]).intValue() > Integer.valueOf(currArray[j]).intValue()){
					ipMin = ips[i];
					ipArray = ipMin.split("\\.");
					break;
				} else if(Integer.valueOf(ipArray[j]).intValue() == Integer.valueOf(currArray[j]).intValue())
					continue;
				else
					break;
			}
		}
		return ipMin;
	}
	
}
