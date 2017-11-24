package com.broada.carrier.monitor.impl.mw.weblogic.agent.webapp;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.basic.WLSBasicMonitorUtil;
import com.broada.carrier.monitor.impl.mw.weblogic.entity.WebAppInstance;
import com.broada.carrier.monitor.method.weblogic.agent.WebLogicJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * zhuhong
 */
public class WLSWebAppMonitor extends BaseMonitor {
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		WebLogicJMXOption method = new WebLogicJMXOption(context.getMethod());
		// 监测结果信息和告警的当前值信息
		StringBuffer msgSB = new StringBuffer();
		List<WebAppInstance> _list = null;
		long startTime = System.currentTimeMillis();
		long respTime = 0;
		MonitorState state = MonitorConstant.MONITORSTATE_NICER;
		try {
			_list = WLSWebAppMonitorUtil.getWebAppInfomations(WLSBasicMonitorUtil
					.getWebAppInfoUrl(method));
		} catch (UnsupportedEncodingException e) {
			msgSB.append("无法将获取Web应用信息的URL用Base64方式编码。");
			state = MonitorConstant.MONITORSTATE_FAILING;
		} catch (Exception e) {
			if (e instanceof ConnectException) {
				result.setResultDesc("无法获取Web应用信息,可能网络不通或者目标主机上的weblogic没有启动.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			} else {
				msgSB.append(e.getMessage() + "\n");
				state = MonitorConstant.MONITORSTATE_FAILING;
			}
		}
		respTime = calcResTime(result, startTime, respTime);
		Map instMap = new HashMap();
		if (_list != null && !_list.isEmpty()) {
			for (Iterator itr = _list.iterator(); itr.hasNext();) {
				WebAppInstance webapp = (WebAppInstance) itr.next();
				instMap.put(webapp.getInstanceKey(), webapp);
			}
		}

		if (_list != null) {
			for (WebAppInstance webapp : _list) {
				MonitorResultRow row = new MonitorResultRow(webapp.getInstanceKey());
				long sessionCur = webapp.getCurSession().longValue();
				long seHigh = webapp.getCurMaxSession().longValue();
				long sessionTotal = webapp.getCurTotalSession().longValue();
				row.setIndicator("WLSWEBAPP-1", sessionCur);
				row.setIndicator("WLSWEBAPP-2", seHigh);
				row.setIndicator("WLSWEBAPP-3", sessionTotal);
				result.addRow(row);
				msgSB.append("Weblogic Web应用" + row.getName());
				msgSB.append("的当前Session数量为:" + sessionCur + "个");
				msgSB.append(";\n");
			}
		}

		result.setResultDesc(msgSB.toString());
		result.setState(state);
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