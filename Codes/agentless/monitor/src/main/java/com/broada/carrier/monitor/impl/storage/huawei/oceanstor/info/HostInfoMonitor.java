package com.broada.carrier.monitor.impl.storage.huawei.oceanstor.info;

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

// 连接主机信息
public class HostInfoMonitor extends BaseMonitor{
	private static final Log LOG = LogFactory.getLog(HostInfoMonitor.class);
	
	public static final String TABLE_OID = ".1.3.6.1.4.1.34774.4.1.23.4.5";
	public static final String COMMON_OID = ".1.";
	public static final String HOST_NAME_OID = "2";
	public static final String HOST_LOCATION_OID = "3";
	public static final String OPERATING_SYSTEM_OID = "6";
	public static final String IP_ADDRESS_OID = "7";
	public static final String NETWORK_NAME_OID = "8";
	public static final String HOST_MODEL_OID = "9";
	public static final String HOST = "OCEANSTOR-HOST-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			// 获取多个主机信息
			Map<String, Map<String, String>> maps = getHostInfos(walk);
			if(maps.isEmpty()){
				result.setResultDesc("通过SNMP方式未能查询到OCEANSTOR-HOST主机信息");
				return result;
			}
			for (Map.Entry<String, Map<String, String>> entry : maps.entrySet()) {  
				Map<String, String> map = entry.getValue();
				MonitorResultRow row = new MonitorResultRow(entry.getKey());
				row.setIndicator(HOST + 1, map.get("host_name"));
				row.setIndicator(HOST + 2, map.get("host_location"));
				row.setIndicator(HOST + 3, map.get("operating_system"));
				row.setIndicator(HOST + 4, map.get("ip_address"));
				row.setIndicator(HOST + 5, map.get("network_name"));
				row.setIndicator(HOST + 6, map.get("host_model"));
				row.addTag("host_" + entry.getKey());
				result.addRow(row);
			}  
			result.setState(MonitorState.SUCCESSED);
		} catch (SnmpException e) {
			LOG.error("通过SNMP方式查询OCEANSTOR-HOST主机信息异常，" + e.getMessage());
			result.setResultDesc("通过SNMP方式查询OCEANSTOR-HOST主机信息异常");
		}
		return result;
	}
	
	/**
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, Map<String, String>> getHostInfos(SnmpWalk walk) throws SnmpException{
		Map<String, Map<String, String>> maps = new HashMap<String,Map<String, String>>();
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
				if((TABLE_OID + COMMON_OID + HOST_NAME_OID + commonOid).equals(oid)){
					// 主机名称
					map.put("host_name",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + HOST_LOCATION_OID + commonOid).equals(oid)){
					// 主机位置
					map.put("host_location",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + OPERATING_SYSTEM_OID + commonOid).equals(oid)){
					// 主机操作系统
					String operatingSystem = OperatingSystem.parseFromLable(Integer.parseInt(snmpResult.getValue().toString())).getValue();
					map.put("operating_system",operatingSystem);
				}else if((TABLE_OID + COMMON_OID + IP_ADDRESS_OID + commonOid).equals(oid)){
					// 主机IP地址
					map.put("ip_address",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + NETWORK_NAME_OID + commonOid).equals(oid)){
					// 主机域名
					map.put("network_name",snmpResult.getValue().toString());
				}else if((TABLE_OID + COMMON_OID + HOST_MODEL_OID + commonOid).equals(oid)){
					// 主机型号
					map.put("host_model",snmpResult.getValue().toString());
				}
				if(map != null && !map.isEmpty()){
					maps.put(node, map);
				}
			}
		}
		return maps;
	}
}
