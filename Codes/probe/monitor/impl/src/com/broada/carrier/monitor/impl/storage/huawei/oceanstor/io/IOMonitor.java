package com.broada.carrier.monitor.impl.storage.huawei.oceanstor.io;

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

// IO 使用  IOPS 替代
public class IOMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(IOMonitor.class);
	
	public static final String OID = ".1.3.6.1.4.1.34774.4.1.21.3.1.5";
	public static final String IO_UTILIZATION = "OCEANSTOR-IO-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			Map<String, Map<String,String>> maps = getIOUtilization(walk);
			if(maps.isEmpty()){
				result.setResultDesc("通过SNMP方式未能查询到OCEANSTOR-IO利用率信息");
				return result;
			}
			for (Map.Entry<String, Map<String, String>> entry : maps.entrySet()) {  
				Map<String, String> map = entry.getValue();
				MonitorResultRow row = new MonitorResultRow(entry.getKey());
				row.setIndicator(IO_UTILIZATION + 1, new BigDecimal(Double.valueOf(map.get("io_utilization"))*1.0).setScale(2, RoundingMode.HALF_UP));
				row.addTag("io_utilization_" + entry.getKey());
				result.addRow(row);
			}  
			result.setState(MonitorState.SUCCESSED);
		} catch (SnmpException e) {
			LOG.error("通过SNMP方式查询OCEANSTOR-IO利用率信息异常，" + e.getMessage());
			result.setResultDesc("通过SNMP方式查询OCEANSTOR-IO利用率信息异常");
		}
		return result;
	}
	
	/**
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, Map<String,String>> getIOUtilization(SnmpWalk walk) throws SnmpException{
		Map<String, Map<String,String>> maps = new HashMap<String, Map<String,String>>();
		SnmpResult[] results = walk.snmpWalk(OID);
		if(results != null && results.length > 0){
			for (SnmpResult snmpResult : results) {
				String oid = snmpResult.getOid().toString();
				String node = oid.substring(oid.lastIndexOf(".")+1);
				Map<String, String> map = null;
				if(maps.get(node) == null){
					map = new HashMap<String,String>();
				}else {
					map = maps.get(node);
				}
				map.put("io_utilization", snmpResult.getValue().toString());
				if(map != null && !map.isEmpty()){
					maps.put(node, map);
				}
			}
		}
		return maps;
	}

}
