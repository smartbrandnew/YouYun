package com.broada.carrier.monitor.impl.storage.oceanstor.memory;

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

public class MemoryMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(CPUMonitor.class);
	public static final String PREFIX_TABLE_ID = ".1.3.6.1.4.1.2011.2.251.36.5.1.1.1";
	public static final String CLUSTER_NAME_OID = "2";
	public static final String CLUSTER_MEMO_TOTAL_OID = "8";
	public static final String CLUSTER_MEMO_USED_OID = "9";
	public static final String MEMO_UTILIZATION = "OCEANSTOR9000-MEMO-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		// 扩展 OceanStor9000的配置项
		String cluster_id = context.getMethod().getProperties().get("cluster_id", "");
		if(StringUtil.isNullOrBlank(cluster_id)){
			result.setResultDesc("通过SNMP方式未能查询到存储容量信息,缺少cluster_id的配置");
			return result;
		}
		SnmpGet walk = new SnmpGet(snmpMethod.getTarget(context.getNode().getIp()));
		String[] arrays = cluster_id.split(",");
		if(arrays != null && arrays.length > 0)
			for(String clusterId:arrays){
				try {
					Map<String, String> map = getMemoUtilization(walk, clusterId, clusterId.length());
					if(map.isEmpty()){
						result.setResultDesc("通过SNMP方式未能查询到存储容量信息");
						return result;
					}
					MonitorResultRow row = new MonitorResultRow(clusterId);
					String cluster_name = map.get("cluster_name");
					double total = Double.valueOf(map.get("total_size") == null? "0":map.get("total_size"));
					double used = Double.valueOf(map.get("used_size") == null? "0":map.get("used_size"));
					if(total == 0d)
						continue;   // 取不到则直接跳过
					row.setIndicator(MEMO_UTILIZATION + 1, new BigDecimal(total*1.0).setScale(2, RoundingMode.HALF_UP));
					row.setIndicator(MEMO_UTILIZATION + 2, new BigDecimal(used/total*1.0).setScale(2, RoundingMode.HALF_UP));
					row.addTag("cluster:" + cluster_name);
					row.addTag("cluster_id:" + clusterId);
					result.addRow(row);
					result.setState(MonitorState.SUCCESSED);
				} catch (SnmpException e) {
					LOG.error("通过SNMP方式查询存储容量信息异常，" + e.getMessage());
					result.setResultDesc("通过SNMP方式查询存储容量信息异常");
				}
			}
		return result;
	}
	
	/**
	 * 获取容量信息
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, String> getMemoUtilization(SnmpGet walk, String cluster_id, int length) throws SnmpException{
		// 集群编号与存储容量及其使用量
		Map<String, String> map = new HashMap<String, String>();
		SnmpResult[] results = walk.snmpSynGet(new SnmpOID[]{new SnmpOID(ToAscii.getOid(PREFIX_TABLE_ID, CLUSTER_MEMO_TOTAL_OID, String.valueOf(length), ToAscii.String2Ascii(cluster_id))), 
				new SnmpOID(ToAscii.getOid(PREFIX_TABLE_ID, CLUSTER_MEMO_USED_OID, String.valueOf(length), ToAscii.String2Ascii(cluster_id))),
				new SnmpOID(ToAscii.getOid(PREFIX_TABLE_ID, CLUSTER_NAME_OID, String.valueOf(length), ToAscii.String2Ascii(cluster_id)))});
		if(results != null && results.length > 0){
			for(SnmpResult sr:results){
				if(sr.getOid().toString().equals(ToAscii.getOid(PREFIX_TABLE_ID, CLUSTER_NAME_OID, String.valueOf(length), ToAscii.String2Ascii(cluster_id))))
					map.put("cluster_name", sr.getValue().toString());
				if(sr.getOid().toString().equals(ToAscii.getOid(PREFIX_TABLE_ID, CLUSTER_MEMO_TOTAL_OID, String.valueOf(length), ToAscii.String2Ascii(cluster_id))))
					map.put("total_size", sr.getValue().toString());
				if(sr.getOid().toString().equals(ToAscii.getOid(PREFIX_TABLE_ID, CLUSTER_MEMO_USED_OID, String.valueOf(length), ToAscii.String2Ascii(cluster_id))))
					map.put("used_size", sr.getValue().toString());
			}
		}
		return map;
	}

}
