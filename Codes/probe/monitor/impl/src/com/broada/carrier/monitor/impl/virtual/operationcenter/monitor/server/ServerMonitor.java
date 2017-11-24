package com.broada.carrier.monitor.impl.virtual.operationcenter.monitor.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.virtual.operationcenter.config.NodeConfig;
import com.broada.carrier.monitor.impl.virtual.operationcenter.entity.Metric;
import com.broada.carrier.monitor.impl.virtual.operationcenter.entity.ServerInfo;
import com.broada.carrier.monitor.impl.virtual.operationcenter.entity.ServerPerf;
import com.broada.carrier.monitor.impl.virtual.operationcenter.entity.ServerResult;
import com.broada.carrier.monitor.impl.virtual.operationcenter.http.HttpsNode;
import com.broada.carrier.monitor.impl.virtual.operationcenter.http.RequestType;
import com.broada.carrier.monitor.impl.virtual.operationcenter.util.HttpsClientUtil;
import com.broada.carrier.monitor.impl.virtual.operationcenter.util.OCManager;
import com.broada.carrier.monitor.method.operationcenter.OperationCenterMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 服务器监测器
 * @author admin
 *
 */
public class ServerMonitor extends BaseMonitor{
	
	private static Logger LOGGER = LoggerFactory.getLogger(ServerMonitor.class);

	private static Map<String, List<String>> metrics = new HashMap<String, List<String>>();

	static ObjectMapper mapper = new ObjectMapper();

