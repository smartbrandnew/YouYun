package com.broada.carrier.monitor.impl.mw.jboss.threadpool;

import java.io.IOException;
import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.jboss.JbossRemoteException;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class JbossThreadMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(JbossThreadMonitor.class);

	private static final String ITEMIDX_JBOSS_THREAD_QUEUE_SIZE = "JBOSS-THREAD-1";

	private static final String ITEMIDX_JBOSS_THREAD_MAX_QUEUE_SIZE = "JBOSS-THREAD-2";

	private static final String ITEMIDX_JBOSS_THREAD_MIN_POOLSIZE = "JBOSS-THREAD-3";

	private static final String ITEMIDX_JBOSS_THREAD_MAX_POOLSIZE = "JBOSS-THREAD-4";

	private static final String ITEMIDX_JBOSS_THREAD_POOL_NUMBER = "JBOSS-THREAD-5";

	private static final String ITEMIDX_JBOSS_ACTIVE_THREADS = "JBOSS-THREAD-6";

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		JbossJMXOption option = new JbossJMXOption(context.getMethod());
		if (!"4.x".equalsIgnoreCase(option.getVersion()) && !"5.x".equalsIgnoreCase(option.getVersion())) {
			throw new RuntimeException("该项监测不支持" + option.getVersion() + "版本");
		}
		ThreadInformation threadInfo = null;
		try {
			threadInfo = JbossThreadMonitorUtil.getThreadInfo(option);
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

		MonitorResultRow row = new MonitorResultRow(threadInfo.getName());
		row.setIndicator(ITEMIDX_JBOSS_THREAD_QUEUE_SIZE, threadInfo.getQueueSize());
		row.setIndicator(ITEMIDX_JBOSS_THREAD_MAX_QUEUE_SIZE, threadInfo.getMaximumQueueSize());
		row.setIndicator(ITEMIDX_JBOSS_THREAD_MIN_POOLSIZE, threadInfo.getMinimumPoolSize());
		row.setIndicator(ITEMIDX_JBOSS_THREAD_MAX_POOLSIZE, threadInfo.getMaximumPoolSize());
		row.setIndicator(ITEMIDX_JBOSS_THREAD_POOL_NUMBER, threadInfo.getPoolNumber());
		row.setIndicator(ITEMIDX_JBOSS_ACTIVE_THREADS, threadInfo.getActiveThreads());
		result.addRow(row);

		return result;
	}

}
