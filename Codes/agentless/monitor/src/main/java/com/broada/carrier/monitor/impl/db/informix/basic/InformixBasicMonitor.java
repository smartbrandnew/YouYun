package com.broada.carrier.monitor.impl.db.informix.basic;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.informix.InformixManager;
import com.broada.carrier.monitor.method.informix.InformixMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.sql.SQLException;

/**
 * <p>
 * Title: InformixBasicMonitor
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Broada
 * </p>
 *
 * @author plx
 * @version 2.4
 */
public class InformixBasicMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(InformixBasicMonitor.class);
	public static final String ITEM_CONNECTS = "INFORMIX-BASIC-1";
	public static final String ITEM_READ_HIT = "INFORMIX-BASIC-2";
	public static final String ITEM_WRITE_HIT = "INFORMIX-BASIC-3";
	public static final String ITEM_LOCKS = "INFORMIX-BASIC-4";
	public static final String ITEM_ROLLBACKS = "INFORMIX-BASIC-5";

	@Override public Serializable collect(CollectContext context) {
		PerfResult connect = new PerfResult("INFORMIX-BASIC-1", false);
		MonitorResult result = new MonitorResult();
		result.setResponseTime(0);

		InformixMonitorMethodOption option = new InformixMonitorMethodOption(context.getMethod());
		String ip = context.getNode().getIp();
		int port = option.getPort();
		String dbName = option.getDbname();
		String serverName = option.getServername();
		String user = option.getUsername();
		String pass = option.getPassword();
		boolean state = true;
		boolean getFail = false;
		StringBuffer msgSB = new StringBuffer();
		StringBuffer valSB = new StringBuffer();

		InformixManager im = new InformixManager(ip, port, serverName, user, pass);
		long replyTime = System.currentTimeMillis();
		try {
			im.initConnection();
		} catch (SQLException ex) {
			boolean failing = false;
			String errorMsg = ex.getMessage();
			if ((isOtherError(errorMsg)) || (!isLsnOpened(errorMsg))) {
				failing = true;
			}

			im.close();

			if (failing) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc(mapErrorMsg(errorMsg) + ";\n");
				return result;
			}
			result.setState(MonitorConstant.MONITORSTATE_NICER);
			result.setResultDesc("监测一切正常");
			return result;
		}

		replyTime = System.currentTimeMillis() - replyTime;
		if (replyTime <= 0L) {
			replyTime = 1L;
		}
		result.setResponseTime((int) replyTime);
		MonitorResultRow row = new MonitorResultRow(dbName);
		try {
				try {
					int valConnect = im.getConnects();
					connect.setValue(valConnect);
					row.setIndicator(ITEM_CONNECTS, im.getConnects());
				} catch (SQLException ex) {
					logger.error("获取数据库链接数失败.", ex);
					msgSB.append("连接数获取失败;\n");
					valSB.append("连接数获取失败;");
					getFail = true;
				}
				try {
					row.setIndicator(ITEM_READ_HIT,im.getBufReadRatio());
					row.setIndicator(ITEM_WRITE_HIT,im.getBufWriteRatio());
					row.setIndicator(ITEM_LOCKS,im.getDeadLocks());
					row.setIndicator(ITEM_ROLLBACKS,im.getRollBacks());
					result.addRow(row);
				} catch (SQLException e) {
					logger.warn(String.format("获取指标失败。错误：%s", new Object[] { e }));
					logger.debug("堆栈：", e);
					msgSB.append("获取指标失败;\n");
					valSB.append("获取指标失败;");
					getFail = true;
				}
		} finally {
			im.close();
		}

		if (getFail) {
			result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
			result.setResultDesc(msgSB.toString());
		} else if (state) {
			result.setState(MonitorConstant.MONITORSTATE_NICER);
			result.setResultDesc("监测一切正常");
		} else {
			result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
			result.setResultDesc(msgSB.toString());
		}
		return result;
	}

	private boolean isLsnOpened(String errorMsg) {
		return (errorMsg.indexOf("无法与数据库建立连接") == -1) && (errorMsg.indexOf("无效的数据库服务名") == -1);
	}

	private boolean isOtherError(String errorMsg) {
		return (errorMsg.indexOf("未指定数据库") != -1) || (errorMsg.indexOf("未指定数据库用户") != -1) || (
				errorMsg.indexOf("无法获取Informix驱动") != -1) || (errorMsg.indexOf("无效的数据库服务名") != -1) || (
				errorMsg.indexOf("建立数据库链接失败") != -1);
	}

	private String mapErrorMsg(String errorMsg) {
		if (errorMsg.indexOf("无法与数据库建立连接") != -1)
			return "无法与数据库建立连接";
		if (errorMsg.indexOf("无效的数据库服务名") != -1)
			return "无效的数据库服务名";
		if (errorMsg.indexOf("用户名或密码不正确") != -1) {
			return "用户名或密码不正确";
		}
		return errorMsg.substring(0, errorMsg.length() - 1);
	}

}
