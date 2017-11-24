package com.broada.carrier.monitor.impl.storage.huawei.oceanstor.responsetime;

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

// 响应时间
public class ResponseTimeMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(ResponseTimeMonitor.class);
	
	public static final String OID = ".1.3.6.1.4.1.34774.4.1.21.3.1.13";
	public static final String RESPONSE_TIME = "OCEANSTOR-RESPONSE-TIME-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			Map<String, Map<String,String>> maps = getResponseTime(walk);
			if(maps.isEmpty()){
				result.setResultDesc("通过SNMP方式未能查询到OCEANSTOR-RESPONSE-TIME响应时间");
				return result;
			}
			for (Map.Entry<String, Map<String, String>> entry : maps.entrySet()) {  
				Map<String, String> map = entry.getValue();
				MonitorResultRow row = new MonitorResultRow(entry.getKey());
				row.setIndicator(RESPONSE_TIME + 1, new BigDecimal(map.get("response_time")).setScale(2, RoundingMode.HALF_UP));
				row.addTag("response_time_"+ entry.getKey());
				result.addRow(row);
			}  
			result.setState(MonitorState.SUCCESSED);
		} catch (SnmpException e) {
			LOG.error("通过SNMP方式查询OCEANSTOR-RESPONSE-TIME响应时间异常，" + e.getMessage());
			result.setResultDesc("通过SNMP方式查询OCEANSTOR-RESPONSE-TIME响应时间异常");
		}
		return result;
	}
	
	/**
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, Map<String,String>> getResponseTime(SnmpWalk walk) throws SnmpException{
		Map<String, Map<String,String>> maps = new HashMap<String, Map<String,String>>();
		SnmpResult[] results = walk.snmpWalk(OID);
		LOG.info("response_time_size:" + results.length);
		if(results != null && results.length > 0){
			for (SnmpResult snmpResult : results) {
				String oid = snmpResult.getOid().toString();
				LOG.info("response_time_oid:" + oid);
				LOG.info("response_time_value:" + snmpResult.getValue().toString());
				String node = oid.substring(oid.lastIndexOf(".")+1);
				Map<String, String> map = null;
				if(maps.get(node) == null){
					map = new HashMap<String,String>();
				}else {
					map = maps.get(node);
				}
				map.put("response_time", snmpResult.getValue().toString());
				if(map != null && !map.isEmpty()){
					maps.put(node, map);
				}
			}
		}
		return maps;
	}

}
