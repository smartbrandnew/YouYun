package com.broada.carrier.monitor.impl.mw.weblogic.snmp;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.mw.weblogic.entity.WebAppInstance;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.jdbc.WlsJdbcInstanceInfo;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.wlec.WlsWlecInstanceInfo;
import com.broada.snmp.SnmpNotFoundException;
import com.broada.snmp.SnmpUtil;
import com.broada.snmp.SnmpWalk;
import com.broada.snmp.SnmpWalkUtil;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;
import com.broada.utils.NumberUtil;

/**
 * 通过Snmp协议进行Weblogic性能参数获取和管理的类
 * 
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * @author plx
 * @version 1.0
 */
public class SnmpWLSManager {
  private static final Log logger = LogFactory.getLog(SnmpWLSManager.class);

  /*------------ basic ------------*/
  public static final String OID_WLS_BASIC_RUNTIME = ".1.3.6.1.4.1.140.625.360.1.25";

  // Weblogic相关的MIB OID定义
  /*------------  JVM  ------------*/
  public static final String OID_WLS_JVM_HEAPFREE = ".1.3.6.1.4.1.140.625.340.1.25";

  public static final String OID_WLS_JVM_HEAPSIZE = ".1.3.6.1.4.1.140.625.340.1.30";

  public static final String OID_WLS_JVM_HEAPUTIL = "(.1.3.6.1.4.1.140.625.340.1.30 - .1.3.6.1.4.1.140.625.340.1.25) / .1.3.6.1.4.1.140.625.340.1.30 * 100";

  public static final String OID_WLS_JVM_RTNAME = ".1.3.6.1.4.1.140.625.340.1.15";

  public static final String OID_WLS_JVM_RTJAVAVENDOR = ".1.3.6.1.4.1.140.625.340.1.40";

  public static final String OID_WLS_JVM_RTJAVAVERSION = ".1.3.6.1.4.1.140.625.340.1.35";

  /*------------  JDBC  ------------*/
  public static final String OID_WLS_JDBC_INDEX = ".1.3.6.1.4.1.140.625.190.1.1";

  public static final String OID_WLS_JDBC_NAME = ".1.3.6.1.4.1.140.625.190.1.15";

  public static final String OID_WLS_JDBC_TOTALCONNECTIONS = ".1.3.6.1.4.1.140.625.190.1.55";

  public static final String OID_WLS_JDBC_ACTIVECONNECTIONS = ".1.3.6.1.4.1.140.625.190.1.25";

  public static final String OID_WLS_JDBC_ACTIVECONNECTIONSHIGH = ".1.3.6.1.4.1.140.625.190.1.40";

  public static final String OID_WLS_JDBC_WAITINGFORCONNECTIONS = ".1.3.6.1.4.1.140.625.190.1.30";

  public static final String OID_WLS_JDBC_WAITINGFORCONNECTIONSHIGH = ".1.3.6.1.4.1.140.625.190.1.45";

  public static final String OID_WLS_JDBC_WAITSECONDSHIGH = ".1.3.6.1.4.1.140.625.190.1.50";

  public static final String OID_WLS_JDBC_MAXCAPACITY = ".1.3.6.1.4.1.140.625.190.1.60";

  /*---------JTA-------*/
  public static final String OID_WLS_JTA_RTINDEX = ".1.3.6.1.4.1.140.625.310.1.1";

  public static final String OID_WLS_JTA_RTNAME = ".1.3.6.1.4.1.140.625.310.1.15";

  // 当前已处理的事务总数
  public static final String OID_WLS_JTA_TATOTAL = ".1.3.6.1.4.1.140.625.310.1.25";

  // 当前已回滚的事务总数
  public static final String OID_WLS_JTA_RBTOTAL = ".1.3.6.1.4.1.140.625.310.1.35";

  // 因资源错误而导致回滚的事务数,jtaRuntimeTransactionRolledBackResourceTotalCount
  public static final String OID_WLS_JTA_TA_RB_RC = ".1.3.6.1.4.1.140.625.310.1.45";

