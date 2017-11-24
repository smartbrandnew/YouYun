package com.broada.carrier.monitor.impl.storage.huawei.oceanstor.transmission;

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

public class TransmissionRateMonitor extends BaseMonitor {
	// 传输速率 = IOPS * 平均IO大小
	private static final Log LOG = LogFactory.getLog(TransmissionRateMonitor.class);
	
	public static final String OID = ".1.3.6.1.4.1.2011.2.251.21.11.1";
	public static final String IOPS_INDEX = ".5";
	public static final String IO_INDEX = ".13";
	public static final BigDecimal UNIT = new BigDecimal(1000000);
	public static final String RATE_UTILIZATION = "OCEANSTOR-RATE-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpGet walk = new SnmpGet(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			Map<String, String> map = getTransferRateUtilization(walk);
			if(map.isEmpty()){
				result.setResultDesc("通过SNMP方式未能查询到OCEANSTOR传输速率信息");
				return result;
			}
			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(RATE_UTILIZATION + 1, new BigDecimal(map.get("transferrate")).setScale(2, RoundingMode.HALF_UP));
			result.addRow(row);
			result.setState(MonitorState.SUCCESSED);
		} catch (SnmpException e) {
			LOG.error("通过SNMP方式查询OCEANSTOR传输速率异常，" + e.getMessage());
			result.setResultDesc("通过SNMP方式查询OCEANSTOR传输速率信息异常");
		}
		return result;
	}
	
	/**
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, String> getTransferRateUtilization(SnmpGet walk) throws SnmpException{
		Map<String, String> ret = new HashMap<String, String>();
		// 获取IOPS
		SnmpResult iopsResult = walk.snmpSynGet(new SnmpOID(OID + IOPS_INDEX));
		// 获取平均IO大小
		SnmpResult ioResult = walk.snmpSynGet(new SnmpOID(OID + IO_INDEX));
		if(iopsResult != null && ioResult != null){
			BigDecimal iops = new BigDecimal(iopsResult.getValue().toString());
			BigDecimal io = new BigDecimal(ioResult.getValue().toString());
			BigDecimal transferrate = iops.multiply(io).divide(UNIT);
			ret.put("transferrate", transferrate.toString());
		}
		return ret;
	}

}