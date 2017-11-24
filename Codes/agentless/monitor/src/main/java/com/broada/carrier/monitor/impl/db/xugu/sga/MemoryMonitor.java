package com.broada.carrier.monitor.impl.db.xugu.sga;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.xugu.XuguMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.cid.action.api.entity.Protocol;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocol;
import com.broada.cid.action.protocol.impl.ccli.CcliSession;

public class MemoryMonitor extends BaseMonitor{
	
	private static Logger LOG = LoggerFactory.getLogger(MemoryMonitor.class);
	
	private final String ITEM_CODE = "XUGU-SGA-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorState.UNMONITOR);
		XuguMonitorMethodOption option = new XuguMonitorMethodOption(context.getMethod());
		String ip = context.getNode().getIp();
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("sessionName", option.getSessionName());
		props.put("remotePort", option.getRemotePort());
		props.put("loginTimeout", option.getLoginTimeout());
		props.put("prompt", option.getPromt());
		props.put("username", option.getUsernameForCli());
		props.put("password", option.getPasswordForCli());
		props.put("sysname", option.getSysname());
		props.put("waitTimeout", option.getWaitTimeout());
		CcliProtocol protocol = new CcliProtocol(new Protocol("ccli", props));
		protocol.setField("ip", ip);
		CcliSession cli = new CcliSession(protocol);
		long replyTime = System.currentTimeMillis();
		String stat = "";
		try{
			cli.connect();
			stat = cli.execute("free");
			cli.disconnect();
		}catch (Exception e) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("查询虚谷数据库所在主机内存信息失败");
			LOG.error("查询虚谷数据库所在主机内存信息失败," + e.getMessage());
			return result;
		}
		replyTime = System.currentTimeMillis() - replyTime;
		if (replyTime <= 0)
			replyTime = 1L;
		result.setResponseTime(replyTime);
		Map<String, List<Double>> map = formatResult(stat);
		if(!map.isEmpty()){
			// 此处利用默认的ip标签作为区分
			MonitorResultRow row = new MonitorResultRow("xugu-sga");
			for(String key : map.keySet()){
				double total = map.get(key).get(0);
				double used = map.get(key).get(1);
				double free = map.get(key).get(2);
				double used_percent = (used / total) * 100;
				if(key.equalsIgnoreCase("Mem")){
					row.setIndicator(ITEM_CODE + 1, (long)(total/1024));
					row.setIndicator(ITEM_CODE + 2, (long)(used/1024));
					row.setIndicator(ITEM_CODE + 3, (long)(free/1024));
					row.setIndicator(ITEM_CODE + 4, new BigDecimal(used_percent).setScale(2, RoundingMode.HALF_EVEN));
				}else if(key.equalsIgnoreCase("Swap")){
					row.setIndicator(ITEM_CODE + 5, (long)(total/1024));
					row.setIndicator(ITEM_CODE + 6, (long)(used/1024));
					row.setIndicator(ITEM_CODE + 7, (long)(free/1024));
					row.setIndicator(ITEM_CODE + 8, new BigDecimal(used_percent).setScale(2, RoundingMode.HALF_EVEN));
				}
			}
			result.addRow(row);
			result.setState(MonitorConstant.MONITORSTATE_NICER);
		}
		return result;
	}
	
	/**
	 * 将返回的值进行排列
	 * @param stat
	 * @return
	 */
	private Map<String, List<Double>> formatResult(String stat){
		Map<String, List<Double>> map = new HashMap<String, List<Double>>();
		String[] lines = stat.split("\n");
		for(int i=0; i<lines.length; i++){
			List<Double> tmp = new ArrayList<Double>();
			lines[i] = lines[i].replace("\r", "");
			if(lines[i].startsWith("Mem:")){
				String[] line = lines[i].split(" ");
				for(String key:line)
					if(!"".equals(key) && !"Mem:".equals(key))
						tmp.add(Double.valueOf(key));
				map.put("Mem", tmp);
			} else if(lines[i].startsWith("Swap:")){
				String[] line = lines[i].split(" ");
				for(String key:line)
					if(!"".equals(key) && !"Swap:".equals(key))
						tmp.add(Double.valueOf(key));
				map.put("Swap", tmp);
			}
		}
		return map;
	}
	
}
