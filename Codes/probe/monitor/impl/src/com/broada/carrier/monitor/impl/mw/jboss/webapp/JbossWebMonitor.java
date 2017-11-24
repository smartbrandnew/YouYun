package com.broada.carrier.monitor.impl.mw.jboss.webapp;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.jboss.JbossRemoteException;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class JbossWebMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(JbossWebMonitor.class);

	private static final String ITEMIDX_JBOSS_WEB_SESSION_MAX_ALIVETIME = "JBOSS-WEB-1";

	private static final String ITEMIDX_JBOSS_WEB_ACTIVE_SESSIONS = "JBOSS-WEB-2";

	private static final String ITEMIDX_JBOSS_WEB_AVG_ALIVETIME = "JBOSS-WEB-3";

	private static final String ITEMIDX_JBOSS_WEB_MAX_ACTIVE = "JBOSS-WEB-4";

	private static final String ITEMIDX_JBOSS_WEB_EXPIRED_SESSIONS = "JBOSS-WEB-5";

	private static final String ITEMIDX_JBOSS_WEB_REJECTED_SESSIONS = "JBOSS-WEB-6";

	private static final String ITEMIDX_JBOSS_WEB_SESSION_COUNTER = "JBOSS-WEB-7";

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		JbossJMXOption option = new JbossJMXOption(context.getMethod());
		WebInformation webInfo = null;
		if ("4.x".equalsIgnoreCase(option.getVersion()) || "5.x".equalsIgnoreCase(option.getVersion())) {
			try {
				webInfo = JbossWebMonitorUtil.getWebInfo(option);
			} catch (IOException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("获取基本信息数据失败", e);
				}
				result.setResultDesc("获取基本信息数据失败,可能是JBOSS服务没有启动或者网络无法连接.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			} catch (SAXException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("数据解析错误.", e);
				}
				result.setResultDesc("数据获取成功,但解析时发生错误.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			} catch (JbossRemoteException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("数据解析错误:" + e.getMessage(), e);
				}
				result.setResultDesc("数据获取成功,但解析时发生错误:" + e.getMessage());
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("获取数据时发生未知错误:" + e.getMessage(), e);
				}
				result.setResultDesc("获取数据时发生未知错误:" + e.getMessage());
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			}
		} else if ("6.x".equalsIgnoreCase(option.getVersion())) {
			try {
				webInfo = JbossWebMonitorUtil.getJboss6WebInfo(option);
			} catch (ConnectTimeoutException e) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc("连接目标地址" + option.getIpAddr() + "超时.");
				if (logger.isDebugEnabled()) {
					logger.debug("连接目标地址" + option.getIpAddr() + "超时.", e);
				}
				return result;
			} catch (ConnectException e) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc("无法连接到" + option.getIpAddr() + "的" + option.getPort() + "端口.");
				if (logger.isDebugEnabled()) {
					logger.debug("无法连接到" + option.getIpAddr() + "的" + option.getPort() + "端口.", e);
				}
				return result;
			} catch (SocketTimeoutException e) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc("成功连接端口:" + option.getPort() + ",但读取数据超时或HTTP协议错误.");
				if (logger.isDebugEnabled()) {
					logger.debug("成功连接端口:" + option.getPort() + ",但读取数据超时或HTTP协议错误.", e);
				}
				return result;
			} catch (IOException e) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc("IO错误:" + e.getMessage());
				if (logger.isDebugEnabled()) {
					logger.debug("IO错误:", e);
				}
				return result;
			} catch (Throwable t) {
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				result.setResultDesc("未知错误:" + t.getMessage());
				if (logger.isDebugEnabled()) {
					logger.debug("未知错误:", t);
				}
				return result;
			}
		} else {
			throw new RuntimeException("该项监测不支持" + option.getVersion() + "版本");
		}

		MonitorResultRow row = new MonitorResultRow(webInfo.getName());
		row.setIndicator(ITEMIDX_JBOSS_WEB_SESSION_MAX_ALIVETIME,
				webInfo.getSessionMaxAliveTime());
		row.setIndicator(ITEMIDX_JBOSS_WEB_ACTIVE_SESSIONS, webInfo.getActiveSessions());
		row.setIndicator(ITEMIDX_JBOSS_WEB_AVG_ALIVETIME,
				webInfo.getSessionAverageAliveTime());
		row.setIndicator(ITEMIDX_JBOSS_WEB_MAX_ACTIVE, webInfo.getMaxActive());
		row.setIndicator(ITEMIDX_JBOSS_WEB_EXPIRED_SESSIONS, webInfo.getExpiredSessions());
		row.setIndicator(ITEMIDX_JBOSS_WEB_REJECTED_SESSIONS, webInfo.getRejectedSessions());
		row.setIndicator(ITEMIDX_JBOSS_WEB_SESSION_COUNTER, webInfo.getSessionCounter());
		result.addRow(row);
		result.setState(MonitorState.SUCCESSED);
		return result;
	}
}
