package com.broada.carrier.monitor.impl.db.postgresql.tablespace;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.postgresql.PostgreSQLException;
import com.broada.carrier.monitor.method.postgresql.PostgreSQLMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PostgreSQLTableSpaceMonitor implements Monitor {
	private static final Log logger = LogFactory.getLog(PostgreSQLTableSpaceMonitor.class);
	private static final String ITEM_CODE = "POSTGRE-TABLESPACE-";

	private PostgreSQLTableSpace getTableSpace(List postgreList, String tsName) {
		PostgreSQLTableSpace postgreSQLTableSpace = null;
		if (postgreList == null || postgreList.size() <= 0 || tsName == null || tsName.equals("")) {
			return postgreSQLTableSpace;
		}
		for (Iterator iter = postgreList.iterator(); iter.hasNext(); ) {
			PostgreSQLTableSpace pts = (PostgreSQLTableSpace) iter.next();

			if (pts.getTsName().equals(tsName)) {
				return pts;
			}
		}
		return postgreSQLTableSpace;
	}

	@Override public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);// 没响应――0
		List<MonitorInstance> instancesList = new ArrayList(Arrays.asList(context.getInstances()));// 返回监测实体列表

		boolean state = true;
		PostgreSQLMonitorMethodOption option = new PostgreSQLMonitorMethodOption(context.getMethod());
		// 连接参数的获取
		String ip = context.getNode().getIp();
		int port = option.getPort();
		String sid = option.getDb();
		String user = option.getUsername();
		String pass = option.getPassword();

		PostgreSQLTableSpaceManager tsManager = new PostgreSQLTableSpaceManager(ip, sid, port, user, pass);
		long replyTime = 0;
		long time = System.currentTimeMillis();
		try {
			tsManager.initConnection();
		} catch (PostgreSQLException e) {
			if (e.getSQLState().equals(PostgreSQLException.ERRCODE_DWPRESU)) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc(e.getMessage());
				// 认为可以连接,所以有连接时间
				replyTime = System.currentTimeMillis() - time;
				if (replyTime <= 0) {
					replyTime = 1;
				}
				result.setResponseTime((int) replyTime);
			} else {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc(e.getMessage());
			}
			tsManager.close();
			return result;
		}

		StringBuffer msgSB = new StringBuffer();
		StringBuffer valSB = new StringBuffer();

		List<PostgreSQLTableSpace> tsList = null;
		try {
			tsList = tsManager.getTSInfo();// 当前监测结果
		} catch (Exception e) {
			String errMsg = "无法获取数据库表空间信息.\n postgresql错误号:" + e.getMessage();
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			return result;
		} finally {
			tsManager.close();
		}
		replyTime = System.currentTimeMillis() - time;
		if (replyTime <= 0) {
			replyTime = 1;
		}
		result.setResponseTime(replyTime);

		for (int i = 0; i < instancesList.size(); i++) {
			MonitorInstance instance = instancesList.get(i);
			String tsName = instance.getCode();// 实例的标识关键字
			PostgreSQLTableSpace pts = getTableSpace(tsList, tsName);// 对比(当前，旧值)查看表是否存在
			MonitorResultRow row = new MonitorResultRow(tsName);
			if (pts == null) {
				// 如果表空间不存在
				msgSB.append("表空间").append(tsName).append("不存在;\n");
				valSB.append("表空间").append(tsName).append("不存在.\n");
				state = false;
				row.setIndicator(ITEM_CODE + 1, tsName);
				continue;
			} else {
				row.setIndicator(ITEM_CODE + 1, pts.getTsName());
				row.setIndicator(ITEM_CODE + 2, pts.getTsSize());
			}
			result.addRow(row);
			// 加入表空间大小的监测
			double currentTSsize = BigDecimal.valueOf(pts.getTsSize()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			msgSB.append(tsName).append("表空间的大小").append(currentTSsize).append(";\n");
			valSB.append(tsName).append("表空间的大小").append(currentTSsize).append(";\n");
		}

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

	@Override public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		List<PostgreSQLTableSpace> tsList = null;
		PostgreSQLMonitorMethodOption option = new PostgreSQLMonitorMethodOption(context.getMethod());
		String ip = context.getNode().getIp();
		String database = option.getDb();
		int port = option.getPort();
		String user = option.getUsername();
		String pass = option.getPassword();
		PostgreSQLTableSpaceManager tsManager = new PostgreSQLTableSpaceManager(ip, database, port, user, pass);
		try {
			tsManager.initConnection();
			tsList = tsManager.getTableSpaceInfo();// 取得当前监测结果
		} catch (PostgreSQLException se) {
			throw new CollectException("无法获取PostgreSQL表空间信息,请检查配置信息!", se);
		} finally {
			tsManager.close();
		}

		for (PostgreSQLTableSpace space : tsList) {
			MonitorResultRow row = new MonitorResultRow(space.getTsName());
			row.setIndicator(ITEM_CODE + 1, space.getTsSize());
			result.addRow(row);
		}
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		return result;
	}
}
