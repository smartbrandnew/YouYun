package com.broada.carrier.monitor.impl.mw.weblogic.agent.servlet;

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
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class WLSServletMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(WLSServletMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

		// 监测结果信息和告警的当前值信息
		
		WebLogicJMXOption option = new WebLogicJMXOption(context.getMethod());
		List<ServletInstances> servlets = new ArrayList<ServletInstances>();
		long startTime = System.currentTimeMillis();
		long respTime = 0;
		try {
			servlets = WLSServletMonitorUtil.getServletInfomations(WLSBasicMonitorUtil
					.getServletInfoUrl(option));
		} catch (UnsupportedEncodingException e) {
			logger.info(e);
			result.setResultDesc("无法将获取Servlet信息的URL用Base64方式编码。");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} catch (Exception e) {
			logger.info(e);
			if (e instanceof ConnectException) {
				result.setResultDesc("无法获取Servlet信息,可能网络不通或者目标主机上的weblogic没有启动.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			} else {
				result.setResultDesc("无法将获取Servlet信息的URL用Base64方式编码。");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			}
		} catch (Throwable e) {
			logger.info(e);
			result.setResultDesc("出现未知错误");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		}
		respTime = calcResTime(result, startTime, respTime);

		for (ServletInstances servlet : servlets) {
			MonitorResultRow row = new MonitorResultRow(servlet.getInstKey(), servlet.getServletName());
			row.setIndicator("WLS-SERVLET-1", Double.parseDouble(servlet.getInvokeTimes()));
			row.setIndicator("WLS-SERVLET-2", servlet.getMaxTime());
			row.setIndicator("WLS-SERVLET-3", servlet.getAvgTime());
			result.addRow(row);
		}

		return result;
	}

	private long calcResTime(MonitorResult result, long startTime, long respTime) {
		if (respTime > 0)
			return respTime;
		respTime = System.currentTimeMillis() - startTime;
		if (respTime <= 0) {
			respTime = 1;
		}
		result.setResponseTime(respTime);
		return respTime;
	}
}
