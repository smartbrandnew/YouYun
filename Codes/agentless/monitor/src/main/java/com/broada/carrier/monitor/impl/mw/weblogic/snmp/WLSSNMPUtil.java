package com.broada.carrier.monitor.impl.mw.weblogic.snmp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.mw.weblogic.snmp.jta.JtaInfo;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.servlet.ServletInfoComparator;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.servlet.WLSServletInfo;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.thread.WLSThreadInfo;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.snmp.SnmpNotFoundException;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;

public class WLSSNMPUtil {

  private static final Log logger = LogFactory.getLog(WLSSNMPUtil.class);

  /**
   * WebLogic 线程监测 name, idleThread, Throughput
   */
  private static final String[] WEBLOGIC_THREAD = new String[] { 
    ".1.3.6.1.4.1.140.625.180.1.15",
    ".1.3.6.1.4.1.140.625.180.1.25", 
    ".1.3.6.1.4.1.140.625.180.1.35", 
    ".1.3.6.1.4.1.140.625.180.1.40" };

  /**
   * WebLogic Servlet - name, avgtime, hightime, invoketimes
   */
  private static final String[] WEBLOGIC_SERVLET = new String[] { 
    ".1.3.6.1.4.1.140.625.380.1.25",
    ".1.3.6.1.4.1.140.625.380.1.60", 
    ".1.3.6.1.4.1.140.625.380.1.50", 
    ".1.3.6.1.4.1.140.625.380.1.35",
    ".1.3.6.1.4.1.140.625.380.1.45"};

  public static List getJDBCInstances(String ip, SnmpMethod snmpMonitorMethodOption) throws SnmpException{
    SnmpWalk walk = getSnmpWalk(ip, snmpMonitorMethodOption);
    try{
      SnmpWLSManager snmpWLSManager = new SnmpWLSManager(walk);
      return snmpWLSManager.getALLWLS_JDBC_INSTANCE();
    }finally{
      if(walk != null){
        walk.close();
      }
    }
  }
  
  public static List getWlecInstances(String ip, SnmpMethod snmpMonitorMethodOption) throws SnmpException, SnmpNotFoundException{
    SnmpWalk walk = getSnmpWalk(ip, snmpMonitorMethodOption);
    try{
      SnmpWLSManager snmpWLSManager = new SnmpWLSManager(walk);
    return snmpWLSManager.getALLWLS_WLEC_INSTANCE();
    }finally{
      if(walk != null){
        walk.close();
      }
    }
  }
  
  public static List getWLSThreads(String ip, SnmpMethod snmpMonitorMethodOption) throws Exception {
    if (logger.isDebugEnabled()) {
      logger.debug(ReflectionToStringBuilder.reflectionToString(snmpMonitorMethodOption));
    }
    List wlsThreadInfos = new ArrayList();

    List results = wlsSnmpWork(ip, snmpMonitorMethodOption, WEBLOGIC_THREAD);
    String[] v1 = (String[]) results.get(0);
    String[] v2 = (String[]) results.get(1);
    String[] v3 = (String[]) results.get(2);
    String[] v4 = (String[]) results.get(3);
    for(int index = 0; index < v1.length; index++){
      WLSThreadInfo wlsThreadInfo = new WLSThreadInfo();
      wlsThreadInfo.setField(v1[index]);
      wlsThreadInfo.setTotalThreadNumber(v4[index]);
      wlsThreadInfo.setIdleThreadNumber(v2[index]);
      wlsThreadInfo.setPendingRequestNumber(v3[index]);
      wlsThreadInfos.add(wlsThreadInfo);
    }
    return wlsThreadInfos;
  }