  // 因应用程序错误而导致回滚的事务数,jjtaRuntimeTransactionRolledBackAppTotalCount
  public static final String OID_WLS_JTA_TA_RB_APP = ".1.3.6.1.4.1.140.625.310.1.50";

  // 因系统内部错误导致回滚的事务数
  public static final String OID_WLS_JTA_TA_RB_SYS = ".1.3.6.1.4.1.140.625.310.1.55";

  /*------------  WEBAPP  ------------*/
  public static final String OID_WLS_WEBAPP_INDEX = ".1.3.6.1.4.1.140.625.430.1.1";

  public static final String OID_WLS_WEBAPP_COMPONENTNAME = ".1.3.6.1.4.1.140.625.430.1.25";

  public static final String OID_WLS_WEBAPP_OPENSESSIONCUR = ".1.3.6.1.4.1.140.625.430.1.50";

  public static final String OID_WLS_WEBAPP_OPENSESSIONHIGH = ".1.3.6.1.4.1.140.625.430.1.55";

  public static final String OID_WLS_WEBAPP_OPENSESSIONTOTAL = ".1.3.6.1.4.1.140.625.430.1.60";

  /*------------  企业连接  ------------*/
  public static final String OID_WLS_WLEC_INDEX = ".1.3.6.1.4.1.140.625.450.1.1";

  public static final String OID_WLS_WLEC_OBJNAME = ".1.3.6.1.4.1.140.625.450.1.5";

  public static final String OID_WLS_WLEC_TYPE = ".1.3.6.1.4.1.140.625.450.1.10";

  public static final String OID_WLS_WLEC_NAME = ".1.3.6.1.4.1.140.625.450.1.15";

  public static final String OID_WLS_WLEC_ADDRESS = ".1.3.6.1.4.1.140.625.450.1.25";

  public static final String OID_WLS_WLEC_REQUESTCOUNT = ".1.3.6.1.4.1.140.625.450.1.40";

  public static final String OID_WLS_WLEC_PENDING_REQUESTCOUNT = ".1.3.6.1.4.1.140.625.450.1.45";

  public static final String OID_WLS_WLEC_ERRORCOUNT = ".1.3.6.1.4.1.140.625.450.1.50";

  private final SnmpWalk walk;

  public SnmpWLSManager(SnmpWalk walk) {
    this.walk = walk;
  }

  /**
   * JVM相关性能获取
   */
  public long getJVMHeapSize() throws SnmpException, SnmpNotFoundException {
    return SnmpWalkUtil.getFirstLongValue(walk, OID_WLS_JVM_HEAPSIZE);
  }

  /**
   * JVM相关性能获取
   */
  public double getJVMHeapUtil() throws SnmpNotFoundException, Exception {
    return NumberUtil.round(SnmpWalkUtil.getFirstExpressionValue(walk, OID_WLS_JVM_HEAPUTIL), 2);
  }

  /**
   * JVM相关获取
   */
  public String getJVMName() throws SnmpNotFoundException, SnmpException {
    return SnmpWalkUtil.getFirstStringValue(walk, OID_WLS_JVM_RTNAME);
  }

  public String getJVMVendor() throws SnmpNotFoundException, SnmpException {
    return SnmpWalkUtil.getFirstStringValue(walk, OID_WLS_JVM_RTJAVAVENDOR);
  }

  public String getJVMVersion() throws SnmpNotFoundException, SnmpException {
    return SnmpWalkUtil.getFirstStringValue(walk, OID_WLS_JVM_RTJAVAVERSION);
  }

  /**
   * WLEC相关获取
   */

  public String getWLECType(String instanceKey) throws SnmpNotFoundException, SnmpException {
    return SnmpWalkUtil.getFirstStringValue(walk, OID_WLS_WLEC_TYPE + instanceKey);
  }

