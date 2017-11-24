package com.broada.carrier.monitor.impl.storage.huawei.oceanstor.memory;

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

// 存储设备容量信息
public class MemoryMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(MemoryMonitor.class);
	
	public static final String TOTAL_CAPACITY_OID = ".1.3.6.1.4.1.34774.4.1.1.5";
	public static final String USED_CAPACITY_OID = ".1.3.6.1.4.1.34774.4.1.1.4";
	public static final String MEMORY = "OCEANSTOR-MEMORY-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			Map<String, String> map = getMemory(walk);
			if(map.isEmpty()){
				result.setResultDesc("通过SNMP方式未能查询到OCEANSTOR-MEMORY存储容量信息");
				return result;
			}
			MonitorResultRow row = new MonitorResultRow();
			double total = Double.valueOf(map.get("memory_total_capacity") == null? "0":map.get("memory_total_capacity"));
			double used = Double.valueOf(map.get("memory_used_capacity") == null? "0":map.get("memory_used_capacity"));
			if(total != 0d) {
				row.setIndicator(MEMORY + 1, new BigDecimal(total *1.0).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(MEMORY + 2, new BigDecimal(used *1.0).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(MEMORY + 3, new BigDecimal(used/total*100.0).setScale(2, RoundingMode.HALF_UP));
				result.addRow(row);
			}
			result.setState(MonitorState.SUCCESSED);
		} catch (SnmpException e) {
			LOG.error("通过SNMP方式查询OCEANSTOR-MEMORY存储容量信息异常，" + e.getMessage());
			result.setResultDesc("通过SNMP方式查询OCEANSTOR-MEMORY存储容量信息异常");
		}
		return result;
	}
	
	/**
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, String> getMemory(SnmpWalk walk) throws SnmpException{
		Map<String, String> map = new HashMap<String, String>();
		// 获取总容量
		SnmpResult[] totalResult = walk.snmpWalk(TOTAL_CAPACITY_OID);
		// 获取已使用容量
		SnmpResult[] usedResult = walk.snmpWalk(USED_CAPACITY_OID);
		if(totalResult != null && totalResult.length > 0){
			map.put("memory_total_capacity", totalResult[0].getValue().toString());
		}
		if(usedResult != null && usedResult.length > 0){
			map.put("memory_used_capacity", usedResult[0].getValue().toString());
		}
		return map;
	}

}
