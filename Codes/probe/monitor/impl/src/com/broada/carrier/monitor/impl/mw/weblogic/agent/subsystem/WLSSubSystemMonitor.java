package com.broada.carrier.monitor.impl.mw.weblogic.agent.subsystem;

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

public class WLSSubSystemMonitor extends BaseMonitor {

	private static final Log logger = LogFactory.getLog(WLSSubSystemMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

		WebLogicJMXOption option = new WebLogicJMXOption(context.getMethod());
		List<SubSystemInstance> subSystems = new ArrayList<SubSystemInstance>();
		try {
      long replyTime = System.currentTimeMillis();
			subSystems = WLSSubSystemMonitorUtil.getSubSystemInfo(WLSBasicMonitorUtil
					.getSubSystemInfoUrl(option));
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
		} catch (UnsupportedEncodingException e) {
			logger.info(e);
			result.setResultDesc("无法将获取子系统信息的URL用Base64方式编码。");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} catch (Exception e) {
			logger.info(e);
			if (e instanceof ConnectException) {
				result.setResultDesc("无法获取子系统信息,可能网络不通或者目标主机上的weblogic没有启动.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			} else {
				result.setResultDesc("无法将获取子系统信息的URL用Base64方式编码。");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			}
		} catch (Throwable e) {
			logger.info(e);
			result.setResultDesc("出现未知错误");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		}
		for (SubSystemInstance subSystem : subSystems) {
			MonitorResultRow row = new MonitorResultRow(subSystem.getInstKey(), subSystem.getSubSystem());
			row.setIndicator("WLS-SUBSYSTEM-1", subSystem.getState());
			row.setIndicator("WLS-SUBSYSTEM-2", subSystem.getReasonCode());
			result.addRow(row);
		}

		return result;
	}
}
