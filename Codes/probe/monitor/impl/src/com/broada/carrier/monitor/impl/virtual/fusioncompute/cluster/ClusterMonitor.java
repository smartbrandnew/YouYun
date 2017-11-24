package com.broada.carrier.monitor.impl.virtual.fusioncompute.cluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.virtual.fusioncompute.FCServiceManager;
import com.broada.carrier.monitor.impl.virtual.fusioncompute.MetricMetaDataGenerator;
import com.broada.carrier.monitor.method.fusioncompute.FusionComputeMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.huawei.esdk.fusioncompute.local.model.ClientProviderBean;
import com.huawei.esdk.fusioncompute.local.model.FCSDKResponse;
import com.huawei.esdk.fusioncompute.local.model.cluster.ClusterBasicInfo;
import com.huawei.esdk.fusioncompute.local.model.common.LoginResp;
import com.huawei.esdk.fusioncompute.local.model.common.Metric;
import com.huawei.esdk.fusioncompute.local.model.common.Objectmetric;
import com.huawei.esdk.fusioncompute.local.model.common.QueryObjectmetricReq;
import com.huawei.esdk.fusioncompute.local.model.common.QueryObjectmetricResp;
import com.huawei.esdk.fusioncompute.local.model.site.SiteBasicInfo;
import com.huawei.esdk.fusioncompute.local.resources.common.AuthenticateResource;
import com.huawei.esdk.fusioncompute.local.resources.site.SiteResource;

/**
 * 监测Fusion集群信息
 * 
 * @author WIN
 * 
 */
public class ClusterMonitor extends BaseMonitor {
	public static final String CLUSTER_CPU_USAGE = "cpu_usage";
	public static final String CLUSTER_MEM_USAGE = "mem_usage";
	public static final String CLUSTER_LOGIC_DISK_USAGE = "logic_disk_usage";
	public static final String CLUSTER_NIC_BYTE_IN_USAGE = "nic_byte_in_usage";
	public static final String CLUSTER_NIC_BYTE_IN = "nic_byte_in";
	public static final String CLUSTER_NIC_BYTE_OUT_USAGE = "nic_byte_out_usage";
	public static final String CLUSTER_NIC_BYTE_OUT = "nic_byte_out";

	private static List<String> metricList = new ArrayList<String>();
	
	private static final Logger LOG = LoggerFactory.getLogger(ClusterMonitor.class);
	
	static {
		metricList.add("cpu_usage");
		metricList.add("mem_usage");
		metricList.add("logic_disk_usage");
		metricList.add("nic_byte_in_usage");
		metricList.add("nic_byte_in");
		metricList.add("nic_byte_out_usage");
		metricList.add("nic_byte_out");
	}

	@Override
	public Serializable collect(CollectContext context) {
		FusionComputeMethod method = new FusionComputeMethod(context.getMethod());
		String serverIp = context.getNode().getIp();
		String username = method.getUsername();
		int port = method.getPort();
		ClientProviderBean bean = new ClientProviderBean();
		bean.setServerIp(serverIp);
		bean.setServerPort(String.valueOf(port));
		bean.setUserName(username);
		String password = method.getPassword();
		return getResult(bean, password);
	}

	private MonitorResult getResult(ClientProviderBean bean, String password) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		List<ClusterPerf> list = getAllClusterPerfs(bean, password);
		if(list.isEmpty()){
			result.setMessage("未查询到集群性能数据!");
			return result;
		}
		for (ClusterPerf perf : list) {
			MonitorResultRow row = new MonitorResultRow(perf.getUrn(), perf.getClusterName());
			row.setIndicator(CLUSTER_CPU_USAGE, perf.getCpu_usage());
			row.setIndicator(CLUSTER_LOGIC_DISK_USAGE, perf.getLogic_disk_usage());
			row.setIndicator(CLUSTER_MEM_USAGE, perf.getMem_usage());
			row.setIndicator(CLUSTER_NIC_BYTE_IN, perf.getNic_byte_in());
			row.setIndicator(CLUSTER_NIC_BYTE_IN_USAGE, perf.getNic_byte_in_usage());
			row.setIndicator(CLUSTER_NIC_BYTE_OUT, perf.getNic_byte_out());
			row.setIndicator(CLUSTER_NIC_BYTE_OUT_USAGE, perf.getNic_byte_out_usage());
			result.addRow(row);
		}
		result.setState(MonitorState.SUCCESSED);
		return result;
	}

	private List<ClusterPerf> getAllClusterPerfs(ClientProviderBean bean, String password) {
		List<ClusterPerf> results = new ArrayList<ClusterPerf>();
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
			LOG.info("查询集群性能数据时没找到任何站点资源!");
			return results;
		}
		FCSDKResponse<List<SiteBasicInfo>> siteResponse = siteResource.querySites();
		List<SiteBasicInfo> siteList = siteResponse.getResult();
		if (siteList == null)
			return results;
		for (SiteBasicInfo siteInfo : siteList) {
			String siteURI = siteInfo.getUri();
			FCSDKResponse<List<ClusterBasicInfo>> clusterResp = FCServiceManager.getClusterResource(bean)
					.queryClusters(siteURI, null, null, null, null);
			List<ClusterBasicInfo> clusterList = clusterResp.getResult();
			if (clusterList != null) {
				for (ClusterBasicInfo clusterInfo : clusterList) {
					String clusterUrn = clusterInfo.getUrn();
					QueryObjectmetricReq req = new QueryObjectmetricReq();
					List<QueryObjectmetricReq> queryList = new ArrayList<QueryObjectmetricReq>();
					queryList.add(req);
					req.setUrn(clusterUrn);
					req.setMetricId(metricList);
					FCSDKResponse<QueryObjectmetricResp> response = FCServiceManager.getMonitorResource(bean)
							.queryObjectmetricRealtimedata(siteURI, queryList);
					QueryObjectmetricResp result = response.getResult();
					ClusterPerf perf = new ClusterPerf();
					results.add(perf);
					if (result != null && result.getItems() != null) {
						for (Objectmetric metric : result.getItems()) {
							List<Metric> metricList = metric.getValue();
							if (metricList != null) {
								for (Metric m : metricList) {
									generatePerf(perf, m, clusterUrn, clusterInfo.getName());
								}
							}
						}
					}
				}
			}
		}
		return results;
	}

	private ClusterPerf generatePerf(ClusterPerf perf, Metric metric, String urn, String clusterName) {
		String name = metric.getMetricId();
		String val = metric.getMetricValue();
		if (val == null)
			return perf;
		LOG.info("开始生成集群性能数据");
		String unit = metric.getUnit();
		MetricMetaDataGenerator.getInstance().addMetaData("cluster", name, unit);
		double v = Double.parseDouble(val);
		perf.setClusterName(clusterName);
		perf.setUrn(urn);
		if ("cpu_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setCpu_usage(v);
		} else if ("mem_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setMem_usage(v);
		} else if ("logic_disk_usage".equalsIgnoreCase(metric.getMetricId())) {
			perf.setLogic_disk_usage(v);
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