  /**
   * List中是一个字符串数组
   * @param oids
   * @return
   * @throws SnmpException 
   */
  private static List wlsSnmpWork(String ip, SnmpMethod snmpMonitorMethodOption, String[] oids) throws SnmpException{
    SnmpWalk walk = getSnmpWalk(ip, snmpMonitorMethodOption);
    List resultList = new ArrayList();
    for(int index = 0; index < oids.length; index++){
      resultList.add(walk.snmpWalk(oids[index]));
    }
    
    List results = new ArrayList();
   
    for(int index = 0; index < WEBLOGIC_THREAD.length; index++){
      SnmpResult[] rets = (SnmpResult[]) resultList.get(index);
      String[] values = new String[rets.length];
      for(int m = 0; m < rets.length; m++){
        values[m] = rets[m].getValue().toString();
      }
      results.add(values);
    }
    walk.close();
    return results;
  }
  private static SnmpWalk getSnmpWalk(String ip, SnmpMethod snmpMonitorMethodOption) {
    SnmpWalk walk = new SnmpWalk(snmpMonitorMethodOption.getTarget(ip));
    return walk;
  }

  public static List<WLSServletInfo> getMonitorServletInfos(String ip, SnmpMethod snmpMonitorMethodOption)
      throws Exception {
    return getServletInfos(ip, snmpMonitorMethodOption);
  }
  
  public static List<WLSServletInfo> getServletInfos(String ip, SnmpMethod snmpMonitorMethodOption)
      throws Exception {
    SnmpWalk walk = getSnmpWalk(ip, snmpMonitorMethodOption);
    List wlsServletInfos = new ArrayList();
    SnmpResult[] servletNames = walk.snmpWalk(WEBLOGIC_SERVLET[0]);
    SnmpResult[] avgTime = walk.snmpWalk(WEBLOGIC_SERVLET[1]);
    SnmpResult[] highTime = walk.snmpWalk(WEBLOGIC_SERVLET[2]);
    SnmpResult[] invokeTimes = walk.snmpWalk(WEBLOGIC_SERVLET[3]);
    SnmpResult[] totalTime = walk.snmpWalk(WEBLOGIC_SERVLET[4]);

    for(int index = 0; index < servletNames.length; index++) {
      WLSServletInfo wlsServletInfo = new WLSServletInfo();
      SnmpResult servletNameValue = servletNames[index];
      SnmpResult avgTimeValue = avgTime[index];
      SnmpResult highTimeValue = highTime[index];
      SnmpResult invokeTimesValue = invokeTimes[index];
      SnmpResult totalTimeValue = totalTime[index];
      wlsServletInfo.setField(servletNameValue.getValue().toString());

      wlsServletInfo.setAvgTime(avgTimeValue.getValue().toString());
      wlsServletInfo.setMaxTime(highTimeValue.getValue().toString());
      wlsServletInfo.setInvokeTimes(invokeTimesValue.getValue().toString());
      wlsServletInfo.setTotalTime(totalTimeValue.getValue().toString());

      wlsServletInfos.add(wlsServletInfo);
    }
    walk.close();
    Collections.sort(wlsServletInfos, new ServletInfoComparator());
    return wlsServletInfos;
  }
  
  public static List getJtaInstances(String ip, SnmpMethod snmpMonitorMethodOption) throws Exception{
    List ret = new ArrayList();
    SnmpWalk walk = getSnmpWalk(ip, snmpMonitorMethodOption);
    try{
      SnmpWLSManager mgr = new SnmpWLSManager(walk);
      JtaInfo jta = new JtaInfo();
      try {
        jta.setInsKey("");
        jta.setTotalTa(Double.parseDouble(mgr.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TATOTAL)));   
        jta.setRbTaRc(Double.parseDouble(mgr.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TA_RB_RC)));   
        jta.setRbTaSys(Double.parseDouble(mgr.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TA_RB_SYS))); 
        jta.setRbTaApp(Double.parseDouble(mgr.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TA_RB_APP)));
      } catch (com.broada.snmp.SnmpException ex) {
        if (ex.getErrindex() == 0) {
          throw new SnmpException("目标节点不支持SNMP代理,无法监测Weblogic服务器JTA,请检查配置参数！", ex);
        } else {
          throw new SnmpException("无法获取JTA性能,目标节点可能不支持JTA性能查看功能,或者监测目标和实例配置错误！", ex);
        }
      } catch (SnmpNotFoundException ex) {
          throw new SnmpException("成功连接到Weblogic SNMP代理,但是无法获取到JTA运行性能状况！", ex);
      }catch (Exception e) {
        throw e;
      }
    }finally{
      if(walk != null){
        walk.close();
      }
    }
    return ret;
  }
  
}
