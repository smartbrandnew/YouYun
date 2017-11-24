package com.broada.carrier.monitor.impl.mw.jboss.jvm;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class Jboss7PmiMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(Jboss7PmiMonitor.class);
	private static final String ITEMIDX_JBOSS7_PMI_MAX_HEAP_MEMORY = "JBOSS7-JVM-1";

	private static final String ITEMIDX_JBOSS7_PMI_USED_HEAP_MEMORY = "JBOSS7-JVM-2";

	private static final String ITEMIDX_JBOSS7_COMMITTED_HEAP_MEMORY = "JBOSS7-JVM-3";

	private static final String ITEMIDX_JBOSS7_INIT_HEAP_MEMORY = "JBOSS7-JVM-4";

	private static final String ITEMIDX_JBOSS7_PMI_MAX_NONHEAP_MEMORY = "JBOSS7-JVM-5";

	private static final String ITEMIDX_JBOSS7_PMI_USED_NONHEAP_MEMORY = "JBOSS7-JVM-6";

	private static final String ITEMIDX_JBOSS7_COMMITTED_NONHEAP_MEMORY = "JBOSS7-JVM-7";

	private static final String ITEMIDX_JBOSS7_INIT_NONHEAP_MEMORY = "JBOSS7-JVM-8";

	private static final String ITEMIDX_JBOSS7_THREAD_COUNT = "JBOSS7-JVM-9";

	private static final String ITEMIDX_JBOSS7_DAEMON_THREAD_COUNT = "JBOSS7-JVM-10";

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		JbossJMXOption option = new JbossJMXOption(context.getMethod());
		if (!"7.x".equalsIgnoreCase(option.getVersion())&& !"6.x-eap".equalsIgnoreCase(option.getVersion())) {
			throw new RuntimeException("该项监测不支持" + option.getVersion() + "版本");
		}
		PmiJboss7Information info = null;
		try {
			info = Jboss7PmiMonitorUtil.getJboss7PmiInfo(option);
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
		} catch (JSONException e) {
			logger.debug("json错误:", e);
		} catch (Throwable t) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("未知错误:" + t.getMessage());
			if (logger.isDebugEnabled()) {
				logger.debug("未知错误:", t);
			}
			return result;
		}

		PerfResult maxHeapMemory = new PerfResult(ITEMIDX_JBOSS7_PMI_MAX_HEAP_MEMORY, info.getMaxHeapMemory());
		PerfResult usedHeapMemory = new PerfResult(ITEMIDX_JBOSS7_PMI_USED_HEAP_MEMORY, info.getUsedHeapMemory());
		PerfResult committedHeapMemory = new PerfResult(ITEMIDX_JBOSS7_COMMITTED_HEAP_MEMORY, info.getCommittedHeapMemory());
		PerfResult initHeapMemory = new PerfResult(ITEMIDX_JBOSS7_INIT_HEAP_MEMORY, info.getInitHeapMemory());
		PerfResult maxNonHeapMeomory = new PerfResult(ITEMIDX_JBOSS7_PMI_MAX_NONHEAP_MEMORY, info.getMaxNonHeapMeomory());
		PerfResult usedNonHeapMemory = new PerfResult(ITEMIDX_JBOSS7_PMI_USED_NONHEAP_MEMORY, info.getUsedNonHeapMemory());
		PerfResult committedNonHeapMemory = new PerfResult(ITEMIDX_JBOSS7_COMMITTED_NONHEAP_MEMORY,
				info.getCommittedNonHeapMemory());
		PerfResult initNonHeapMemory = new PerfResult(ITEMIDX_JBOSS7_INIT_NONHEAP_MEMORY, info.getInitNonHeapMemory());
		PerfResult threadCount = new PerfResult(ITEMIDX_JBOSS7_THREAD_COUNT, info.getThreadCount());
		PerfResult daemonThreadCount = new PerfResult(ITEMIDX_JBOSS7_DAEMON_THREAD_COUNT, info.getDaemonThreadCount());
		result.addPerfResult(initNonHeapMemory);
		result.addPerfResult(committedNonHeapMemory);
		result.addPerfResult(usedNonHeapMemory);
		result.addPerfResult(maxNonHeapMeomory);
		result.addPerfResult(initHeapMemory);
		result.addPerfResult(committedHeapMemory);
		result.addPerfResult(usedHeapMemory);
		result.addPerfResult(maxHeapMemory);
		result.addPerfResult(threadCount);
		result.addPerfResult(daemonThreadCount);
		return result;
	}

}
