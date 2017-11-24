package com.broada.carrier.monitor.impl.mw.weblogic.agent.webappstatus;

import java.io.Serializable;
import java.net.ConnectException;
import java.util.Iterator;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.basic.WLSBasicMonitorUtil;
import com.broada.carrier.monitor.method.weblogic.agent.WebLogicJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * zhuhong
 */
public class WLSWebAppStatusMonitor extends BaseMonitor {
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		
		WebLogicJMXOption option = new WebLogicJMXOption(context.getMethod());
		List _list = null;
		long startTime = System.currentTimeMillis();
		long respTime = 0;
		try {
			_list = WLSWebAppStatusMonitorUtil.getWebAppInfomations(WLSBasicMonitorUtil
					.getWebAppInfoUrl(option));
		} catch (Exception e) {
			if (e instanceof ConnectException) {
				result.setResultDesc("无法获取Web应用信息,可能网络不通或者目标主机上的weblogic没有启动.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			} 
		}
		respTime = calcResTime(result, startTime, respTime);
		
		for (Iterator itr = _list.iterator(); itr.hasNext();) {
			WebAppStatusInstance webapp = (WebAppStatusInstance) itr.next();
			MonitorResultRow row = new MonitorResultRow(webapp.getInstanceKey(), webapp.getAppName());
			row.setIndicator("WLS-STATUS-1", webapp.getAppStatus());
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