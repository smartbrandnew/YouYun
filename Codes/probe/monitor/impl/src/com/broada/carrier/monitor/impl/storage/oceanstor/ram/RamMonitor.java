package com.broada.carrier.monitor.impl.storage.oceanstor.ram;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.storage.oceanstor.cpu.CPUMonitor;
import com.broada.carrier.monitor.impl.storage.oceanstor.util.ToAscii;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpGet;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpOID;
import com.broada.snmputil.SnmpResult;
import com.broada.utils.StringUtil;

public class RamMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(CPUMonitor.class);
	
	public static final String PREFIX_TABLE_ID = ".1.3.6.1.4.1.2011.2.251.21.11.1";
	public static final String RAM_UTILIZATION_OID = "3";
	public static final String RAM_UTILIZATION = "OCEANSTOR9000-RAM-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		// 扩展 OceanStor9000的配置项
		String cluster_id = context.getMethod().getProperties().get("cluster_id", "");
		if(StringUtil.isNullOrBlank(cluster_id)){
			result.setResultDesc("通过SNMP方式未能查询到内存利用率信息,缺少cluster_id的配置");
			return result;
		}
		SnmpGet walk = new SnmpGet(snmpMethod.getTarget(context.getNode().getIp()));
		String[] arrays = cluster_id.split(",");
		if(arrays != null && arrays.length > 0)
			for(String clusterId:arrays){
				try {
					Map<String, String> map = getRamUtilization(walk, clusterId, clusterId.length());
					if(map.isEmpty()){
						result.setResultDesc("通过SNMP方式未能查询到内存利用率信息");
						return result;
					}
					MonitorResultRow row = new MonitorResultRow(clusterId);
					row.setIndicator(RAM_UTILIZATION + 1, new BigDecimal(Double.valueOf(map.get("ram_utilization"))*1.0).setScale(2, RoundingMode.HALF_UP));
					row.addTag("cluster_id:" + clusterId);
					result.addRow(row);
					result.setState(MonitorState.SUCCESSED);
				} catch (SnmpException e) {
					LOG.error("通过SNMP方式查询内存利用率信息异常，" + e.getMessage());
					result.setResultDesc("通过SNMP方式查询内存利用率信息异常");
				}
				
			}
		return result;
	}
	
	/**
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, String> getRamUtilization(SnmpGet walk, String clusterId, int length) throws SnmpException{
		Map<String, String> map = new HashMap<String, String>();
		SnmpResult result = walk.snmpSynGet(new SnmpOID(ToAscii.getOid(PREFIX_TABLE_ID, RAM_UTILIZATION_OID, 
				String.valueOf(length), ToAscii.String2Ascii(clusterId))));
		if(result != null){
			map.put("ram_utilization", result.getValue().toString());
		}
		return map;
	}

}