  public String getWLECName(String instanceKey) throws SnmpNotFoundException, SnmpException {
    return SnmpWalkUtil.getFirstStringValue(walk, OID_WLS_WLEC_NAME + instanceKey);
  }

  public String getWLECAddress(String instanceKey) throws SnmpNotFoundException, SnmpException {
    return SnmpWalkUtil.getFirstStringValue(walk, OID_WLS_WLEC_ADDRESS + instanceKey);
  }

  /**
   * 得到Weblogic JVM堆栈大小
   * @return
   * @throws SnmpException
   * @throws SnmpException
   */
  public long getWLS_JVM_HEAPSIZE() throws SnmpException {
    String heapsizeS = "";
    long heapsizeL = 0;
    SnmpResult[] results = walk.snmpWalk(OID_WLS_JVM_HEAPSIZE);
    if (results != null && results.length == 1) {
      SnmpResult _jvmvar = results[0];
      heapsizeS = _jvmvar.getValue().toString();
    }
    if (!heapsizeS.equals("")) {
      try {
        heapsizeL = Long.parseLong(heapsizeS);
      } catch (NumberFormatException e) {
        logger.error("数据格转换错误：" + heapsizeS, e);
      }
    }
    return heapsizeL;
  }

  /**
   * 得到Weblogic JVM空闲堆栈大小
   * @return
   * @throws SnmpException
   */
  public long getWLS_JVM_HEAPFREE() throws SnmpException {
    String heapfreeS = "";
    long heapfreeL = 0;
    SnmpResult[] results = walk.snmpWalk(OID_WLS_JVM_HEAPFREE);
    if (results != null && results.length >= 1) {
      SnmpResult _jvmvar = results[0];
      heapfreeS = _jvmvar.getValue().toString();
    }
    if (!heapfreeS.equals("")) {
      try {
        heapfreeL = Long.parseLong(heapfreeS);
      } catch (NumberFormatException e) {
        logger.error("数据格转换错误：" + heapfreeL, e);
      }
    }
    return heapfreeL;
  }

  /**
   * 取得所有WLEC连接池实例
   * @return
   * @throws SnmpException
   * @throws SnmpNotFoundException
   */
  public List getALLWLS_WLEC_INSTANCE() throws SnmpException, SnmpNotFoundException {
    List wlecInstances = new ArrayList();
    String instanceIndex = "";
    String instanceKey = "";
    String instanceName = "";
    SnmpResult[] results = walk.snmpWalk(OID_WLS_WLEC_INDEX);
    if (results != null && results.length > 0) {
      for (int i = 0; i < results.length; i++) {
        SnmpResult _wlecIndex = results[i];
        instanceIndex = _wlecIndex.getOid().toString();
        instanceKey = instanceIndex.substring(OID_WLS_WLEC_INDEX.length());

        SnmpResult[] nameResults = walk.snmpWalk(OID_WLS_WLEC_NAME + instanceKey);
        if (nameResults != null && nameResults.length == 1) {
          SnmpResult _wlecName = nameResults[0];
          instanceName = _wlecName.getValue().toString();
          WlsWlecInstanceInfo wlsWlecInstanceInfo = new WlsWlecInstanceInfo();
          wlsWlecInstanceInfo.setField(instanceName);
          wlsWlecInstanceInfo.setWlecType(getWLECType(instanceKey));
          wlsWlecInstanceInfo.setWlecName(getWLECName(instanceKey));
          wlsWlecInstanceInfo.setAddress(getWLECAddress(instanceKey));
          wlsWlecInstanceInfo.setRequestCount(getWLS_WLEC_REQUESTCOUNTByInstanceKey(instanceKey));
          wlsWlecInstanceInfo.setPendingCount(getWLS_WLEC_PENDING_REQUESTCOUNTByInstanceKey(instanceKey));
          wlsWlecInstanceInfo.setErrorCount(getWLS_WLEC_ERRORCOUNTByInstanceKey(instanceKey));
          wlecInstances.add(wlsWlecInstanceInfo);
        }
      }
    }
    return wlecInstances;
  }

