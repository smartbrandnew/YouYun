package com.broada.carrier.monitor.impl.storage.oceanstor.quota;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;
import com.broada.utils.StringUtil;

public class QuotaMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(QuotaMonitor.class);
	
	public static final String PREFIX_TABLE_ID = ".1.3.6.1.4.1.2011.2.251.36.5.22.1.1";
	public static final String QUOTA_PATH_INDEX = ".3";
	public static final String QUOTA_TYPE_INDEX = ".14";
	public static final String QUOTA_USED_INDEX = ".5";
	public static final String QUOTA_AVAILABLE_INDEX = ".6";
	
	public static final String DISK_UTILIZATION = "OCEANSTOR9000-QUOTA-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		// 扩展 OceanStor9000的配置项
		String cluster_id = context.getMethod().getProperties().get("cluster_id", "");
		if(StringUtil.isNullOrBlank(cluster_id)){
			result.setResultDesc("通过SNMP方式未能查询到Quota利用率信息,缺少cluster_id的配置");
			return result;
		}
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		String[] arrays = cluster_id.split(",");
		if(arrays != null && arrays.length > 0){
			for(String clusterId:arrays){
				try {
					Map<String, Map<String, String>> map = getQuotaInfo(walk);
					if(map.isEmpty()){
						result.setResultDesc("通过SNMP方式未能查询到Quota性能信息");
						return result;
					}
					for(String key:map.keySet()){
						MonitorResultRow row = new MonitorResultRow(key);
						Map<String, String> perfs = map.get(key);
						if(Integer.valueOf(perfs.get("quota.type").toString()) == 0){
							// 容量配额
							row.setIndicator(DISK_UTILIZATION + 2, perfs.get("quota.used"));
							row.setIndicator(DISK_UTILIZATION + 3, perfs.get("quota.available"));
							row.setIndicator(DISK_UTILIZATION + 4, Double.valueOf(perfs.get("quota.used")) + Double.valueOf(perfs.get("quota.available")));
						} else if(Integer.valueOf(perfs.get("quota.type").toString()) == 1){
							// 文件配额
							row.setIndicator(DISK_UTILIZATION + 5, perfs.get("quota.used"));
							row.setIndicator(DISK_UTILIZATION + 6, perfs.get("quota.available"));
							row.setIndicator(DISK_UTILIZATION + 7, Double.valueOf(perfs.get("quota.used")) + Double.valueOf(perfs.get("quota.available")));
						} else{
							continue;
						}
						row.addTag("cluster_id:" + clusterId);
						row.addTag("quota:" + key);     // 添加配额标签
						result.addRow(row);
					}
					result.setState(MonitorState.SUCCESSED);
				} catch (SnmpException e) {
					LOG.error("通过SNMP方式查询CPU利用率信息异常，" + e.getMessage());
					result.setResultDesc("通过SNMP方式查询CPU利用率信息异常");
				}
			}
		}
		return result;
	}
	
	/**
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, Map<String, String>> getQuotaInfo(SnmpWalk walk) throws SnmpException{
		Map<String, Map<String, String>> ret = new HashMap<String, Map<String,String>>();
		Map<String, String> map = new HashMap<String, String>();
		SnmpResult[] results = walk.snmpWalk(PREFIX_TABLE_ID);
		if(results != null ){
			String slot = PREFIX_TABLE_ID.concat(QUOTA_PATH_INDEX);
			for(SnmpResult sr:results){
				if(sr.getOid().toString().startsWith(slot)){
					map.put(sr.getOid().toString().substring(slot.length(), sr.getOid().toString().length()), sr.getValue().toString());
				}
			}
			if(!map.isEmpty()){
				for(String key:map.keySet()){
					Map<String, String> tmp = new HashMap<String, String>();
					for(SnmpResult sr:results){
						if(sr.getOid().toString().contains(key)){
							if(sr.getOid().toString().startsWith(PREFIX_TABLE_ID.concat(QUOTA_TYPE_INDEX))){
								tmp.put("quota.type", sr.getValue().toString());
							} else if(sr.getOid().toString().startsWith(PREFIX_TABLE_ID.concat(QUOTA_USED_INDEX))){
								tmp.put("quota.used", sr.getValue().toString());
							} else if(sr.getOid().toString().startsWith(PREFIX_TABLE_ID.concat(QUOTA_AVAILABLE_INDEX))){
								tmp.put("quota.available", sr.getValue().toString());
							}
						}
					}
					ret.put(map.get(key), tmp);
				}
			}
		}
		return ret;
	}
}
