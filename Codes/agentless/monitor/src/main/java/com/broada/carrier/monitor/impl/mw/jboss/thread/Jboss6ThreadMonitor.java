package com.broada.carrier.monitor.impl.mw.jboss.thread;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class Jboss6ThreadMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(Jboss6ThreadMonitor.class);

	private static final String ITEMIDX_JBOSS_THREAD_CORE_THREADS = "JBOSS6-THREAD-1";

	private static final String ITEMIDX_JBOSS_THREAD_MAX_THREADS = "JBOSS6-THREAD-2";

	private static final String ITEMIDX_JBOSS_THREAD_REJECTED_COUNT = "JBOSS6-THREAD-3";

	private static final String ITEMIDX_JBOSS_CURRENT_THREAD_COUNT = "JBOSS6-THREAD-4";

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		JbossJMXOption option = new JbossJMXOption(context.getMethod());
		if (!"6.x".equalsIgnoreCase(option.getVersion())) {
			throw new RuntimeException("该项监测不支持" + option.getVersion() + "版本");
		}
		Jboss6ThreadInformation info = null;
		try {
			info = Jboss6ThreadMonitorUtil.getJboss6ThreadInfo(option);
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
		}
		MonitorResultRow row = new MonitorResultRow(info.getName());
		row.setIndicator(ITEMIDX_JBOSS_THREAD_CORE_THREADS, info.getCoreThreads());
		row.setIndicator(ITEMIDX_JBOSS_THREAD_MAX_THREADS, info.getMaxThreads());
		row.setIndicator(ITEMIDX_JBOSS_THREAD_REJECTED_COUNT, info.getRejectedCount());
		row.setIndicator(ITEMIDX_JBOSS_CURRENT_THREAD_COUNT, info.getCurrentThreadCount());
		result.addRow(row);

		return result;
	}
}