  /**
   * 取得所有JDBC连接池实例
   * @return
   * @throws SnmpException
   */
  public List getALLWLS_JDBC_INSTANCE() throws SnmpException {
    List jdbcInstances = new ArrayList();
    String instanceIndex = "";
    String instanceKey = "";
    String instanceName = "";
    SnmpResult[] results = walk.snmpWalk(OID_WLS_JDBC_INDEX);
    if (results != null && results.length > 0) {
      for (int i = 0; i < results.length; i++) {
        SnmpResult _jdbcIndex = results[i];
        instanceIndex = _jdbcIndex.getOid().toString();
        instanceKey = instanceIndex.substring(OID_WLS_JDBC_INDEX.length());

        SnmpResult[] nameResults = walk.snmpWalk(OID_WLS_JDBC_NAME + instanceKey);
        if (nameResults != null && nameResults.length == 1) {
          SnmpResult _jdbcName = nameResults[0];
          instanceName = _jdbcName.getValue().toString();
          WlsJdbcInstanceInfo wlsJdbcInstanceInfo = new WlsJdbcInstanceInfo();
          wlsJdbcInstanceInfo.setField(instanceName);
          wlsJdbcInstanceInfo.setActiveConn(getWLS_JDBC_ACTIVEByInstanceKey(instanceKey));
          wlsJdbcInstanceInfo.setWaitingConn(getWLS_JDBC_WAITByInstanceKey(instanceKey));
          wlsJdbcInstanceInfo.setCapacity(getWLS_JDBC_MAXCAPACITYByInstanceKey(instanceKey));
          wlsJdbcInstanceInfo.setMaxActivedConn(getWLS_JDBC_ACTIVEHIGHByInstanceKey(instanceKey));
          wlsJdbcInstanceInfo.setMaxWaitConn(getWLS_JDBC_WAITHIGHByInstanceKey(instanceKey));
          wlsJdbcInstanceInfo.setMaxWaitingTime(getWLS_JDBC_WAITSECONDSHIGHByInstanceKey(instanceKey));
          wlsJdbcInstanceInfo.setTotalConn(getWLS_JDBC_TOTALByInstanceKey(instanceKey));
          jdbcInstances.add(wlsJdbcInstanceInfo);
        }
      }
    }
    return jdbcInstances;
  }

