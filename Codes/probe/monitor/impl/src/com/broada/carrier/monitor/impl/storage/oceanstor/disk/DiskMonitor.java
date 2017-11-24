package com.broada.carrier.monitor.impl.storage.oceanstor.disk;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

public class DiskMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(DiskMonitor.class);
	
	public static final String PREFIX_TABLE_ID = ".1.3.6.1.4.1.2011.2.251.36.5.5.1.1";
	public static final String DISK_SLOT_INDEX = ".2";
	public static final String SECTION_COUNT_INDEX = ".10";
	public static final String SECTION_SIZE_INDEX = ".11";
	public static final String SECTION_BANDWIDTH_INDEX = ".12";
	
	public static final String DISK_UTILIZATION = "OCEANSTOR9000-DISK-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		// 扩展 OceanStor9000的配置项
		String cluster_id = context.getMethod().getProperties().get("cluster_id", "");
		if(StringUtil.isNullOrBlank(cluster_id)){
			result.setResultDesc("通过SNMP方式未能查询到CPU利用率信息,缺少cluster_id的配置");
			return result;
		}
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		String[] arrays = cluster_id.split(",");
		if(arrays != null && arrays.length > 0){
			for(String clusterId:arrays){
				try {
					Map<String, Map<String, String>> map = getDiskUtilization(walk);
					if(map.isEmpty()){
						result.setResultDesc("通过SNMP方式未能查询到Disk性能信息");
						return result;
					}
					for(String key:map.keySet()){
						MonitorResultRow row = new MonitorResultRow(key);
						Map<String, String> perfs = map.get(key);
						String count = perfs.get("section.count");
						String size = perfs.get("section.size");
						BigDecimal capacity = new BigDecimal(count).multiply(new BigDecimal(size)).divide(new BigDecimal(1024*1024));
						row.setIndicator(DISK_UTILIZATION + 1, capacity.setScale(2, RoundingMode.HALF_UP));
						row.setIndicator(DISK_UTILIZATION + 2, new BigDecimal(Double.valueOf(perfs.get("section.bandwidth"))*1.0).setScale(2, RoundingMode.HALF_UP));
						row.addTag("cluster_id:" + clusterId);
						row.addTag("disk_id:" + key);     // 磁盘添加磁盘标签
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
	private Map<String, Map<String, String>> getDiskUtilization(SnmpWalk walk) throws SnmpException{
		Map<String, Map<String, String>> ret = new HashMap<String, Map<String,String>>();
		// 存储每个磁盘的oid和名称
		Map<String, String> map = new HashMap<String, String>();
		SnmpResult[] results = walk.snmpWalk(PREFIX_TABLE_ID);
		if(results != null ){
			String slot = PREFIX_TABLE_ID.concat(DISK_SLOT_INDEX);
			for(SnmpResult sr:results){
				// map 存储 磁盘oid与其对应的 字符串
				if(sr.getOid().toString().startsWith(slot)){
					map.put(sr.getOid().toString().substring(slot.length(), sr.getOid().toString().length()), sr.getValue().toString());
				}
			}
			if(!map.isEmpty()){
				for(String key:map.keySet()){
					Map<String, String> tmp = new HashMap<String, String>();
					for(SnmpResult sr:results){
						if(sr.getOid().toString().contains(key)){
							if(sr.getOid().toString().startsWith(PREFIX_TABLE_ID.concat(SECTION_COUNT_INDEX))){
								tmp.put("section.count", sr.getValue().toString());
							} else if(sr.getOid().toString().startsWith(PREFIX_TABLE_ID.concat(SECTION_SIZE_INDEX))){
								tmp.put("section.size", sr.getValue().toString());
							} else if(sr.getOid().toString().startsWith(PREFIX_TABLE_ID.concat(SECTION_BANDWIDTH_INDEX))){
								tmp.put("section.bandwidth", sr.getValue().toString());
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
