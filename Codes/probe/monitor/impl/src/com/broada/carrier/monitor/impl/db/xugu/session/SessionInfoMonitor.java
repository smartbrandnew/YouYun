package com.broada.carrier.monitor.impl.db.xugu.session;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.xugu.XuguManager;
import com.broada.carrier.monitor.method.xugu.XuguMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class SessionInfoMonitor extends BaseMonitor{
	
	private static Logger LOG = LoggerFactory.getLogger(SessionInfoMonitor.class);
	
	private final String ITEM_CODE = "XUGU-SESSION-";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorState.UNMONITOR);

		XuguMonitorMethodOption option = new XuguMonitorMethodOption(context.getMethod());
		String ip = context.getNode().getIp();
		int port = option.getPort();
		String database = option.getDatabaseName();
		String username = option.getUserName();
		String password = option.getPassword();
		int conType = option.getConType();
		String ips = option.getIps();
		List<SessionInfo> infos = null;
		long replyTime = System.currentTimeMillis();
		XuguManager manager = null;
		try {
			manager = new XuguManager(ip, port, database, username, password, conType, ips);
		} catch (Exception e) {
			LOG.error("xugu.yaml文件con_type和ips配置项错误," + e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("xugu.yaml文件con_type和ips配置项错误");
			return result;
		}
		try {
			infos = manager.queryDatabaseSessionInfo();
		} catch (Exception e) {
			LOG.error("虚谷数据库会话信息查询失败" + e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("虚谷数据库会话信息查询失败");
			return result;
		}
		replyTime = System.currentTimeMillis() - replyTime;
		if (replyTime <= 0)
			replyTime = 1L;
		result.setResponseTime(replyTime);
		if (infos != null) {
			for(SessionInfo sf:infos){
				MonitorResultRow row = new MonitorResultRow();
				row.setInstCode(String.valueOf(sf.getNode_id()));
				row.addTag("node_id:" + sf.getNode_id());
				row.addTag("user_name:" + sf.getUser_name());
				row.setIndicator(ITEM_CODE + 1, sf.getLast_time());
				result.addRow(row);
			}
		}
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		return result;
	}

}
