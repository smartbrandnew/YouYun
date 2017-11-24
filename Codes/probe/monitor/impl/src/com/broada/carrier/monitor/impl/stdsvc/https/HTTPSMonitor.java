package com.broada.carrier.monitor.impl.stdsvc.https;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * HTTPS服务 监听器实现类
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class HTTPSMonitor implements Monitor {

  private static final Log logger = LogFactory.getLog(HTTPSMonitor.class);

  /*单行去除HTML标志的正则表达式*/
  /*有个问题，中文不能匹配*/
  public static final String REGEX_LINE = "<[^>]+>|&nbsp;|[ \t\f\\v]";

  /*全部去除HTML标志的正则表达式*/
  /*有个问题，中文不能匹配*/
  public static final String REGEX_ALL = "<[^>]+>|&nbsp;|[ \t\f\r\n\\v]";

  private static final String ITEMKDX_REPLYTIME = "HTTPS-1";// 响应时间

  public HTTPSMonitor() {
  }

  /**
   * 实现监测,当前使用HttpClient包来完成HTTPS请求
   * 但当网站需要数字证书时就会监测不成功
   *
   * @param srv
   * @return
   */
  @SuppressWarnings("deprecation")
	public MonitorResult monitor(MonitorContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);    

    String param = context.getTask().getParameter();
    String ip = context.getNode().getIp();
    HTTPSParameter p = new HTTPSParameter(param);

    int port = p.getPort();
    String absUrl = p.getURL();
    String domain = p.getDomain();
    boolean chkDomain = p.isChkDomain();

    result.setState(MonitorConstant.MONITORSTATE_FAILING);

    //组合URL
    if (chkDomain) {
      ip = domain;
    }
    String httpUrl = "https://" + ip + ":" + port + absUrl;
    try {
      new URL(httpUrl);
    } catch (MalformedURLException ex) {
      //表示URL错误
      result.setResultDesc("无效的URL配置地址:" + absUrl);
      return result;
    }
    
    //设置自动接受许可文件
    Protocol myhttps = new Protocol("https",new BroadaSecureProtocolSocketFactory(), 443);
    Protocol.registerProtocol("https", myhttps);

    //创建一个client实例
    HttpClient client = new HttpClient();
    //设置socket超时30000ms！这是为了防止一些错误的配置
    //引起的阻塞，譬如这样的方式连接到一个ftp服务器，就会导致
    //正确连接但是无返回数据的情况，导致线程阻塞
    client.getParams().setSoTimeout(30000);
    //设置一个GET请求
    GetMethod get = null;
    try {//在实际获取的时候可能对路径内的特殊符号无法解码而造成异常，这里简单进行了处理
      get = new GetMethod(httpUrl);
    } catch (Exception e) {
      String errMsg = "无法获取URL地址(" + httpUrl + ")的页面数据.";
      if (logger.isDebugEnabled()) {
        logger.debug(errMsg, e);
      }
      result.setResultDesc(errMsg);
      return result;
    }
    //配置一个请求用户，对于部分服务器是必要的，譬如GOOGLE若是没有请求身份，将什么也不能得到
    NTCredentials upc = new NTCredentials();
    upc.setUserName(p.getUsername() == null ? "foo" : p.getUsername());
    upc.setPassword(p.getPassword() == null ? "bar" : p.getPassword());
    upc.setDomain(ip);
    upc.setHost(ip);

    //暂时没有域，p.getRealm一定为null
    client.getState().setCredentials(p.getRealm(), null, upc);
    //设置连接超时
    client.setConnectionTimeout(30000);
    double replyTime = 0;
    //通过客户端执行GET方法
    try {
      replyTime = System.currentTimeMillis();
      client.executeMethod(get);
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0) {
        replyTime = 1;
      }
      result.setResponseTime((int) replyTime);
      replyTime = replyTime / 1000;
    } catch (IllegalArgumentException e) {
      //url地址错误
      result.setResultDesc("无效的监测配置URL：" + absUrl + "，请确认地址是否可以访问.");
      return closeAndReturn(result, get, null);
    } catch (UnknownHostException e) {
      //无效地址
      result.setResultDesc("无效HTTP Web服务地址：" + ip + "，请确认地址是否存在.");
      return closeAndReturn(result, get, null);
    } catch (ConnectTimeoutException e) {
      //连接超时
      result.setResultDesc("系统连接目标地址" + ip + "时发生超时。");
      return closeAndReturn(result, get, null);
    } catch (ConnectException e) {
      //无法到达目标地址
      result.setResultDesc("系统无法连接到Web服务器" + ip + "的" + port + "端口，请确认是否可达.");
      return closeAndReturn(result, get, null);
    } catch (SocketTimeoutException e) {
      //读取数据超时
      result.setResultDesc("系统成功连接到Web服务器的" + port + "端口，但读取HTTPS数据超时或HTTPS协议有错误.");
      return closeAndReturn(result, get, null);
    } catch (SSLHandshakeException e) {
      //SSL错误
      result.setResultDesc("系统成功连接到Web服务器的" + port + "端口，但服务不支持SSL");
      return closeAndReturn(result, get, null);
    } catch (SSLException e) {
      //SSL错误
      result.setResultDesc("系统成功连接到Web服务器的" + port + "端口，但服务类型不是HTTPS");
      return closeAndReturn(result, get, null);
    } catch (IOException e) {
      //输入输出错误
      result.setResultDesc("系统访问" + httpUrl + "时发生错误，错误信息为未知的数据输入输出（IO）错误。");
      return closeAndReturn(result, get, null);
    } catch (Throwable e) {
      //未知错误
      result.setResultDesc("系统访问" + httpUrl + "时发生错误，错误信息为：" + e.getMessage());
      return closeAndReturn(result, get, null);
    }

    //  HttpURLConnection conn = null;
    int statusCode = get.getStatusCode();
    //需要用流的方式打开
    InputStream is = null;

    StringBuffer msg = new StringBuffer();
    //    msg.append("成功连接到端口:" + port + ".\n");
    boolean wonted = true;
    
    result.addPerfResult(new PerfResult(ITEMKDX_REPLYTIME, replyTime));
    if (p.isChkReplyTime()) {
      if (replyTime <= p.getReplyTime()) {
        wonted = wonted && true;
        msg.append("URL页面响应时间:" + replyTime + "秒。\n");
      } else {
        wonted = wonted && false;
        msg.append("URL页面的响应时间为：" + replyTime + "秒，已超监控设定的阈值" + p.getReplyTime() + "秒。\n");
      }
    }

    //判断是否校验返回状态码
    if (p.isChkStatusCode()) {
      if (statusCode == HttpURLConnection.HTTP_OK) {
        wonted = wonted && true;
        //        msg.append("回应码等于200.\n");
      } else {
        //不等于OK则返回
        //        msg.append("回应码不等于200(" + statusCode + ").\n");
        wonted = wonted && false;
        result.setResultDesc("URL的HTTP请求的回应码为" + statusCode + "，正常的回应码需要是200。\n");
        return closeAndReturn(result, get, is);
      }
    }
    //判断是否检查包含文本或不包含文本
    if (p.isChkContain() || p.isChkNotContain()) {
      try {
        is = get.getResponseBodyAsStream();
      } catch (IOException ex2) {
        result.setResultDesc("当前监测无法连接到Web服务的" + port + "端口。");
        return closeAndReturn(result, get, is);
      }
    } else {
      if (wonted) {
        result.setState(MonitorConstant.MONITORSTATE_NICER);
//        result.setResultDesc(MonitorConstant.MONITORSTATE_NICER_DESC);
        result.setResultDesc("HTTP URL " + httpUrl + "访问正常，页面响应时间为："
            + replyTime + "S。");
      } else {
        result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
        result.setResultDesc(msg.toString());
      }

      return closeAndReturn(result, get, is);
    }

    //为了优化速度，分开各种情况判断
    if (p.isChkContain() && p.isChkNotContain()) { //两个都检查
      String content = getAllNotHtmlContent(is);
      if (content.indexOf(p.getContain()) > -1) {
        wonted = wonted && true;
        //        msg.append("页面内容包含:" + p.getContain() + ".\n");
      } else {
        wonted = wonted && false;
        msg.append("URL（" + httpUrl + "）的页面内容未包含关键字“" + p.getContain() + "”.\n");
      }

      if (content.indexOf(p.getNotContain()) > -1) {
        wonted = wonted && false;
        msg.append("URL（" + httpUrl + "）页面访问正常，但页面内容包含关键字“" + p.getNotContain() + "”.\n");
      } else {
        wonted = wonted && true;
        //        msg.append("页面内容不包含:" + p.getNotContain() + ".\n");
      }
    } else if (p.isChkContain()) { //只检查包含，为了提高速度，采用逐行扫描
      if (chkContainByEach(is, p.getContain())) {
        wonted = wonted && true;
        //        msg.append("页面内容包含:" + p.getContain() + ".\n");
      } else {
        wonted = wonted && false;
        msg.append("URL（" + httpUrl + "）的页面内容未包含关键字“" + p.getContain() + "”.\n");
      }
    } else if (p.isChkNotContain()) { //只检查不包含，为了提高速度，采用逐行扫描
      if (chkContainByEach(is, p.getNotContain())) {
        wonted = wonted && false;
        msg.append("URL（" + httpUrl + "）页面访问正常，但页面内容包含关键字“" + p.getNotContain() + "”.\n");
      } else {
        wonted = wonted && true;
        //        msg.append("页面内容不包含:" + p.getNotContain() + ".\n");
      }
    }

    if (wonted) {
      result.setState(MonitorConstant.MONITORSTATE_NICER);
//      result.setResultDesc(MonitorConstant.MONITORSTATE_NICER_DESC);
      result.setResultDesc("HTTP URL " + httpUrl + "访问正常，页面响应时间为：" + replyTime + "S.");
    } else {
      result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      result.setResultDesc(msg.toString());
    }

    return closeAndReturn(result, get, is);
  }

  /**
   * 获取得文件所有内容
   * @param is
   * @return
   */
  private String getAllContent(InputStream is) {
    StringBuffer sb = new StringBuffer();
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
      String str = reader.readLine();
      while (str != null) {
        sb.append(str + "\n");
        str = reader.readLine();
      }
    } catch (IOException ex) {
    }
    return sb.toString();
  }

  /**
   * 获取得去除HTML标志或换行等字符后的文件内容
   * @param is
   * @return
   */
  private String getAllNotHtmlContent(InputStream is) {
    return getAllContent(is).replaceAll(REGEX_ALL, "");
  }

  /**
   * 判断是否包含某个文本
   *
   * 为了优化速度，采用逐行判断的办法（但损失精确度)
   *
   * @param is
   * @param txt
   * @return
   */
  private boolean chkContainByEach(InputStream is, String txt) {
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is, "GBK"));
      String str = reader.readLine();
      while (str != null) {
        str = str.replaceAll(REGEX_LINE, ""); //去掉HTML标志
        if (str.indexOf(txt) > -1) {
          return true;
        }
        str = reader.readLine();
      }
    } catch (IOException ex) {
      return false;
    }
    return false;
  }

  /**
   * 关闭并返回
   * @param mr
   * @param get
   * @param is
   * @return
   */
  private MonitorResult closeAndReturn(MonitorResult mr, GetMethod get, InputStream is) {
    close(get, is);
    return mr;
  }

  /**
   * 关闭连接
   * @param conn
   * @param is
   */
  private void close(GetMethod get, InputStream is) {
    try {
      if (is != null) {
        is.close();
      }
      if (get != null) {
        get.releaseConnection();
      }
    } catch (Exception ex) {
    }
  }
  
  @Override
	public Serializable collect(CollectContext context) {
		return null;
	}
}