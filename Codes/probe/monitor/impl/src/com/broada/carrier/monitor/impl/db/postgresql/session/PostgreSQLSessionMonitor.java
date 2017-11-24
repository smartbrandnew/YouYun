package com.broada.carrier.monitor.impl.db.postgresql.session;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.postgresql.PostgreSQLException;
import com.broada.carrier.monitor.method.postgresql.PostgreSQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.broada.carrier.monitor.impl.common.entity.MonitorConstant.MONITORSTATE_FAILING;

public class PostgreSQLSessionMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(PostgreSQLSessionMonitor.class);
	private static final String[] COLUMNS = { "用户名", "IP地址", "端口", "登录时间" };
	private static final String ITEM_CODE = "POSTGRE-SESSION-";

	@Override public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);// 没响应——0

		boolean state = true;
		PostgreSQLMonitorMethodOption option = new PostgreSQLMonitorMethodOption(context.getMethod());
		// 连接参数的获取
		String ip = context.getNode().getIp();
		int port = option.getPort();
		String sid = option.getDb();
		String user = option.getUsername();
		String pass = option.getPassword();

		PostgreSQLSessionManager sessionManager = new PostgreSQLSessionManager(ip, sid, port, user, pass);
		long replyTime = 0;
		long time = System.currentTimeMillis();
		try {
			sessionManager.initConnection();
		} catch (PostgreSQLException e) {
			if (e.getSQLState().equals(PostgreSQLException.ERRCODE_USERPD)) {
				result.setState(MONITORSTATE_FAILING);
				result.setResultDesc(e.getMessage());
				// 认为可以连接,所以有连接时间
				replyTime = System.currentTimeMillis() - time;
				if (replyTime <= 0) {
					replyTime = 1;
				}
				result.setResponseTime((int) replyTime);
			} else {
				result.setState(MONITORSTATE_FAILING);
				result.setResultDesc(e.getMessage());
			}
			sessionManager.close();
			return result;
		}

		StringBuffer msgSB = new StringBuffer();
		StringBuffer valSB = new StringBuffer();

		List<PostgreSQLSession> sessionList = null;
		try {
			sessionList = sessionManager.getSessionInfo();
		} catch (Exception e) {
			String errMsg = "无法获取数据库表空间信息.\n postgresql错误号:" + e.getMessage();
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			return result;
		} finally {
			sessionManager.close();
		}
		replyTime = System.currentTimeMillis() - time;
		if (replyTime <= 0) {
			replyTime = 1;
		}
		result.setResponseTime(replyTime);

		int length = COLUMNS.length * sessionList.size();

		for (int i = 0, wontedCount = 0; i < length; i = i + COLUMNS.length, wontedCount++) {
			PostgreSQLSession ps = sessionList.get(wontedCount);
			MonitorResultRow row = new MonitorResultRow(ps.getProcpid());
			row.setIndicator(ITEM_CODE + 1, ps.getUsename());
			row.setIndicator(ITEM_CODE + 2, ps.getClient_addr());
			row.setIndicator(ITEM_CODE + 3, ps.getClient_port());
			row.setIndicator(ITEM_CODE + 4, ps.getBackend_start());
			result.addRow(row);
		}

		// 计算当前登录用户数和连接数
		ArrayList<String> userList = new ArrayList<String>();
		for (Iterator<PostgreSQLSession> iter = sessionList.iterator(); iter.hasNext(); ) {
			PostgreSQLSession ps = iter.next();
			String username = ps.getUsename();
			if (username == null) {
				userList.add(username);
				continue;
			}
			if (!userList.contains(username)) {
				userList.add(username);
			}
		}

		int currentUserCount = userList.size();
		int currentConnCount = sessionList.size();

		// 加入表是否存在的监测
				msgSB.append("用户登录数" + currentUserCount + "\n");

		// 加入用户登录数的监测
				msgSB.append("当前连接数" + currentConnCount + ";\n");

		// 最后的result.setResultDesc、result.setCurrentVal
		if (!state) {
			result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
			msgSB.insert(0, "");
			valSB.insert(0, "");
			result.setResultDesc(msgSB.toString());
		} else {
			result.setState(MonitorConstant.MONITORSTATE_NICER);
		}
		return result;
	}

}
