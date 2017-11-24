package com.broada.carrier.monitor.impl.virtual.fusioncompute.host;

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
import com.huawei.esdk.fusioncompute.local.model.host.HostBasicInfo;
import com.huawei.esdk.fusioncompute.local.model.host.QueryHostListReq;
import com.huawei.esdk.fusioncompute.local.model.site.SiteBasicInfo;
import com.huawei.esdk.fusioncompute.local.resources.common.AuthenticateResource;
import com.huawei.esdk.fusioncompute.local.resources.site.SiteResource;

/**
 * 监测Fusion 主机信息
 * 
 * @author WIN
 * 
 */
public class HostMonitor extends BaseMonitor {
	public static final String HOST_CPU_USAGE = "cpu_usage";
	public static final String HOST_DOMAIN_O_CPU_USAGE = "dom0_cpu_usage";
	public static final String HOST_DOMAIN_O_MEM_USAGE = "dom0_mem_usage";
	public static final String HOST_DOMAIN_U_CPU_USAGE = "domU_cpu_usage";
	public static final String HOST_DOMAIN_U_MEM_USAGE = "domU_mem_usage";
	public static final String HOST_MEM_USAGE = "mem_usage";
	public static final String HOST_DISK_IO_IN = "disk_io_in";
	public static final String HOST_DISK_IO_OUT = "disk_io_out";
	public static final String HOST_LOGIC_DISK_USAGE = "logic_disk_usage";
	public static final String HOST_NIC_PKG_SEND = "nic_pkg_send";
	public static final String HOST_NIC_PKG_RCV = "nic_pkg_rcv";
	public static final String HOST_NIC_BYTE_IN_USAGE = "nic_byte_in_usage";
	public static final String HOST_NIC_BYTE_IN = "nic_byte_in";
	public static final String HOST_NIC_BYTE_OUT_USAGE = "nic_byte_out_usage";
	public static final String HOST_NIC_BYTE_OUT = "nic_byte_out";
	public static final String HOST_IPADDR = "ip";
	public static final String HOST_OS = "os";
	private static List<String> metricList = new ArrayList<String>();
	
	private static final Logger LOG = LoggerFactory.getLogger(HostMonitor.class);
	
