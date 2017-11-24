package com.broada.carrier.monitor.impl.mw.tomcat.jvm;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.tomcat.AbstractTomcatManager;
import com.broada.carrier.monitor.impl.mw.tomcat.PerfItemMap;
import com.broada.carrier.monitor.impl.mw.tomcat.TomcatStatus;
import com.broada.carrier.monitor.method.tomcat.TomcatMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TomcatJVMMonitor extends BaseMonitor {
  private Logger logger = LoggerFactory.getLogger(TomcatJVMMonitor.class);
  public final static String JVM_INFO_URL = "http://{0}:{1}/manager/status?XML=true";

  public static final String[] CONDITION_NAME = { "_free","_total","_used" };

  private static Map<String,String> CONDITION_MSG = new HashMap<String,String>();
  static {
    CONDITION_MSG.put("_free", "可用内存数");
    CONDITION_MSG.put("_total", "内存总数");
    CONDITION_MSG.put("_used", "已用内存");
  }

  private static Map<String,String> MAPINFO = new HashMap<String,String>();
  static {
    MAPINFO.put("_free", "TOMCAT-JVM-1");
    MAPINFO.put("_total", "TOMCAT-JVM-2");
    MAPINFO.put("_used", "TOMCAT-JVM-3");
  }

  @Override public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

    boolean state = true;
    Map<String, String> conditionMap = new HashMap<String, String>();
    AbstractTomcatManager tm = new TomcatJVMManager();
    TomcatMonitorMethodOption option = new TomcatMonitorMethodOption(context.getMethod());
    //连接参数的获取
    String ip = context.getNode().getIp();
    int port = option.getPort();
    String user = option.getUsername();
    String password = option.getPassword();



    String absUrl = MessageFormat.format(JVM_INFO_URL, new Object[] { ip, port + "" });

    GetMethod get = null;
    int statusCode = 0;

    long replyTime = System.currentTimeMillis();
    try {
      //向tomcat发出请求
      get = tm.fetchResponse(absUrl, user, password);
      statusCode = get.getStatusCode();
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

    if(statusCode!=200){
      result.setResultDesc("回应码不等于200(" + statusCode + ").\n");
      result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      return result;
    }

    TomcatStatus ts = null;
    try {
      ts = (TomcatStatus)tm.fetchInfo(get.getResponseBodyAsStream());
    } catch (Throwable t) {
			logger.error("JVM使用情况获取失败.",t);
      result.setResultDesc("JVM使用情况获取失败.\n" + t.getStackTrace());
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    replyTime = System.currentTimeMillis() - replyTime;
    if (replyTime <= 0)
      replyTime = 1L;
    result.setResponseTime(replyTime);

    //  装配性能参数
    double free=ts.getJVMInfo().getFree()/(1024*1024);
    double total=ts.getJVMInfo().getTotal()/(1024*1024);
    double used = ts.getJVMInfo().getUsed()/(1024*1024);

    List<PerfItemMap> nameForIndex = new ArrayList<PerfItemMap>();
    nameForIndex.add(new PerfItemMap(MAPINFO.get("_free"),"_free",new Double(free)));
    nameForIndex.add(new PerfItemMap(MAPINFO.get("_total"),"_total",new Double(total)));
    nameForIndex.add(new PerfItemMap(MAPINFO.get("_used"), "_used", new Double(used)));

    result.setPerfResults(tm.assemblePerf(nameForIndex));
    
    //告警比较
    conditionMap.put(CONDITION_NAME[0], free+"");
    conditionMap.put(CONDITION_NAME[1], total+"");
    conditionMap.put(CONDITION_NAME[2], used+"");
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
