package com.broada.carrier.monitor.impl.virtual.operationcenter.monitor.cloundvm;

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
import com.broada.carrier.monitor.impl.virtual.operationcenter.entity.VMInfo;
import com.broada.carrier.monitor.impl.virtual.operationcenter.entity.VMPerf;
import com.broada.carrier.monitor.impl.virtual.operationcenter.entity.VMResult;
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
 * 云虚拟机监测器
 * @author admin
 *
 */
public class CloundVMMonitor extends BaseMonitor{
	
	private static Logger LOGGER = LoggerFactory.getLogger(CloundVMMonitor.class);
	
	private static Map<String, List<String>> metrics = new HashMap<String, List<String>>();
	
	static ObjectMapper mapper = new ObjectMapper();
	
	static{
		metrics.put("virtrualkpi", Arrays.asList(new String[]{"cpuusage" , "memusage", "diskioin", "diskioout", "nicbytein", "nicbyteout"}));
		metrics.put("diskstate", Arrays.asList(new String[]{"diskusage"}));
		metrics.put("cpuinhost", Arrays.asList(new String[]{"cpuusageinhost"}));
		metrics.put("virtualdisk", Arrays.asList(new String[]{"io_read_number", "io_write_number", "disk_io_in", "disk_io_out", "latency_read", "latency_write"}));
		metrics.put("phydisk", Arrays.asList(new String[]{"disk_rd_ios", "disk_wr_ios", "disk_rd_sectors", "disk_wr_sectors", "disk_iord_ticks", "disk_iowr_ticks", "disk_tot_ticks"}));
		metrics.put("nicbyteinout", Arrays.asList(new String[]{"nicbyte_in_out"}));
		metrics.put("diskioinout", Arrays.asList(new String[]{"diskio_in_out"}));
		metrics.put("commonstatus", Arrays.asList(new String[]{"status"}));
		metrics.put("vmnicstate", Arrays.asList(new String[]{"nic_rx_drop_pkt_speed", "nic_tx_drop_pkt_speed"}));
	}
	
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		OperationCenterMethodOption option = new OperationCenterMethodOption(context.getMethod());
		NodeConfig config = NodeConfig.initNodeConfig(option);
		HttpsNode node = new HttpsNode("NBI", option.getOCIp(), 
				String.valueOf(option.getPort()), "HTTPS");
		String tokenId = OCManager.getToken(option, node, config);
		Object vmList = HttpsClientUtil.sendRequestt(node, config, "/oc/v2.3/mo/server?attr=name&attr=ip_addr&attr=id&attr=status", 
				RequestType.GET, null, null, tokenId);
		if(vmList != null){
			Map<String, Map<String, String>> vms = new HashMap<String, Map<String, String>>();
			try {
				getAllVMInfo(node, config, vms, vmList, tokenId);
			} catch (Exception e) {
				LOGGER.error("获取云虚拟机基本数据异常," , e);
				result.setResultDesc("获取云虚拟机基本数据异常," + e.getMessage());
			}
			if(!vms.isEmpty()){
				List<VMPerf> perfs = new ArrayList<VMPerf>();
				for(String key:vms.keySet()){
					String perf_data = OCManager.getPerfmance(node, config, "server", 
							OCManager.getURLEncodedMetrics(key, metrics), tokenId);
					if(perf_data == null || perf_data.endsWith("{}"))
						continue;
					String metrics = perf_data.substring(perf_data.indexOf(":") + 1, perf_data.length());
					try {
						VMPerf perf = mapper.readValue(metrics, VMPerf.class);
						perf.setObjId(key);
						Map<String, String> map = vms.get(key);
						perf.setIp_addr(map.get("ip"));
						perf.setName(map.get("name"));
						perfs.add(perf);
					} catch (Exception e) {
						LOGGER.error("获取云虚拟机性能数据异常," , e);
						result.setResultDesc("获取云虚拟机性能数据异常," + e.getMessage());
					} 
				}
				if(!perfs.isEmpty()){
					for(VMPerf perf:perfs){
						MonitorResultRow row = new MonitorResultRow(perf.getObjId());
						row.setIndicator("ip", perf.getIp_addr());
						row.setIndicator("name", perf.getName());
						row.setIndicator("id", perf.getObjId());
						List<Metric> metrics = perf.getMetrics();
						for(Metric metric:metrics){
							Map<String, String> ms = metric.getMetrics();
							for(String key:ms.keySet()){
								if(key.equalsIgnoreCase("status") && ms.get(key).equalsIgnoreCase("running"))
									row.setIndicator(key, 1f);
								else
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
	private static void resolve(VMResult ret, Map<String, Map<String, String>> vms) {
		for(VMInfo info:ret.getServers()){
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
	private static void getAllVMInfo(HttpsNode node, NodeConfig config, Map<String, Map<String, String>> vms, 
			Object vmList, String tokenId){
		VMResult ret;
		try {
			ret = mapper.readValue(vmList.toString(), VMResult.class);
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
					url = "/oc/v2.3/mo/server?attr=name&attr=status&attr=ip_addr&attr=id&limit=10&start=" + start*count;
					vmList = HttpsClientUtil.sendRequestt(node, config, url, RequestType.GET, null, null, tokenId);
					ret = mapper.readValue(vmList.toString(), VMResult.class);
					resolve(ret, vms);
					times --;
					
				}
				if(total%count !=0){
					url = "/oc/v2.3/mo/server?attr=name&attr=status&attr=ip_addr&attr=id&limit=10&start=" + (total-start*count+10);
					vmList = HttpsClientUtil.sendRequestt(node, config, url, RequestType.GET, null, null, tokenId);
					ret = mapper.readValue(vmList.toString(), VMResult.class);
					resolve(ret, vms);
				}
			}
		} catch (Exception e) {
			LOGGER.error("获取云虚拟机基本信息异常", e);
		}
	}
	
}
