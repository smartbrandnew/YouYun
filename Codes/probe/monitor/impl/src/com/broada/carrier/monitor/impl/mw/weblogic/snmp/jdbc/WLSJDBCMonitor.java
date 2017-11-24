package com.broada.carrier.monitor.impl.mw.weblogic.snmp.jdbc;

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
import com.broada.snmputil.SnmpException;

/**
 * <p>Title: </p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 1.0
 */
public class WLSJDBCMonitor extends BaseMonitor {

  private static final String ITEMIDX_WLSJDBCTOTALCONNECTIONS = "WLSJDBC-1";

  private static final String ITEMIDX_WLSJDBCACTIVECONNECTIONS = "WLSJDBC-2";

  private static final String ITEMIDX_WLSJDBCACTIVECONNECTIONSHIGH = "WLSJDBC-3";

  private static final String ITEMIDX_WLSJDBCWAITINGFORCONNECTIONS = "WLSJDBC-4";

  private static final String ITEMIDX_WLSJDBCWAITINGFORCONNECTIONSHIGH = "WLSJDBC-5";

  private static final String ITEMIDX_WLSJDBCWAITSECONDSHIGH = "WLSJDBC-6";

  private static final String ITEMIDX_WLSJDBCMAXCAPACITY = "WLSJDBC-7";
  
  private static final String ITEMIDX_WLSJDBCACTIVERATIO = "WLSJDBC-8";

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    long startTime=System.currentTimeMillis();
    List wlsJdbcs = null;
    try {
      wlsJdbcs = WLSSNMPUtil.getJDBCInstances(context.getNode().getIp(), new SnmpMethod(context.getMethod()));
    } catch (SnmpException e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("目标节点可能不支持SNMP代理或者配置错误,无法进行JDBC监测.");
      return result;
    }
    long respTime = System.currentTimeMillis() - startTime;
    if(respTime<=0){
      respTime=1;
    }
    result.setResponseTime(respTime);
    //监测结果信息和告警的当前值信息
    for(int index = 0; index < wlsJdbcs.size(); index++){    	
      WlsJdbcInstanceInfo wlsJdbc = (WlsJdbcInstanceInfo)wlsJdbcs.get(index);
      
      MonitorResultRow row = new MonitorResultRow(wlsJdbc.getField());
      row.setIndicator(ITEMIDX_WLSJDBCTOTALCONNECTIONS, wlsJdbc.getTotalConn());
      row.setIndicator(ITEMIDX_WLSJDBCACTIVECONNECTIONS, wlsJdbc.getActiveConn());
      row.setIndicator(ITEMIDX_WLSJDBCACTIVECONNECTIONSHIGH, wlsJdbc.getMaxActivedConn());
      row.setIndicator(ITEMIDX_WLSJDBCWAITINGFORCONNECTIONS, wlsJdbc.getWaitingConn());
      row.setIndicator(ITEMIDX_WLSJDBCWAITINGFORCONNECTIONSHIGH, wlsJdbc.getMaxWaitConn());
      row.setIndicator(ITEMIDX_WLSJDBCWAITSECONDSHIGH, wlsJdbc.getMaxWaitingTime());
      row.setIndicator(ITEMIDX_WLSJDBCMAXCAPACITY, wlsJdbc.getCapacity());
      if(wlsJdbc.getTotalConn()!=0){
        row.setIndicator(ITEMIDX_WLSJDBCACTIVERATIO, new BigDecimal((wlsJdbc.getActiveConn()*100/wlsJdbc.getTotalConn())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
      }else{
        row.setIndicator(ITEMIDX_WLSJDBCACTIVERATIO, 100);
      }      
      result.addRow(row);      
    }
    return result;
  }
}