	static{
		metrics.put("cpustate", Arrays.asList(new String[]{"cpuusage"}));
		metrics.put("memstate", Arrays.asList(new String[]{"memusage"}));
		metrics.put("nicstate", Arrays.asList(new String[]{"nicbytein", "nicbyteout", "nicpkgsend", "nicpkgrcv"}));
		metrics.put("diskiostate", Arrays.asList(new String[]{"diskioin", "diskioout"}));
		metrics.put("diskstate", Arrays.asList(new String[]{"diskusage"}));
		metrics.put("nicusagestate", Arrays.asList(new String[]{"nicbyteinusage", "nicbyteoutusage"}));
		metrics.put("dom0", Arrays.asList(new String[]{"dom0cpuusage", "dom0memusage"}));
		metrics.put("domu", Arrays.asList(new String[]{"domucpuusage", "domumemusage"}));
		metrics.put("hostdatastore", Arrays.asList(new String[]{"io_read_number", "io_write_number", "throughput_read", "throughput_write", "latency_read", "latency_write"}));
		metrics.put("clouddisk", Arrays.asList(new String[]{"disk_io_read", "disk_io_write", "disk_latency_read", "disk_latency_write"}));
		metrics.put("storageadapter", Arrays.asList(new String[]{"hba_io_read_number", "hba_io_write_number", "hba_throughput_read", "hba_throughput_write", "hba_latency_read", "hba_latency_write"}));
		metrics.put("necontrlableratestat", Arrays.asList(new String[]{"necontrlablerate"}));
		metrics.put("avgresponsestat", Arrays.asList(new String[]{"avgresponse"}));
		metrics.put("dev_mem_group", Arrays.asList(new String[]{"dev_mem_total", "dev_mem_free"}));
		metrics.put("dev_used_mem_group", Arrays.asList(new String[]{"dev_mem_used"}));
	}
	
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		OperationCenterMethodOption option = new OperationCenterMethodOption(context.getMethod());
		NodeConfig config = NodeConfig.initNodeConfig(option);
		HttpsNode node = new HttpsNode("NBI", option.getOCIp(), 
				String.valueOf(option.getPort()), "HTTPS");
		String tokenId = OCManager.getToken(option, node, config);
		Object serverList = HttpsClientUtil.sendRequestt(node, config, "/oc/v2.3/mo/phy_server?attr=name&attr=ip_addr&attr=id", 
				RequestType.GET, null, null, tokenId);
		if(serverList != null){
			Map<String, Map<String, String>> vms = new HashMap<String, Map<String, String>>();
			try {
				getAllServerInfo(node, config, vms, serverList, tokenId);
			} catch (Exception e) {
				LOGGER.error("获取云主机基本数据异常," , e);
				result.setResultDesc("获取云主机基本数据异常," + e.getMessage());
			}
			if(!vms.isEmpty()){
				List<ServerPerf> perfs = new ArrayList<ServerPerf>();
				for(String key:vms.keySet()){
					String perf_data = OCManager.getPerfmance(node, config, "phy_server", 
							OCManager.getURLEncodedMetrics(key, metrics), tokenId);
					if(perf_data == null || perf_data.endsWith("{}"))
						continue;
					String metrics = perf_data.substring(perf_data.indexOf(":") + 1, perf_data.length());
					try {
						ServerPerf perf = mapper.readValue(metrics, ServerPerf.class);
						perf.setObjId(key);
						Map<String, String> map = vms.get(key);
						perf.setIp_addr(map.get("ip"));
						perf.setName(map.get("name"));
						perfs.add(perf);
					} catch (Exception e) {
						LOGGER.error("获取云主机性能数据异常," , e);
						result.setResultDesc("获取云主机性能数据异常," + e.getMessage());
					} 
				}
				if(!perfs.isEmpty()){
					for(ServerPerf perf:perfs){
						MonitorResultRow row = new MonitorResultRow(perf.getObjId());
						row.setIndicator("ip", perf.getIp_addr());
						row.setIndicator("name", perf.getName());
						row.setIndicator("id", perf.getObjId());
						List<Metric> metrics = perf.getMetrics();
						for(Metric metric:metrics){
							Map<String, String> ms = metric.getMetrics();
							for(String key:ms.keySet()){
								row.setIndicator(key, Float.valueOf(ms.get(key)));
							}
						}
						result.addRow(row);
						result.setState(MonitorState.SUCCESSED);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 将获取到的虚拟机信息写到Map中
	 * @param ret
	 * @param vms
	 */
	private static void resolve(ServerResult ret, Map<String, Map<String, String>> vms) {
		for(ServerInfo info:ret.getPhy_servers()){
			Map<String, String> value = new HashMap<String, String>();
			value.put("id", info.getId());
			value.put("name", info.getName());
			value.put("ip", info.getIp_addr());
			vms.put(info.getId(), value);
		}
	}
	
	/**
	 * 获取所有云虚拟机信息
	 * @param node
	 * @param config
	 * @param vms
	 * @param vmList
	 * @param tokenId
	 */
	private static void getAllServerInfo(HttpsNode node, NodeConfig config, Map<String, Map<String, String>> vms, 
			Object serverList, String tokenId){
		ServerResult ret;
		try {
			ret = mapper.readValue(serverList.toString(), ServerResult.class);
			int total = ret.getTotal();
			int count = ret.getCount();
			int start = Integer.valueOf(ret.getStart());
			if(total == count){
				// 取完了
				resolve(ret, vms);
			}else{
				int times = total/count;
				resolve(ret, vms);
				start = 0;
				String url = "";
				while(times > 0){
					start++;
					url = "/oc/v2.3/mo/phy_server?attr=name&attr=ip_addr&attr=id&limit=10&start=" + start*count;
					serverList = HttpsClientUtil.sendRequestt(node, config, url, RequestType.GET, null, null, tokenId);
					ret = mapper.readValue(serverList.toString(), ServerResult.class);
					resolve(ret, vms);
					times --;
					
				}
				if(total%count !=0){
					url = "/oc/v2.3/mo/phy_server?attr=name&attr=ip_addr&attr=id&limit=10&start=" + (total-start*count+10);
					serverList = HttpsClientUtil.sendRequestt(node, config, url, RequestType.GET, null, null, tokenId);
					ret = mapper.readValue(serverList.toString(), ServerResult.class);
					resolve(ret, vms);
				}
			}
		} catch (Exception e) {
			LOGGER.error("获取云虚拟机基本信息异常", e);
		}
	}

}
