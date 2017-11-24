package com.broada.carrier.monitor.impl.mw.resin.webApp;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.resin.ResinMonitorManager;
import com.broada.carrier.monitor.impl.mw.resin.ResinMonitorUtil;
import com.broada.carrier.monitor.method.resin.ResinJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.monitor.agent.resin.server.entity.ResinWebApp;
import com.broada.utils.StringUtil;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ResinWebAppMonitor implements Monitor {

	public static final String INDEX_STATE = "RESIN-WEBAPP-1";

	public static final String INDEX_REQUEST_COUNT = "RESIN-WEBAPP-2";

	public static final String INDEX_SESSION_COUNT = "RESIN-WEBAPP-3";

	public static final String INDEX_STARTTIME = "RESIN-WEBAPP-4";

	public static final String INDEX_RESPONSE_TOTAL = "RESIN-WEBAPP-5";

	public static final String INDEX_RESPONSE_TIME = "RESIN-WEBAPP-6";

	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		result.setState(MonitorConstant.MONITORSTATE_NICER);

		List webApps = null;
		ResinJMXOption option = new ResinJMXOption(context.getMethod());
		String message = ResinMonitorUtil.testHostAndPort(context.getNode().getIp(), option);
		if (message != null) {
			result.setResultDesc(message);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		}

		String url = null; // Resin管理Url

		try {
			url = ResinMonitorUtil.getAgentUrl(context.getNode().getIp(), option);
		} catch (UnsupportedEncodingException e) {
			result.setResultDesc("Resin连接失败,代理名称不正确。");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		}
		try {
			ResinMonitorManager manager = new ResinMonitorManager();
			long replyTime = System.currentTimeMillis();
			webApps = manager.getFirstWebAppsByUrl(url, option.getWebAppRoot());
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			result.setResponseTime(replyTime);
		} catch (Exception e) {
			result.setResultDesc("Resin连接失败,Web应用根路径不正确。");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		}

		List<PerfResult> perfs = new ArrayList<PerfResult>();

		Iterator it = new ArrayList(Arrays.asList(context.getInstances())).iterator();
		while (it.hasNext()) {
			MonitorInstance instance = (MonitorInstance) it.next();
			//TODO 源代码为getWebAppByInsKey(webApps, instance.getInstanceKey()) 不清楚instanKey对应什么 暂时用getKey().getCode()
			ResinWebApp webApp = getWebAppByInsKey(webApps, instance.getCode());
			if (webApp == null) {
				//TODO 同上
				result.setResultDesc(
						(StringUtil.isNullOrBlank(result.getResultDesc()) ? "" : (result.getResultDesc() + ",")) + "连接池 "
								+ instance
										.getCode() + "不存在");
				result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
			} else {
				if (webApp.getSessionActiveCount() < 0) {
					webApp.setSessionActiveCount(0);
				}
				String path = (String) webApp.getContextPath();
				PerfResult prefState = new PerfResult(INDEX_STATE, webApp.getState());
				prefState.setInstanceKey(path);

				PerfResult prefRequestCount = new PerfResult(INDEX_REQUEST_COUNT, webApp.getRequestCount());
				prefRequestCount.setInstanceKey(path);

				PerfResult prefSessionCount = new PerfResult(INDEX_SESSION_COUNT, webApp.getSessionActiveCount());
				prefSessionCount.setInstanceKey(path);

				PerfResult prefStartTime = new PerfResult(INDEX_STARTTIME, webApp.getStartTime());
				prefStartTime.setInstanceKey(path);

				PerfResult prefResponseTotal = new PerfResult(INDEX_RESPONSE_TOTAL, webApp.getStatus500CountTotal());
				prefResponseTotal.setInstanceKey(path);

				PerfResult prefResponseTime = new PerfResult(INDEX_RESPONSE_TIME, webApp.getStatus500LastTime());
				prefResponseTime.setInstanceKey(path);

				perfs.add(prefState);
				perfs.add(prefRequestCount);
				perfs.add(prefSessionCount);
				perfs.add(prefStartTime);
				perfs.add(prefResponseTotal);
				perfs.add(prefResponseTime);
			}
		}

		result.setPerfResults((PerfResult[]) perfs.toArray(new PerfResult[perfs.size()]));
		if (result.getState() == MonitorConstant.MONITORSTATE_NICER) {
			result.setResultDesc("监测一切正常");
		}

		return result;
	}

	private ResinWebApp getWebAppByInsKey(List webApps, String key) {
		for (int index = 0; index < webApps.size(); index++) {
			ResinWebApp resinWebApp = (ResinWebApp) webApps.get(index);
			if (resinWebApp.getContextPath().equals(key)) {
				return resinWebApp;
			}
		}
		return null;
	}

	@Override
	public Serializable collect(CollectContext context) {
		List<ResinWebApp> webApps = null;
		ResinJMXOption option = new ResinJMXOption(context.getMethod());
		String message = ResinMonitorUtil.testHostAndPort(context.getNode().getIp(), option);
		if (message != null) {
			throw new CollectException(message);
		}

		String url = null; // Resin管理Url

		try {
			url = ResinMonitorUtil.getAgentUrl(context.getNode().getIp(), option);
		} catch (UnsupportedEncodingException e) {
			throw new CollectException("Resin连接失败,代理名称不正确。", e);
		}
		try {
			ResinMonitorManager manager = new ResinMonitorManager();
			webApps = manager.getFirstWebAppsByUrl(url, option.getWebAppRoot());
		} catch (Exception e) {
			throw new CollectException("Resin连接失败,Web应用根路径不正确。", e);
		}

		List<PerfResult> perfs = new ArrayList<PerfResult>();
		List<MonitorInstance> instances = new ArrayList<MonitorInstance>();

		for (ResinWebApp webApp : webApps) {
			String path = (String) webApp.getContextPath();
			if (webApp.getSessionActiveCount() < 0)
				webApp.setSessionActiveCount(0);
			PerfResult prefState = new PerfResult(INDEX_STATE, webApp.getState());
			prefState.setInstanceKey(path);

			PerfResult prefRequestCount = new PerfResult(INDEX_REQUEST_COUNT, webApp.getRequestCount());
			prefRequestCount.setInstanceKey(path);

			PerfResult prefSessionCount = new PerfResult(INDEX_SESSION_COUNT, webApp.getSessionActiveCount());
			prefSessionCount.setInstanceKey(path);

			PerfResult prefStartTime = new PerfResult(INDEX_STARTTIME, webApp.getStartTime());
			prefStartTime.setInstanceKey(path);

			PerfResult prefResponseTotal = new PerfResult(INDEX_RESPONSE_TOTAL, webApp.getStatus500CountTotal());
			prefResponseTotal.setInstanceKey(path);

			PerfResult prefResponseTime = new PerfResult(INDEX_RESPONSE_TIME, webApp.getStatus500LastTime());
			prefResponseTime.setInstanceKey(path);

			perfs.add(prefState);
			perfs.add(prefRequestCount);
			perfs.add(prefSessionCount);
			perfs.add(prefStartTime);
			perfs.add(prefResponseTotal);
			perfs.add(prefResponseTime);

			MonitorInstance mi = new MonitorInstance();
			mi.setInstanceKey(path);
			mi.setInstanceName(path);
			instances.add(mi);
		}
		MonitorResult result = new MonitorResult();
		result.setPerfResults(perfs.toArray(new PerfResult[perfs.size()]));
		return result;
	}
}
