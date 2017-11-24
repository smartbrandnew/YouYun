package com.broada.carrier.monitor.impl.mw.weblogic.agent.jvm;

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

/**
 * weblogic jvm监测执行方法
 * 
 * @author zhuhong
 * 
 */
public class WLSJvmMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(WLSJvmMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

		WebLogicJMXOption option = new WebLogicJMXOption(context.getMethod());
		List<JvmInfo> jvms = new ArrayList<JvmInfo>();
		long startTime = System.currentTimeMillis();
		long respTime = 0;
		try {
			jvms = WLSJvmMonitorUtil.getJvmInfomations(WLSBasicMonitorUtil
					.getBaseInfoUrl(option));
		} catch (UnsupportedEncodingException e) {
			logger.info(e);
			result.setResultDesc("无法将获取JVM信息的URL用Base64方式编码。");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} catch (Exception e) {
			logger.info(e);
			if (e instanceof ConnectException) {
				result.setResultDesc("无法获取JVM信息,可能网络不通或者目标主机上的weblogic没有启动.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			} else {
				result.setResultDesc("无法将获取JVM信息的URL用Base64方式编码。");
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

		for (JvmInfo jvm : jvms) {
			MonitorResultRow row = new MonitorResultRow(jvm.getInstKey());
			row.setIndicator("WLS-JVM-1", jvm.getHeapCurr());
			row.setIndicator("WLS-JVM-2", jvm.getHeapFree());
			row.setIndicator("WLS-JVM-3", jvm.getHeapPercent());
			row.setIndicator("WLS-JVM-4", jvm.getHeapMax());
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
