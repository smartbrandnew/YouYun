package com.broada.carrier.monitor.impl.mw.weblogic.agent.server;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.basic.WLSBasicMonitorUtil;
import com.broada.carrier.monitor.method.weblogic.agent.WebLogicJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * 
 * server性能的监测器
 * 
 * @author Yaojj Create By Mar 26, 2010 1:09:57 PM
 */
public class WLSServerMonitor extends BaseMonitor {

	private static final Log logger = LogFactory.getLog(WLSServerMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
			
		WebLogicJMXOption option = new WebLogicJMXOption(context.getMethod());
		
		List<ServerPerfInst> list = new ArrayList<ServerPerfInst>();
		try {
      long replyTime = System.currentTimeMillis();
			list = WLSServerMonitorUtil.getServerPerfInfomations(WLSBasicMonitorUtil
					.getBaseInfoUrl(option));
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
		} catch (UnsupportedEncodingException e) {
			logger.info(e);
			result.setResultDesc("无法将获取Server性能信息的URL用Base64方式编码。");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} catch (Exception e) {
			logger.info(e);
			if (e instanceof ConnectException) {
				result.setResultDesc("无法获取Server性能信息,可能网络不通或者目标主机上的weblogic没有启动.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			} else {
				result.setResultDesc("无法将获取Server性能信息的URL用Base64方式编码。");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			}
		} catch (Throwable e) {
			logger.info(e);
			result.setResultDesc("出现未知错误");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		}

		List<PerfResult> prs = new ArrayList<PerfResult>();
		ServerPerfInst serverInst = (ServerPerfInst) list.get(0);
		PerfResult name = new PerfResult("WLS-SERVER-1", true);
		PerfResult state = new PerfResult("WLS-SERVER-2", true);
		PerfResult runState = new PerfResult("WLS-SERVER-3", true);
		PerfResult ideThreadCount = new PerfResult("WLS-SERVER-4", true);
		PerfResult request = new PerfResult("WLS-SERVER-5", true);
		PerfResult memoryUsage = new PerfResult("WLS-SERVER-6", true);

		name.setStrValue(serverInst.getInstKey());
		state.setStrValue(serverInst.getState());
		runState.setStrValue(serverInst.getHealthState());
		ideThreadCount.setValue((double) serverInst.getExecuteThreadCurrentIdleCount());
		request.setValue((double) serverInst.getPendingRequestCurrentCount());
		memoryUsage.setValue((double) serverInst.getMemoryUsage());
		prs.add(name);
		prs.add(state);
		prs.add(runState);
		prs.add(ideThreadCount);
		prs.add(request);
		prs.add(memoryUsage);
		result.setPerfResults(prs.toArray(new PerfResult[0]));

		return result;
	}
}
