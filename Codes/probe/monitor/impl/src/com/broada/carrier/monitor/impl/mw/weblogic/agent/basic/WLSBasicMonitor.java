package com.broada.carrier.monitor.impl.mw.weblogic.agent.basic;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.WLSRemoteException;
import com.broada.carrier.monitor.method.weblogic.agent.WebLogicJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class WLSBasicMonitor extends BaseMonitor {
	private static final Logger logger = LoggerFactory.getLogger(WLSBasicMonitor.class);
  private static final String ITEMIDX_WLSBASIC_WEBLOGICVERSION = "WLSBASIC-1";

  private static final String ITEMIDX_WLSBASIC_STATE = "WLSBASIC-2";

  private static final String ITEMIDX_WLSBASIC_HEALTHSTATE = "WLSBASIC-3";

  private static final String ITEMIDX_WLSBASIC_LISTENSERVER = "WLSBASIC-4";

  private static final String ITEMIDX_WLSBASIC_LISTENPOR = "WLSBASIC-5";

  private static final String ITEMIDX_WLSBASIC_SSLPORT = "WLSBASIC-6";

  private static final String ITEMIDX_WLSBASIC_OPENDSOCKETS = "WLSBASIC-7";

  private static final String ITEMIDX_WLSBASIC_RESTARTTIMES = "WLSBASIC-8";

  private static final String ITEMIDX_WLSBASIC_CURRENTDIRECTORY = "WLSBASIC-9";

  private static final String ITEMIDX_WLSBASIC_HEAPSIZECURRENT = "WLSBASIC-10";

  private static final String ITEMIDX_WLSBASIC_HEAPFREECURRENT = "WLSBASIC-11";

  private static final String ITEMIDX_WLSBASIC_OSNAME = "WLSBASIC-12";

  private static final String ITEMIDX_WLSBASIC_OSVERSION = "WLSBASIC-13";

  private static final String ITEMIDX_WLSBASIC_JAVAVERSION = "WLSBASIC-14";

  private static final String ITEMIDX_WLSBASIC_JAVAVENDOR = "WLSBASIC-15";

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    result.setState(MonitorConstant.MONITORSTATE_NICER);
    WebLogicJMXOption option = new WebLogicJMXOption(context.getMethod());
    ServerInformation serverInformation = null;
    String url=null;
    try {
      url = getUrl(option);
    } catch (Exception e) {
      logger.error("获取Weblogic管理URL错误.",e);
      result.setState(MonitorConstant.MONITORSTATE_CANCEL);
      return result;
    }
    long respTime=System.currentTimeMillis();
    boolean noexception=false;
    try {
      serverInformation = WLSBasicMonitorUtil.getServerInformation(url);
      noexception=true;
    } catch (MalformedURLException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("不合法的URL:"+url, e);
      }
      result.setResultDesc("管理URL非法:"+url);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
    } catch (IOException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("获取基本信息数据失败,URL="+url, e);
      }
      result.setResultDesc("获取基本信息数据失败,可能是Weblogic服务没有启动或者网络无法连接.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } catch (SAXException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("数据解析错误.", e);
      }
      result.setResultDesc("数据获取成功,但解析时发生错误.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
    } catch (WLSRemoteException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("数据解析错误:"+e.getMessage(), e);
      }
      result.setResultDesc("数据获取成功,但解析时发生错误:"+e.getMessage());
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("获取数据时发生未知错误:"+e.getMessage(), e);
      }
      result.setResultDesc("获取数据时发生未知错误:"+e.getMessage());
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    //计算响应时间
    respTime=System.currentTimeMillis()-respTime;
    if(respTime<=0){
      respTime=1;
    }
    result.setResponseTime(respTime);
    if(!noexception){
      //如果发生异常则直接返回
      return result;
    }
      
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_WEBLOGICVERSION, serverInformation.getWeblogicVersion()));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_STATE, serverInformation.getState()));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_HEALTHSTATE, serverInformation.getHealthState()));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_LISTENSERVER, serverInformation.getListenAddress()));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_LISTENPOR, "" + serverInformation.getListenPort()));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_SSLPORT, "" + serverInformation.getSSLListenPort()));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_OPENDSOCKETS, "" + serverInformation.getOpenSocketsCurrentCount()));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_RESTARTTIMES, "" + serverInformation.getRestartsTotalCount()));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_CURRENTDIRECTORY, serverInformation.getCurrentDirectory()));
		result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_HEAPSIZECURRENT, serverInformation.getHeapSizeCurrent() / 1048576));
		result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_HEAPFREECURRENT, serverInformation.getHeapFreeCurrent() / 1048576));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_OSNAME, serverInformation.getOSName()));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_OSVERSION, serverInformation.getOSVersion()));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_JAVAVERSION, serverInformation.getJavaVersion()));
    result.addPerfResult(new PerfResult(ITEMIDX_WLSBASIC_JAVAVENDOR, serverInformation.getJavaVendor()));

    StringBuffer buffer = new StringBuffer();
    addProperty(buffer, "WebLogic版本", serverInformation.getWeblogicVersion());
    addProperty(buffer, "运行状态", serverInformation.getState());
    addProperty(buffer, "健康状况", serverInformation.getHealthState());
    addProperty(buffer, "服务器", serverInformation.getListenAddress());
    addProperty(buffer, "服务监听端口", "" + serverInformation.getListenPort());
    addProperty(buffer, "SSL端口", "" + serverInformation.getSSLListenPort());
    addProperty(buffer, "活动socket连接数", "" + serverInformation.getOpenSocketsCurrentCount());
    addProperty(buffer, "重启次数", "" + serverInformation.getRestartsTotalCount());
    addProperty(buffer, "当前目录", serverInformation.getCurrentDirectory());
    addProperty(buffer, "堆栈大小", "" + serverInformation.getHeapSizeCurrent() + "("
        + serverInformation.getHeapSizeCurrent() / 1048576 + "MB)");
    addProperty(buffer, "当前可用堆栈", "" + serverInformation.getHeapFreeCurrent() + "("
        + serverInformation.getHeapFreeCurrent() / 1048576 + "MB)");
    addProperty(buffer, "操作系统", serverInformation.getOSName());
    addProperty(buffer, "操作系统版本", serverInformation.getOSVersion());
    addProperty(buffer, "Java版本", serverInformation.getJavaVersion());
    addProperty(buffer, "JavaVendor", serverInformation.getJavaVendor());
    result.setResultDesc(buffer.toString());
    return result;
  }

  private String getUrl(WebLogicJMXOption webLogicJMXOption) throws Exception {

    return WLSBasicMonitorUtil.getBaseInfoUrl(webLogicJMXOption);
  }

  private void addProperty(StringBuffer buffer, String name, String value) {
    buffer.append(name).append(":").append(value).append("\n");
  }  
}
