package com.broada.carrier.monitor.method.resin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.broada.carrier.monitor.impl.mw.resin.ResinMonitorManager;
import com.broada.carrier.monitor.impl.mw.resin.ResinMonitorUtil;
import com.broada.utils.ListUtil;

public class ResinJMXTest4Probe {
  public String test(String host, ResinJMXOption option){
    String ret = null;
    ret = testHostAndPort(host,option.getPort());
    if (null != ret) {
      return ret;
    }

    String url = null; // Resin管理Url
    try {
      url = ResinMonitorUtil.getAgentUrl(host, option);
    } catch (UnsupportedEncodingException e) {
      return "测试Resin连接失败：\n请检查代理名称的正确性。" + exception2String(e);
    }

    ResinMonitorManager manager = new ResinMonitorManager();
    try {
      manager.getBaseInfoByUrl(url);
    } catch (Exception e) {
      return "测试Resin连接失败：\n请检查代理部署的正确性。" + exception2String(e);
    }
    if (null != option.getWebAppRoot()) {
      try {
        List webApps = manager.getFirstWebAppsByUrl(url, option.getWebAppRoot());
        if (ListUtil.isNullOrEmpty(webApps)) {
          return "测试Resin连接失败：\n请检查Web应用根路径的正确性。";
        }
      } catch (Exception e) {
        return "测试Resin连接失败：\n请检查代理部署的正确性。" + exception2String(e);
      }
    }
    return null;
  }
  
  private String testHostAndPort(String host,int port) {
    // 组建访问Resin的URL
    StringBuffer url = new StringBuffer("http://");
    url.append(host);
    url.append(":");
    url.append(port);

    HttpClient httpClient = new HttpClient();
    GetMethod getMethod = new GetMethod(url.toString());
    getMethod.getParams().setSoTimeout(3 * 1000); // 1s
    int statusCode = 0;

    try {
      statusCode = httpClient.executeMethod(getMethod);
    } catch (Exception e) {
      return "测试Resin连接失败：\n网络不通或者目标主机上的Resin没有启动。" + exception2String(e);
    }

    // 如果HTTP请求的返回码不正常
    if (statusCode != HttpStatus.SC_OK) {
      return "测试Resin连接失败：\n网络不通或者目标主机上的Resin没有启动。";
    }

    return null;
  }
  
  public static String exception2String(Throwable ex){
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(bos);
    ex.printStackTrace(ps);
    ps.close();
    return new String(bos.toByteArray());
  }

}
