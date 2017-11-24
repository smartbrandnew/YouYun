package com.broada.carrier.monitor.impl.storage.oceanstor.filesystem;

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
import com.broada.utils.StringUtil;

public class FileSystemMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(FileSystemMonitor.class);
	
	public static final String PREFIX_TABLE_ID = ".1.3.6.1.4.1.2011.2.251.36.5.8.1.1";
	public static final String FILESYSTEM_NAME_INDEX = ".2";
	public static final String FILESYSTEM_CAPACITY_INDEX = ".6";
	public static final String FILESYSTEM_USED_INDEX = ".7";
	
	public static final String FILESYSTEM_UTILIZATION = "OCEANSTOR9000-FILESYSTEM-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		// 扩展 OceanStor9000的配置项
		String cluster_id = context.getMethod().getProperties().get("cluster_id", "");
		if(StringUtil.isNullOrBlank(cluster_id)){
			result.setResultDesc("通过SNMP方式未能查询到FileSystem利用率信息,缺少cluster_id的配置");
			return result;
		}
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		String[] arrays = cluster_id.split(",");
		if(arrays != null && arrays.length > 0)
			for(String clusterId:arrays){
				try {
					Map<String, Map<String, String>> map = getFileSystemInfo(walk);
					if(map.isEmpty()){
						result.setResultDesc("通过SNMP方式未能查询到FileSystem性能信息");
						return result;
					}
					for(String key:map.keySet()){
						MonitorResultRow row = new MonitorResultRow(key);
						row.setIndicator(FILESYSTEM_UTILIZATION + 1 ,map.get(key).get("filesystem.capacity"));
						row.setIndicator(FILESYSTEM_UTILIZATION + 2 ,map.get(key).get("filesystem.used"));
						row.addTag("cluster_id:" + clusterId);
						row.addTag("filesystem:" + key);        // 添加文件系统标签
						result.addRow(row);
					}
					result.setState(MonitorState.SUCCESSED);
				} catch (SnmpException e) {
					LOG.error("通过SNMP方式查询CPU利用率信息异常，" + e.getMessage());
					result.setResultDesc("通过SNMP方式查询CPU利用率信息异常");
				}
			}
		return result;
	}
	
	/**
	 * @param walk
	 * @return
	 * @throws SnmpException
	 */
	private Map<String, Map<String, String>> getFileSystemInfo(SnmpWalk walk) throws SnmpException{
		Map<String, Map<String, String>> ret = new HashMap<String, Map<String,String>>();
		SnmpResult[] results = walk.snmpWalk(PREFIX_TABLE_ID);
		Map<String, String> map = new HashMap<String, String>();
		if(results != null ){
			String slot = PREFIX_TABLE_ID.concat(FILESYSTEM_NAME_INDEX);
			for(SnmpResult sr:results){
				if(sr.getOid().toString().startsWith(slot)){
					map.put(sr.getOid().toString().substring(slot.length(), sr.getOid().toString().length()), sr.getValue().toString());
				}
			}
			if(!map.isEmpty()){
				for(String key:map.keySet()){
					Map<String, String> tmp = new HashMap<String, String>();
					for(SnmpResult sr:results){
						if(sr.getOid().toString().contains(key)){
							if(sr.getOid().toString().startsWith(PREFIX_TABLE_ID.concat(FILESYSTEM_CAPACITY_INDEX))){
								tmp.put("filesystem.capacity", sr.getValue().toString());
							} else if(sr.getOid().toString().startsWith(PREFIX_TABLE_ID.concat(FILESYSTEM_USED_INDEX))){
								tmp.put("filesystem.used", sr.getValue().toString());
							} 
						}
					}
					ret.put(map.get(key), tmp);
				}
			}
		}
		return ret;
	}
}