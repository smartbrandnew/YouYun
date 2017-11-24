package com.broada.carrier.monitor.impl.storage.huawei.oceanstor.raid;

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

// 存储池信息
public class RAIDMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(RAIDMonitor.class);
	
	public static final String TABLE_OID = ".1.3.6.1.4.1.34774.4.1.23.4.2";
	public static final String COMMON_OID = ".1.";
	
	public static final String TOTAL_CAPACITY_OID = "7";
	public static final String SUBSCRIBED_CAPACITY_OID = "8";
	public static final String FREE_CAPACITY_OID = "9";
	public static final String PROTECTION_CAPACITY_OID = "10";
	public static final String TIER0_CAPACITY_OID = "11";
	public static final String TIER1_CAPACITY_OID = "12";
	public static final String TIER2_CAPACITY_OID = "13";
	public static final String FULL_THRESHOLD_OID = "14";
	public static final String EXTENT_SIZE_OID = "15";
	public static final String ESTIMATED_MOVE_UP_DATA_OID = "20";
	public static final String ESTIMATED_MOVE_DOWN_DATA_OID = "21";
	public static final String ESTIMATED_DATA_RELOCATION_DURATION_OID = "22";
	public static final String RAID = "OCEANSTOR-RAID-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			Map<String, Map<String, String>> maps = getRaidInfo(walk);
			if(maps.isEmpty()){
				result.setResultDesc("通过SNMP方式未能查询到OCEANSTOR-RAID存储池信息");
				return result;
			}
			for (Map.Entry<String, Map<String, String>> entry : maps.entrySet()) { 
				Map<String, String> map = entry.getValue();
				MonitorResultRow row = new MonitorResultRow(entry.getKey());
				row.setIndicator(RAID + 1, new BigDecimal(map.get("total_capacity")).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(RAID + 2, new BigDecimal(map.get("subscribed_capacity")).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(RAID + 3, new BigDecimal(map.get("free_capacity")).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(RAID + 4, new BigDecimal(map.get("protection_capacity")).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(RAID + 5, new BigDecimal(map.get("tier0_capacity")).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(RAID + 6, new BigDecimal(map.get("tier1_capacity")).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(RAID + 7, new BigDecimal(map.get("tier2_capacity")).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(RAID + 8, new BigDecimal(Double.valueOf(map.get("full_threshold"))*1.0).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(RAID + 9, new BigDecimal(map.get("extent_size")).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(RAID + 10, new BigDecimal(map.get("estimated_move_up_data")).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(RAID + 11, new BigDecimal(map.get("estimated_move_down_data")).setScale(2, RoundingMode.HALF_UP));
				row.setIndicator(RAID + 12, new BigDecimal(map.get("estimated_data_relocation_duration")).setScale(2, RoundingMode.HALF_UP));
				row.addTag("raid_" + entry.getKey());
				result.addRow(row);
			}  
			result.setState(MonitorState.SUCCESSED);
		} catch (SnmpException e) {
			LOG.error("通过SNMP方式查询OCEANSTOR-RAID存储池信息异常，" + e.getMessage());
			result.setResultDesc("通过SNMP方式查询OCEANSTOR-RAID存储池信息异常");
		}
		return result;
	}
	
	/**
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, Map<String, String>> getRaidInfo(SnmpWalk walk) throws SnmpException{
		Map<String, Map<String, String>> maps = new HashMap<String, Map<String, String>>();
		SnmpResult[] results = walk.snmpWalk(TABLE_OID);
		if(results != null && results.length > 0){
			for (SnmpResult snmpResult : results) {
				String oid = snmpResult.getOid().toString();
				String node = oid.substring(oid.lastIndexOf(".")+1);
				Map<String, String> map = null;
				if(maps.get(node) == null){
					map = new HashMap<String, String>();
				}else {
					map = maps.get(node);
				}
				String commonOid = COMMON_OID + node;
				// todo 此地方jdk1.7之后的版本可以修改为 switch，目前版本不支持switch 比较字符串
				if((TABLE_OID + COMMON_OID + TOTAL_CAPACITY_OID + commonOid).equals(oid)){
					// 存储池总容量
					map.put("total_capacity",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + SUBSCRIBED_CAPACITY_OID + commonOid).equals(oid)){
					// 存储池已用容量
					map.put("subscribed_capacity",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + FREE_CAPACITY_OID + commonOid).equals(oid)){
					// 存储池可用容量
					map.put("free_capacity",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + PROTECTION_CAPACITY_OID + commonOid).equals(oid)){
					// 存储池数据保护容量
					map.put("protection_capacity",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + TIER0_CAPACITY_OID + commonOid).equals(oid)){
					// 存储池Tier0裸容量
					map.put("tier0_capacity",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + TIER1_CAPACITY_OID + commonOid).equals(oid)){
					// 存储池Tier1裸容量
					map.put("tier1_capacity",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + TIER2_CAPACITY_OID + commonOid).equals(oid)){
					// 存储池Tier2裸容量
					map.put("tier2_capacity",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + FULL_THRESHOLD_OID + commonOid).equals(oid)){
					// 存储池已用容量阈值
					map.put("full_threshold",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + EXTENT_SIZE_OID + commonOid).equals(oid)){
					// 存储池迁移粒度
					map.put("extent_size",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + ESTIMATED_MOVE_UP_DATA_OID + commonOid).equals(oid)){
					// 存储池待上迁数据量
					map.put("estimated_move_up_data",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + ESTIMATED_MOVE_DOWN_DATA_OID + commonOid).equals(oid)){
					// 存储池待下迁数据量
					map.put("estimated_move_down_data",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + ESTIMATED_DATA_RELOCATION_DURATION_OID + commonOid).equals(oid)){
					// 存储池迁移时间
					map.put("estimated_data_relocation_duration",snmpResult.getValue().toString());
				}
				if(map != null && !map.isEmpty()){
					maps.put(node, map);
				}
			}
		}
		return maps;
	}
}
