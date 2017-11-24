package com.broada.carrier.monitor.impl.mw.tomcat.basic;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.tomcat.AbstractTomcatManager;
import com.broada.carrier.monitor.impl.mw.tomcat.PerfItemMap;
import com.broada.carrier.monitor.method.tomcat.TomcatMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.*;

public class TomcatBasicMonitor extends BaseMonitor {
  protected static final Log logger = LogFactory.getLog(TomcatBasicMonitor.class);
  public final static String SERVER_INFO_URL = "http://{0}:{1}/manager/status";

  public static final String[] CONDITION_NAME = { "_reply" };

  private static Map<String,String> CONDITION_MSG = new HashMap<String,String>();
  static {
    CONDITION_MSG.put("_reply", "Tomcat响应时间");
  }

  private static Map<String,String> MAPINFO = new HashMap<String,String>();
  static {
    MAPINFO.put("tomcatVersion", "TOMCAT-BASIC-1");
    MAPINFO.put("jvmVersion", "TOMCAT-BASIC-2");
    MAPINFO.put("jvmVendor", "TOMCAT-BASIC-3");
    MAPINFO.put("osName", "TOMCAT-BASIC-4");
    MAPINFO.put("osVersion", "TOMCAT-BASIC-5");
    MAPINFO.put("osArchitecture", "TOMCAT-BASIC-6");
    MAPINFO.put("_reply", "TOMCAT-BASIC-7");
  }

  @Override public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

    boolean state = true;
    Map<String, String> conditionMap = new HashMap<String, String>();
    AbstractTomcatManager tm = new TomcatBasicManager();

    TomcatMonitorMethodOption option = new TomcatMonitorMethodOption(context.getMethod());
    //连接参数的获取
    String ip = context.getNode().getIp();
    int port = option.getPort();
    String user = option.getUsername();
    String password = option.getPassword();

    logger.debug("Tomcat监测器:端口=" + port + ",用户名=" + user + ",密码=" + password);

    String absUrl = MessageFormat.format(SERVER_INFO_URL, new Object[] { ip, port + "" });

    long reponseTime = 0;
    GetMethod get = null;
    int statusCode = 0;
    long startResponse = System.currentTimeMillis();
    try {
      //向tomcat发出请求
      get = tm.fetchResponse(absUrl, user, password);
      reponseTime = System.currentTimeMillis() - startResponse;
      if(reponseTime<=0){
        reponseTime=1;
      }
      conditionMap.put(CONDITION_NAME[0], reponseTime + "");
      statusCode = get.getStatusCode();
      result.setResponseTime(reponseTime);
    } catch (IllegalArgumentException e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("无效的URL:" + absUrl+".");
      return result;
    } catch (ConnectTimeoutException e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("连接目标地址" + ip + "超时.");
      return result;
    } catch (ConnectException e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("无法连接到" + ip + "的" + port + "端口.");
      return result;
    } catch (SocketTimeoutException e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("成功连接端口:" + port + ",但读取数据超时或HTTP协议错误.");
      return result;
    } catch (IOException e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("IO错误:"+e.getMessage());
      return result;
    } catch (Throwable t) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("未知错误:"+t.getMessage());
      return result;
    }

    if (statusCode != 200) {
      result.setResultDesc("回应码不等于200(" + statusCode + ").");
      result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      return result;
    }

    TomcatBasic tomcatBasic=null;
    try {
      //取得tomcat监测数据
      tomcatBasic = (TomcatBasic) tm.fetchInfo(get.getResponseBodyAsStream());
    } catch (Throwable t) {
      result.setResultDesc("基本参数获取失败.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    long replyTime = System.currentTimeMillis() - startResponse;
    if (replyTime <= 0)
      replyTime = 1L;
    result.setResponseTime(replyTime);
    
    List<PerfItemMap> nameForIndex = new ArrayList<PerfItemMap>();
    BeanMap bm = new BeanMap(tomcatBasic);
    for (Iterator it = bm.keyIterator(); it.hasNext();) {
      String key = (String) it.next();
      if (MAPINFO.containsKey(key)) {
        PerfItemMap perfItemMap = new PerfItemMap(MAPINFO.get(key),key,bm.get(key));
        nameForIndex.add(perfItemMap);
      }
    }
    nameForIndex.add(new PerfItemMap(MAPINFO.get("_reply"), "_reply", new Double(reponseTime)));
    
    //装配性能参数
    result.setPerfResults(tm.assemblePerf(nameForIndex));

    if (!state) {
      result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      result.setResultDesc(tm.getResultDesc().toString());
    } else {
      result.setState(MonitorConstant.MONITORSTATE_NICER);
      result.setResultDesc("监测一切正常");
    }

    return result;
  }
}
