package com.broada.carrier.monitor.impl.mw.weblogic.snmp.webapp;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.weblogic.entity.WebAppInstance;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.SnmpWLSManager;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;

/**
 * zhuhong
 */
public class WLSWebAppMonitor extends BaseMonitor {
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		SnmpMethod method = new SnmpMethod(context.getMethod());

		// 监测结果信息和告警的当前值信息
		StringBuffer msgSB = new StringBuffer();
		MonitorState state = MonitorConstant.MONITORSTATE_NICER;
		SnmpWalk walk = new SnmpWalk(method.getTarget(context.getNode().getIp()));
		SnmpWLSManager mgr = new SnmpWLSManager(walk);
		try {
			List<WebAppInstance> apps = mgr.getALLWLS_WEBAPP_INSTANCE();
			for (WebAppInstance app : apps) {
				MonitorResultRow row = new MonitorResultRow(app.getInstanceKey());
				row.setIndicator("WLSWEBAPP-1", app.getCurSession());
				row.setIndicator("WLSWEBAPP-2", mgr.getWebAppOpenSessionHigh(app.getInstanceKey()));
				row.setIndicator("WLSWEBAPP-3", mgr.getWebAppOpenSessionTotal(app.getInstanceKey()));
				result.addRow(row);
				msgSB.append("Weblogic Web应用" + row.getName());
				msgSB.append("的当前Session数量为:" + app.getCurSession() + "个");
				msgSB.append(";\n");
			}
		} catch (SnmpException snmpEx) {
			msgSB.append("目标节点可能不支持SNMP代理或者配置错误,无法进行Web应用监测.");
			state = MonitorConstant.MONITORSTATE_FAILING;
		} catch (Exception ex) {
			msgSB.append(ex.getMessage());
			state = MonitorConstant.MONITORSTATE_FAILING;
		} finally {
			walk.close();
		}

		result.setResultDesc(msgSB.toString());
		result.setState(state);
		return result;
	}
}