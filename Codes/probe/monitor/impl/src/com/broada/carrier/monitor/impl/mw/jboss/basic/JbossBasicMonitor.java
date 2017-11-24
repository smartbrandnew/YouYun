package com.broada.carrier.monitor.impl.mw.jboss.basic;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.jboss.JbossRemoteException;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class JbossBasicMonitor extends BaseMonitor {

	private static final Log logger = LogFactory.getLog(JbossBasicMonitor.class);

	private static final String ITEMIDX_JBOSS_BASIC_JAVAVENDOR = "JBOSS-BASIC-1";

	private static final String ITEMIDX_JBOSS_BASIC_JAVAVERSION = "JBOSS-BASIC-2";

	private static final String ITEMIDX_JBOSS_BASIC_OSNAME = "JBOSS-BASIC-3";

	private static final String ITEMIDX_JBOSS_BASIC_VERSION = "JBOSS-BASIC-4";

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		JbossJMXOption option = new JbossJMXOption(context.getMethod());
		ServerInformation serverInfo = null;
		if ("4.x".equalsIgnoreCase(option.getVersion()) || "5.x".equalsIgnoreCase(option.getVersion())) {
			try {
				serverInfo = JbossBasicMonitorUtil.getServerInfo(option);
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
				serverInfo = JbossBasicMonitorUtil.getJboss6ServerInfo(option);
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
			} catch (JDOMException e) {
				if (logger.isDebugEnabled()) {
					logger.debug("数据解析错误.", e);
				}
				result.setResultDesc("数据获取成功,但解析时发生错误.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			}

		} else if ("7.x".equalsIgnoreCase(option.getVersion())) {
			try {
				serverInfo = JbossBasicMonitorUtil.getJboss7ServerInfo(option);
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
		} else if ("6.x-eap".equalsIgnoreCase(option.getVersion())) {
			try {
				serverInfo = JbossBasicMonitorUtil.getJboss6eapServerInfo(option);
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
		}
		PerfResult javaVendor = new PerfResult(ITEMIDX_JBOSS_BASIC_JAVAVENDOR, serverInfo.getJavaVendor());
		PerfResult javaVersion = new PerfResult(ITEMIDX_JBOSS_BASIC_JAVAVERSION, serverInfo.getJavaVersion());
		PerfResult osName = new PerfResult(ITEMIDX_JBOSS_BASIC_OSNAME, serverInfo.getoSName());
		PerfResult version = new PerfResult(ITEMIDX_JBOSS_BASIC_VERSION, serverInfo.getVersion());

		result.addPerfResult(version);
		result.addPerfResult(osName);
		result.addPerfResult(javaVersion);
		result.addPerfResult(javaVendor);
		if ("running".equalsIgnoreCase(serverInfo.getState()))
			result.setState(MonitorState.SUCCESSED);
		else
			result.setState(MonitorState.FAILED);
		return result;
	}
}