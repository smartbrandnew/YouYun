package com.broada.carrier.monitor.impl.mw.weblogic.agent.cluster;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
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
 * weblogic 集群的监测类
 * 
 * @author Yaojj Create By Mar 18, 2010 10:44:13 AM
 */
public class WLSClusterMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(WLSClusterMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

		WebLogicJMXOption option = new WebLogicJMXOption(context.getMethod());
		
		List<ServerInstance> serverList = new ArrayList<ServerInstance>();
		try {
      long replyTime = System.currentTimeMillis();
			serverList = WLSClusterMonitorUtil.getClusterInformations(WLSBasicMonitorUtil
					.getClusterInfoUrl(option));
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
		} catch (UnsupportedEncodingException e) {
			if (logger.isDebugEnabled())
				logger.debug(e);
			result.setResultDesc("无法将获取集群服务信息的URL用Base64方式编码。");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug(e);
			if (e instanceof ConnectException) {
				result.setResultDesc("无法获取集群服务信息,可能网络不通或者目标主机上的weblogic没有启动.");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			} else {
				result.setResultDesc("无法将获取集群服务信息的URL用Base64方式编码。");
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
				return result;
			}
		} catch (Throwable e) {
			if (logger.isDebugEnabled())
				logger.debug(e);
			result.setResultDesc("出现未知错误");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		}
		for (Iterator iterator2 = serverList.iterator(); iterator2.hasNext();) {
			ServerInstance server = (ServerInstance) iterator2.next();
			String key = server.getClusterName()+"_"+server.getServerName();
			MonitorResultRow row = new MonitorResultRow(key);
			row.setIndicator("WLS-CLUSTER-1", server.getClusterName());
			row.setIndicator("WLS-CLUSTER-2", server.getServerName());
			row.setIndicator("WLS-CLUSTER-3", server.getState());
			result.addRow(row);
		}
		return result;
	}
}
