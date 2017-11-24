package com.broada.carrier.monitor.impl.storage.huawei.oceanstor.partition;

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

public class PartitionMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(PartitionMonitor.class);
	
	public static final String OID = ".1.3.6.1.4.1.34774.4.1.21.4";
	public static final String PARTITION_NUM = "OCEANSTOR-PARTITION-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			Map<String, Integer> map = getPartitionNum(walk);
			if(map.isEmpty()){
				result.setResultDesc("通过SNMP方式未能查询到OCEANSTOR分区数");
				return result;
			}
			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(PARTITION_NUM + 1, map.get("partition_num") == null ? 0 : map.get("partition_num"));
			result.addRow(row);
			result.setState(MonitorState.SUCCESSED);
		} catch (SnmpException e) {
			LOG.error("通过SNMP方式查询OCEANSTOR分区数异常，" + e.getMessage());
			result.setResultDesc("通过SNMP方式查询OCEANSTOR分区数异常");
		}
				
		return result;
	}
	
	/**
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, Integer> getPartitionNum(SnmpWalk walk) throws SnmpException{
		Map<String, Integer> map = new HashMap<String, Integer>();
		SnmpResult[] results = walk.snmpWalk(OID);
		if(results != null && results.length > 0){
			// 分区数
			int num = 0;
			for (SnmpResult snmpResult : results) {
				// 获取IOD数组值
				int[] arr = snmpResult.getOid().getValue();
				if(arr[13] == 1){
					// 统计数量
					num++;
				}
			}
			map.put("partition_num", num);
		}
		return map;
	}
}