  /**
   * 根据jdbc连接池实例获得该连接池总数
   * @param expressionName jdbc连接池实例
   * @return
   * @throws SnmpException
   */
  public long getWLS_JDBC_TOTALByInstanceKey(String instanceKey) throws SnmpException {
    String totalS = "";
    long totalL = 0;
    SnmpResult[] totalResults = walk.snmpWalk(OID_WLS_JDBC_TOTALCONNECTIONS + instanceKey);
    if (totalResults != null && totalResults.length >= 1) {
      SnmpResult _jdbcTotal = totalResults[0];
      totalS = _jdbcTotal.getValue().toString();
    }
    if (!totalS.equals("")) {
      try {
        totalL = Long.parseLong(totalS);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    return totalL;
  }

  /**
   * 根据WLEC连接池实例获得该连接池连接错误的连接数
   * @param expressionName jdbc连接池实例
   * @return
   * @throws SnmpException
   */
  public long getWLS_WLEC_ERRORCOUNTByInstanceKey(String instanceKey) throws SnmpException {
    String errorCountS = "";
    long errorCountL = 0;
    SnmpResult[] errorResults = walk.snmpWalk(OID_WLS_WLEC_ERRORCOUNT + instanceKey);
    if (errorResults != null && errorResults.length >= 1) {
      SnmpResult _wlec_errorCount = errorResults[0];
      errorCountS = _wlec_errorCount.getValue().toString();
    }
    if (!errorCountS.equals("")) {
      try {
        errorCountL = Long.parseLong(errorCountS);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    return errorCountL;
  }

  /**
   * 根据WLEC连接池实例获得该连接池等待请求数
   * @param instanceKey wlec连接池实例
   * @return
   * @throws SnmpException
   */
  public long getWLS_WLEC_PENDING_REQUESTCOUNTByInstanceKey(String instanceKey) throws SnmpException {
    String pendingCountS = "";
    long pendingCountL = 0;
    SnmpResult[] pendingResults = walk.snmpWalk(OID_WLS_WLEC_PENDING_REQUESTCOUNT + instanceKey);
    if (pendingResults != null && pendingResults.length >= 1) {
      SnmpResult _wlec_pendingCount = pendingResults[0];
      pendingCountS = _wlec_pendingCount.getValue().toString();
    }
    if (!pendingCountS.equals("")) {
      try {
        pendingCountL = Long.parseLong(pendingCountS);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    return pendingCountL;
  }

  /**
   * 根据WLEC连接池实例获得该连接池请求数
   * @param instanceKey wlec连接池实例
   * @return
   * @throws SnmpException
   */
  public long getWLS_WLEC_REQUESTCOUNTByInstanceKey(String instanceKey) throws SnmpException {
    String requestCountS = "";
    long requestCountL = 0;
    SnmpResult[] requestResults = walk.snmpWalk(OID_WLS_WLEC_REQUESTCOUNT + instanceKey);
    if (requestResults != null && requestResults.length >= 1) {
      SnmpResult _wlec_requestCount = requestResults[0];
      requestCountS = _wlec_requestCount.getValue().toString();
    }
    if (!requestCountS.equals("")) {
      try {
        requestCountL = Long.parseLong(requestCountS);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    return requestCountL;
  }

  /**
   * 根据jdbc连接池实例获得该连接池激活的连接数
   * @param instanceKey wlec连接池实例
   * @return
   * @throws SnmpException
   */
  public long getWLS_JDBC_ACTIVEByInstanceKey(String instanceKey) throws SnmpException {
    String activeCountS = "";
    long activeCountL = 0;
    SnmpResult[] activeResults = walk.snmpWalk(OID_WLS_JDBC_ACTIVECONNECTIONS + instanceKey);
    if (activeResults != null && activeResults.length >= 1) {
      SnmpResult _jdbcActive = activeResults[0];
      activeCountS = _jdbcActive.getValue().toString();
    }
    if (!activeCountS.equals("")) {
      try {
        activeCountL = Long.parseLong(activeCountS);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    return activeCountL;
  }

  /**
   * 根据jdbc连接池实例获得该连接池激活的连接数的最大值
   * @param expressionName jdbc连接池实例
   * @return
   * @throws SnmpException
   */
  public long getWLS_JDBC_ACTIVEHIGHByInstanceKey(String instanceKey) throws SnmpException {
    String activeHighS = "";
    long activeHighL = 0;
    SnmpResult[] activeHighResults = walk.snmpWalk(OID_WLS_JDBC_ACTIVECONNECTIONSHIGH + instanceKey);
    if (activeHighResults != null && activeHighResults.length >= 1) {
      SnmpResult _jdbcActiveHigh = activeHighResults[0];
      activeHighS = _jdbcActiveHigh.getValue().toString();
    }
    if (!activeHighS.equals("")) {
      try {
        activeHighL = Long.parseLong(activeHighS);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    return activeHighL;
  }

  /**
   * 根据jdbc连接池实例获得该连接池等待的连接数
   * @param expressionName jdbc连接池实例
   * @return
   * @throws SnmpException
   */
  public long getWLS_JDBC_WAITByInstanceKey(String instanceKey) throws SnmpException {
    String waitCountS = "";
    long waitCountL = 0;
    SnmpResult[] waitResults = walk.snmpWalk(OID_WLS_JDBC_WAITINGFORCONNECTIONS + instanceKey);
    if (waitResults != null && waitResults.length >= 1) {
      SnmpResult _jdbcWait = waitResults[0];
      waitCountS = _jdbcWait.getValue().toString();
    }
    if (!waitCountS.equals("")) {
      try {
        waitCountL = Long.parseLong(waitCountS);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    return waitCountL;
  }

  /**
   * 根据jdbc连接池实例获得该连接池等待的连接数的最大值
   * @param expressionName jdbc连接池实例
   * @return
   * @throws SnmpException
   */
  public long getWLS_JDBC_WAITHIGHByInstanceKey(String instanceKey) throws SnmpException {
    String waitHighS = "";
    long waitHighL = 0;
    SnmpResult[] waitHighResults = walk.snmpWalk(OID_WLS_JDBC_WAITINGFORCONNECTIONSHIGH + instanceKey);
    if (waitHighResults != null && waitHighResults.length >= 1) {
      SnmpResult _jdbcWaitHigh = waitHighResults[0];
      waitHighS = _jdbcWaitHigh.getValue().toString();
    }
    if (!waitHighS.equals("")) {
      try {
        waitHighL = Long.parseLong(waitHighS);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    return waitHighL;
  }

  /**
   * 根据jdbc连接池实例获得该连接池中最大的等待时间
   * @param expressionName jdbc连接池实例
   * @return
   * @throws SnmpException
   */
  public long getWLS_JDBC_WAITSECONDSHIGHByInstanceKey(String instanceKey) throws SnmpException {
    String waitSecondS = "";
    long waitSecondL = 0;
    SnmpResult[] waitSecondResult = walk.snmpWalk(OID_WLS_JDBC_WAITSECONDSHIGH + instanceKey);
    if (waitSecondResult != null && waitSecondResult.length >= 1) {
      SnmpResult _jdbcWaitSecond = waitSecondResult[0];
      waitSecondS = _jdbcWaitSecond.getValue().toString();
    }
    if (!waitSecondS.equals("")) {
      try {
        waitSecondL = Long.parseLong(waitSecondS);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    return waitSecondL;
  }

  /**
   * 根据jdbc连接池实例获得该连接池的容量
   * @param expressionName jdbc连接池实例
   * @return
   * @throws SnmpException
   */
  public long getWLS_JDBC_MAXCAPACITYByInstanceKey(String instanceKey) throws SnmpException {
    String maxCapacityS = "";
    long maxCapacityL = 0;
    SnmpResult[] maxCapacityResults = walk.snmpWalk(OID_WLS_JDBC_MAXCAPACITY + instanceKey);
    if (maxCapacityResults != null && maxCapacityResults.length >= 1) {
      SnmpResult _jdbcMaxCapacity = maxCapacityResults[0];
      maxCapacityS = _jdbcMaxCapacity.getValue().toString();
    }
    if (!maxCapacityS.equals("")) {
      try {
        maxCapacityL = Long.parseLong(maxCapacityS);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    return maxCapacityL;
  }

  // 取得WEBLOGIC JTA 的信息
  public String getWlsJtaInfo(String oid) throws Exception {
    return SnmpWalkUtil.getFirstStringValue(walk, oid);
  }

  /**
   * 取得所有WebApp实例
   * @return 返回的List中是WebAppInstance对象
   * @throws SnmpException
   */
  public List<WebAppInstance> getALLWLS_WEBAPP_INSTANCE() throws SnmpException, SnmpNotFoundException {
    List appInstances = new ArrayList();
    String instanceIndex = "";
    String instanceKey = "";
    String appName = "";
    Long sessionCur = new Long(0);

    SnmpResult[] indexResults = walk.snmpWalk(OID_WLS_WEBAPP_INDEX);
    if (indexResults != null && indexResults.length >= 1) {
      for (int i = 0; i < indexResults.length; i++) {
        SnmpResult _jdbcIndex = indexResults[i];
        instanceIndex = _jdbcIndex.getOid().toString();
        instanceKey = instanceIndex.substring(OID_WLS_WEBAPP_INDEX.length());
        appName = getWebAppName(instanceKey);
        sessionCur = new Long(getWebAppOpenSessionCur(instanceKey));
        WebAppInstance app = new WebAppInstance(instanceKey, appName, sessionCur);
        appInstances.add(app);
      }
    }
    return appInstances;
  }

  /**
   * 获取WebApp名称
   * @param expressionName
   * @return
   * @throws SnmpNotFoundException
   * @throws SnmpException
   */
  public String getWebAppName(String instanceKey) throws SnmpException {
    return getStringByOidAndInstance(OID_WLS_WEBAPP_COMPONENTNAME, instanceKey);
  }

  /**
   * 获取WebApp当前session个数
   * @param expressionName
   * @return
   * @throws SnmpNotFoundException
   * @throws SnmpException
   */
  public long getWebAppOpenSessionCur(String instanceKey) throws SnmpException {
    return getLongByOidAndInstance(OID_WLS_WEBAPP_OPENSESSIONCUR, instanceKey);
  }

  /**
   * 获取WebApp的session最大值
   * @param expressionName
   * @return
   * @throws SnmpNotFoundException
   * @throws SnmpException
   */
  public long getWebAppOpenSessionHigh(String instanceKey) throws SnmpException {
    return getLongByOidAndInstance(OID_WLS_WEBAPP_OPENSESSIONHIGH, instanceKey);
  }

  /**
   * 获取WebApp的session总数
   * @param expressionName
   * @return
   * @throws SnmpNotFoundException
   * @throws SnmpException
   */
  public long getWebAppOpenSessionTotal(String instanceKey) throws SnmpException {
    return getLongByOidAndInstance(OID_WLS_WEBAPP_OPENSESSIONTOTAL, instanceKey);
  }

  /**
   * 根据OID和instanceKey取值
   * @param oid
   * @param expressionName
   * @return String
   * @throws SnmpException
   */
  public String getStringByOidAndInstance(String oid, String instanceKey) throws SnmpException {
    String valueS = "";
    SnmpResult[] pduResults = walk.snmpWalk(oid + instanceKey);
    if (pduResults != null && pduResults.length > 0) {
      SnmpResult result = pduResults[0];
      valueS = result.getValue().toString();
    }
    return valueS;
  }

  /**
   * 根据OID和instanceKey取值
   * @param oid
   * @param expressionName
   * @return long
   * @throws SnmpException
   */
  public long getLongByOidAndInstance(String oid, String instanceKey) throws SnmpException {
    String valueS = "";
    long valueL = 0;
    valueS = getStringByOidAndInstance(oid, instanceKey);
    if (!valueS.equals("")) {
      try {
        valueL = Long.parseLong(valueS);
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    }
    return valueL;
  }

  /*
   * public static void main(String args[]) throws Exception{ SnmpWalk walk = new SnmpWalk(SnmpAPI.SNMP_VERSION_1,
   * "192.168.0.231", 1161, "public");
   * 
   * SnmpWLSManager wlsM = new SnmpWLSManager(walk); List _list = wlsM.getALLWLS_WEBAPP_INSTANCE(); for(int i = 0; i <
   * _list.size(); i++){ WebAppInstance app = (WebAppInstance)_list.get(i);
   * System.out.println("==================================="); System.out.println(app.getAppName());
   * System.out.println(app.getCurSession()); System.out.println(app.getInstanceKey()); } }
   */

  public static void main(String args[]) throws Exception {
    SnmpWalk walk = new SnmpWalk(SnmpUtil.getVersion("v1"), "192.168.0.231", 1611, "public");

    SnmpWLSManager wlsM = new SnmpWLSManager(walk);
    /*
     * List _list = wlsM.getALLWLS_JDBC_INSTANCE(); System.out.println(_list.size());
     */
    System.out.println(wlsM.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TATOTAL));
    System.out.println(wlsM.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_RTINDEX));
    System.out.println(wlsM.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_RTNAME));
    System.out.println(wlsM.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TA_RB_APP));
    System.out.println(wlsM.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TA_RB_SYS));
    System.out.println(wlsM.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TA_RB_RC));
    walk.close();
    // System.exit(0);
  }

}
