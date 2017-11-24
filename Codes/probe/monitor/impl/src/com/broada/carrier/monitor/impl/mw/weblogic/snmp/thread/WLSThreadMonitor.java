package com.broada.carrier.monitor.impl.mw.weblogic.snmp.thread;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.WLSSNMPUtil;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class WLSThreadMonitor extends BaseMonitor {
  private static final Log logger = LogFactory.getLog(WLSThreadMonitor.class);

  private static final String INDEX_TOTALTHREAD = "WLSTHREAD-1";

  private static final String INDEX_IDLETHREAD = "WLSTHREAD-2";

  private static final String INDEX_PENDINGREQUEST = "WLSTHREAD-3";

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    List wlsThreadInfos = null;
    long respTime=System.currentTimeMillis();
    try {
      wlsThreadInfos = WLSSNMPUtil.getWLSThreads(context.getNode().getIp(), new SnmpMethod(context.getMethod()));
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("获取WebLogic线程信息发生错误", e);
      }
      result.setResultDesc("获取WebLogic线程信息发生错误:" + e.getMessage());
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    //计算响应时间
    respTime=System.currentTimeMillis()-respTime;
    if(respTime<=0){
      respTime=1;
    }

    for (int index = 0; index < wlsThreadInfos.size(); index++) {
      WLSThreadInfo wlsThreadInfo = (WLSThreadInfo) wlsThreadInfos.get(index);
      MonitorResultRow row = new MonitorResultRow(wlsThreadInfo.getField());
      row.setIndicator(INDEX_TOTALTHREAD, Double.parseDouble(wlsThreadInfo.getTotalThreadNumber()));
      row.setIndicator(INDEX_IDLETHREAD, Double.parseDouble(wlsThreadInfo.getIdleThreadNumber()));
      row.setIndicator(INDEX_PENDINGREQUEST, Double.parseDouble(wlsThreadInfo.getPendingRequestNumber()));
      result.addRow(row);
    }
    return result;
  }
}