	static {
		metricList.add("cpu_usage");
		metricList.add("dom0_cpu_usage");
		metricList.add("dom0_mem_usage");
		metricList.add("domU_cpu_usage");
		metricList.add("domU_mem_usage");
		metricList.add("mem_usage");
		metricList.add("disk_io_in");
		metricList.add("disk_io_out");
		metricList.add("logic_disk_usage");
		metricList.add("nic_pkg_send");
		metricList.add("nic_pkg_rcv");
		metricList.add("nic_byte_in_usage");
		metricList.add("nic_byte_in");
		metricList.add("nic_byte_out_usage");
		metricList.add("nic_byte_out");
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

	private MonitorResult getResult(ClientProviderBean bean, String password) {
		MonitorResult result = new MonitorResult();
		List<HostPerf> list = getAllHostPerfs(bean, password);
		if(list.isEmpty()){
			result.setMessage("未查询到主机性能数据!");
			return result;
		}
		for (HostPerf perf : list) {
			MonitorResultRow row = new MonitorResultRow(perf.getUrn(), perf.getHostname());
			row.setIndicator(HOST_OS, perf.getOs());
			row.setIndicator(HOST_CPU_USAGE, perf.getCpu_usage());
			row.setIndicator(HOST_DISK_IO_IN, perf.getDisk_io_in());
			row.setIndicator(HOST_DISK_IO_OUT, perf.getDisk_io_out());
			row.setIndicator(HOST_DOMAIN_O_CPU_USAGE, perf.getDom0_cpu_usage());
			row.setIndicator(HOST_DOMAIN_O_MEM_USAGE, perf.getDom0_mem_usage());
			row.setIndicator(HOST_DOMAIN_U_CPU_USAGE, perf.getDomU_cpu_usage());
			row.setIndicator(HOST_DOMAIN_U_MEM_USAGE, perf.getDomU_mem_usage());
			row.setIndicator(HOST_IPADDR, perf.getIp());
			row.setIndicator(HOST_LOGIC_DISK_USAGE, perf.getLogic_disk_usage());
			row.setIndicator(HOST_MEM_USAGE, perf.getMem_usage());
			row.setIndicator(HOST_NIC_BYTE_IN, perf.getNic_byte_in());
			row.setIndicator(HOST_NIC_BYTE_IN_USAGE, perf.getNic_byte_in_usage());
			row.setIndicator(HOST_NIC_BYTE_OUT, perf.getNic_byte_out());
			row.setIndicator(HOST_NIC_BYTE_OUT_USAGE, perf.getNic_byte_out_usage());
			row.setIndicator(HOST_NIC_PKG_RCV, perf.getNic_pkg_rcv());
			row.setIndicator(HOST_NIC_PKG_SEND, perf.getNic_pkg_send());
			result.addRow(row);
		}
		result.setState(MonitorState.SUCCESSED);
		return result;
	}

	private List<HostPerf> getAllHostPerfs(ClientProviderBean bean, String password) {
		List<HostPerf> results = new ArrayList<HostPerf>();
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
		SiteResource siteResource = FCServiceManager.getSiteResource(bean);
		if(siteResource == null){
			LOG.info("查询主机性能数据时没找到任何站点资源!");
			return results;
		}
		
		FCSDKResponse<List<SiteBasicInfo>> siteResponse = siteResource.querySites();
		List<SiteBasicInfo> siteList = siteResponse.getResult();
		if (siteList == null){
			LOG.info("查询主机性能数据时没找到站点!");
			return results;
		}
		for (SiteBasicInfo siteInfo : siteList) {
			String siteUri = siteInfo.getUri();
			FCSDKResponse<PageList<HostBasicInfo>> hostResp = FCServiceManager.getHostResource(bean).queryHostList(
					siteUri, new QueryHostListReq());
			if (hostResp != null) {
				PageList<HostBasicInfo> pageList = hostResp.getResult();
				if (pageList != null && pageList.getList() != null) {
					for (HostBasicInfo hostInfo : pageList.getList()) {
						String urn = hostInfo.getUrn();
						QueryObjectmetricReq req = new QueryObjectmetricReq();
						List<QueryObjectmetricReq> queryList = new ArrayList<QueryObjectmetricReq>();
						queryList.add(req);
						req.setUrn(urn);
						req.setMetricId(metricList);
						FCSDKResponse<QueryObjectmetricResp> response = FCServiceManager.getMonitorResource(bean)
								.queryObjectmetricRealtimedata(siteUri, queryList);
						QueryObjectmetricResp result = response.getResult();
						HostPerf perf = new HostPerf();
						if(StringUtil.isNullOrBlank(hostInfo.getIp())) continue;
						perf.setIp(hostInfo.getIp());
						results.add(perf);
						if (result != null && result.getItems() != null) {
							for (Objectmetric metric : result.getItems()) {
								List<Metric> metricList = metric.getValue();
								if (metricList != null) {
									for (Metric m : metricList) {
										generatePerf(perf, m, urn, hostInfo.getName(), hostInfo.getIp());
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

	private HostPerf generatePerf(HostPerf perf, Metric metric, String urn, String hostName, String ip) {
//		String name = metric.getMetricId();
		String val = metric.getMetricValue();
		if (StringUtil.isNullOrBlank(val))
			return perf;
//		String unit = metric.getUnit();
//		MetricMetaDataGenerator.getInstance().addMetaData("host", name, unit);
		double v = Double.parseDouble(val);
		perf.setHostname(hostName);
		perf.setUrn(urn);
		if ("cpu_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setCpu_usage(v);
		} else if ("dom0_cpu_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDom0_cpu_usage(v);
		} else if ("dom0_mem_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDom0_mem_usage(v);
		} else if ("domU_cpu_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDomU_cpu_usage(v);
		} else if ("domU_mem_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDomU_mem_usage(v);
		} else if ("mem_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setMem_usage(v);
		} else if ("disk_io_in".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDisk_io_in(v);
		} else if ("disk_io_out".equalsIgnoreCase(metric.getMetricId())) {
			perf.setDisk_io_out(v);
		} else if ("logic_disk_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setLogic_disk_usage(v);
		} else if ("nic_pkg_send".equalsIgnoreCase(metric.getMetricId())) {
			perf.setNic_pkg_send(v);
		} else if ("nic_pkg_rcv".equalsIgnoreCase(metric.getMetricId())) {
			perf.setNic_pkg_rcv(v);
		} else if ("nic_byte_in_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setNic_byte_in_usage(v);
		} else if ("nic_byte_in".equalsIgnoreCase(metric.getMetricId())) {
			perf.setNic_byte_in(v);
		} else if ("nic_byte_out_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setNic_byte_out_usage(v);
		} else if ("nic_byte_out".equalsIgnoreCase(metric.getMetricId())) {
			perf.setNic_byte_out(v);
		}
		return perf;
	}

}
