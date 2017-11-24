package com.broada.carrier.monitor.impl.storage.huawei.oceanstor.ram;

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
import com.broada.snmp.SnmpGet;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpOID;
import com.broada.snmputil.SnmpResult;

public class RamMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(RamMonitor.class);
	
	public static final String OID = ".1.3.6.1.4.1.34774.4.1.23.5.2.1.9";
	public static final String RAM_UTILIZATION = "OCEANSTOR-RAM-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpGet walk = new SnmpGet(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			Map<String, String> map = getRamUtilization(walk);
			if(map.isEmpty()){
				result.setResultDesc("通过SNMP方式未能查询到OCEANSTOR内存利用率信息");
				return result;
			}
			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(RAM_UTILIZATION + 1, new BigDecimal(Double.valueOf(map.get("ram_utilization"))*1.0).setScale(2, RoundingMode.HALF_UP));
			result.addRow(row);
			result.setState(MonitorState.SUCCESSED);
		} catch (SnmpException e) {
			LOG.error("通过SNMP方式查询OCEANSTOR内存利用率信息异常，" + e.getMessage());
			result.setResultDesc("通过SNMP方式查询OCEANSTOR内存利用率信息异常");
		}
				
		return result;
	}
	
	/**
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, String> getRamUtilization(SnmpGet walk) throws SnmpException{
		Map<String, String> map = new HashMap<String, String>();
		SnmpResult result = walk.snmpSynGet(new SnmpOID(OID));
		if(result != null){
			map.put("ram_utilization", result.getValue().toString());
		}
		return map;
	}

}
