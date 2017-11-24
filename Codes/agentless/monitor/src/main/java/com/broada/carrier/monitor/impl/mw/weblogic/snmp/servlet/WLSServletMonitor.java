package com.broada.carrier.monitor.impl.mw.weblogic.snmp.servlet;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.WLSSNMPUtil;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class WLSServletMonitor extends BaseMonitor {
  private static final String INDEX_AVGTIME = "WLSSERVLET-1";

  private static final String INDEX_MAXTIME = "WLSSERVLET-2";

  private static final String INDEX_INVOKETIMES = "WLSSERVLET-3";

  private static final String INDEX_INVOKEPER = "WLSSERVLET-4";

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    List<WLSServletInfo> conditions;
    long respTime = System.currentTimeMillis();
    try {
    	conditions = WLSSNMPUtil.getMonitorServletInfos(context.getNode().getIp(), new SnmpMethod(context.getMethod()));
    } catch (Exception e) {
      result.setResultDesc("获取WebLogic Servlet信息发生错误:" + e.getMessage());
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    // 计算响应时间
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    for (int index = 0; index < conditions.size(); index++) {
      WLSServletInfo info = (WLSServletInfo) conditions.get(index);
      MonitorResultRow row = new MonitorResultRow(info.getField());
      row.setIndicator(INDEX_AVGTIME, Double.parseDouble(info.getAvgTime()));
      row.setIndicator(INDEX_MAXTIME, Double.parseDouble(info.getMaxTime()));
      row.setIndicator(INDEX_INVOKETIMES, Double.parseDouble(info.getInvokeTimes()));
      if (info.getTotalTime() != null && (Double.parseDouble(info.getTotalTime()) / 1000) != 0) {
        row.setIndicator(INDEX_INVOKEPER, new BigDecimal(Double.parseDouble(info.getInvokeTimes())
            / (Double.parseDouble(info.getTotalTime()) / 1000)).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
      } else {
        row.setIndicator(INDEX_INVOKEPER, 0);
      }
      result.addRow(row);
    }
    return result;
  }  
}
