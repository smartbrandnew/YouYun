package com.broada.carrier.monitor.method.weblogic.agent;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import com.broada.carrier.monitor.impl.mw.weblogic.agent.basic.WLSBasicMonitorUtil;

public class WebLogicJMXTest4Probe {
	public String test(String host, Integer port, String agentName, String username, String password, Integer proxyPort,
			Boolean isCluster) {
    String ret = null;
    ret = testHostAndPort(host, port);
    if(null!=ret){
      return ret;
    }
    
    //2. 代理、用户名和密码的正确性
    WebLogicJMXOption option = new WebLogicJMXOption();
    option.setAgentName(agentName);
    option.setHost(host);
    option.setPort(port);
    option.setUsername(username);
    option.setPassword(password);
    option.setProxyPort(proxyPort);
    option.setIfCluster(isCluster);
    
    String url = null; //WebLogic管理Url
    
    try {
      url = WLSBasicMonitorUtil.getBaseInfoUrl(option);
    } catch (UnsupportedEncodingException e) {
      return "测试WebLogic连接失败：\n请检查代理名、用户名和密码的正确性。" + exception2String(e);
    }
    
    try {
      WLSBasicMonitorUtil.getServerInformation(url);
    } catch (Exception e) {
      String msg = e.toString();
      //由于原始异常被封装，所以只能从采用以下的方式来判定
      if (msg != null && msg.indexOf("FailedLoginException") != -1) {
        return "测试WebLogic连接失败：用户名或者密码不正确。" +  exception2String(e);
      } else {
        return "测试WebLogic连接失败：请检查代理名是否正确。" + exception2String(e);
      }
    }
    
    return null;
  }
  
  private String testHostAndPort(String host,int port) {
    //组建访问weblogic的URL
    StringBuffer url = new StringBuffer("http://");
    url.append(host);
    url.append(":");
    url.append(port);
    url.append("/console");
    
    HttpClient httpClient = new HttpClient();
    GetMethod getMethod = new GetMethod(url.toString());
    getMethod.getParams().setSoTimeout(3*1000);  //1s
    int statusCode = 0;
      
    
    try {
      statusCode = httpClient.executeMethod(getMethod);
    } catch (Exception e) {
      return "测试WebLogic连接失败：\n网络不通或者目标主机上的WebLogic没有启动。" +  exception2String(e);
    }
    //如果HTTP请求的返回码不正常
    if (statusCode != HttpStatus.SC_OK &&  statusCode!= 404) {//其中404是海南地税，集群模式下，返回404，展示用此方法屏蔽
      return "测试WebLogic连接失败：HTTP状态码:" + statusCode;
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
