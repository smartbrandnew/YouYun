package com.broada.carrier.monitor.impl.mw.weblogic.snmp.wlec;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.WLSSNMPUtil;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpNotFoundException;
import com.broada.snmputil.SnmpException;

public class WlsWLECMonitor extends BaseMonitor {

  private static final String ITEMIDX_WLS_WLEC_OBJECTNAME = "WLS-WLEC-1";

  private static final String ITEMIDX_WLS_WLEC_TYPE = "WLS-WLEC-2";

  private static final String ITEMIDX_WLS_WLEC_ADDRESS = "WLS-WLEC-3";

  private static final String ITEMIDX_WLS_WLEC_REQUESTCOUNT = "WLS-WLEC-4";

  private static final String ITEMIDX_WLS_WLEC_PENDINGCOUNT = "WLS-WLEC-5";

  private static final String ITEMIDX_WLS_WLEC_ERRORCOUNT = "WLS-WLEC-6";

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    long startTime = System.currentTimeMillis();
    List wlsWlecs = null;
    try {
      wlsWlecs = WLSSNMPUtil.getWlecInstances(context.getNode().getIp(), new SnmpMethod(context.getMethod()));
    } catch (SnmpNotFoundException e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("目标节点可能不支持SNMP代理或者配置错误,无法进行Wlec监测.");
      return result;
    } catch (SnmpException e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("目标节点可能不支持SNMP代理或者配置错误,无法进行Wlec监测.");
      return result;
    }
    long respTime = System.currentTimeMillis() - startTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    result.setResponseTime(respTime);
    // 监测结果信息和告警的当前值信息
    for (int index = 0; index < wlsWlecs.size(); index++) {
      WlsWlecInstanceInfo wlsWlec = (WlsWlecInstanceInfo) wlsWlecs.get(index);
      MonitorResultRow row = new MonitorResultRow(wlsWlec.getField());
      row.setIndicator(ITEMIDX_WLS_WLEC_OBJECTNAME, wlsWlec.getObjectName());
      row.setIndicator(ITEMIDX_WLS_WLEC_TYPE, wlsWlec.getWlecType());
      row.setIndicator(ITEMIDX_WLS_WLEC_ADDRESS, wlsWlec.getAddress());
      row.setIndicator(ITEMIDX_WLS_WLEC_REQUESTCOUNT, wlsWlec.getRequestCount());
      row.setIndicator(ITEMIDX_WLS_WLEC_PENDINGCOUNT, wlsWlec.getPendingCount());
      row.setIndicator(ITEMIDX_WLS_WLEC_ERRORCOUNT, wlsWlec.getErrorCount());
      result.addRow(row);
    }
    return result;
  }
}
