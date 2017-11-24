package com.broada.carrier.monitor.impl.stdsvc.http;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * HTTP 监听器实现类
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class HTTPMonitor implements Monitor {

  private static final Log logger = LogFactory.getLog(HTTPMonitor.class);

  /*单行去除HTML标志的正则表达式*/
  /*有个问题，中文不能匹配*/
  public static final String REGEX_LINE = "<[^>]+>|&nbsp;|[ \t\f\\v]";

  /*全部去除HTML标志的正则表达式*/
  /*有个问题，中文不能匹配*/
  public static final String REGEX_ALL = "<[^>]+>|&nbsp;|[ \t\f\r\n\\v]";

  private static final String ITEMKDX_REPLYTIME = "HTTP-1";// 响应时间
  
  public static final String REGEX_CHARSET = "<head>.*<meta\\s+[^>]*charset=([^'\"/>]*)['\"][^>]*/>.*</head>";
  
  /* 最后一次监测的URL */
  private String lastURL;
  /* 最后一次监测的页面的编码，和lastURL配合使用，用来提高效率 */
  private String lastCharset;

  public HTTPMonitor() {
  }

  /**
   * 实现监测,当前使用HttpClient包来完成HTTP请求
   * @param srv
   * @return
   */
  public MonitorResult monitor(MonitorContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(0);

    String param = context.getTask().getParameter();
    String ip = context.getNode().getIp();
    HTTPParameter p = new HTTPParameter(param);

    int port = p.getPort();
    String absUrl = p.getURL();
    String domain = p.getDomain();
    boolean chkDomain = p.isChkDomain();

    result.setState(MonitorConstant.MONITORSTATE_FAILING);

    //组合URL
    if (chkDomain) {
      ip = domain;
    }
    String httpUrl = "http://" + ip + ":" + port + absUrl;
    try {
      new URL(httpUrl);
    } catch (MalformedURLException ex) {
      //表示URL错误
      result.setResultDesc("无效的URL配置地址:" + absUrl);
      return result;
    }

    //设置一个GET请求
    GetMethod method = null;
    try {//在实际获取的时候可能对路径内的特殊符号无法解码而造成异常，这里简单进行了处理
      method = new GetMethod(httpUrl);
    } catch (Exception e) {
      String errMsg = "无法获取URL地址(" + httpUrl + ")的页面数据.";
      if (logger.isDebugEnabled()) {
        logger.debug(errMsg, e);
      }
      result.setResultDesc(errMsg);
      return result;
    }

    double replyTime = 0;//响应时间
    //通过客户端执行GET方法
    try {
      replyTime = httpClientLoadTime(result, method, httpUrl, p, ip);
    }catch(Throwable e){
      return closeAndReturn(result, method, null);
    }
      result.setResponseTime((int) replyTime);
      //以后修改成毫秒
      replyTime = replyTime / 1000.0;

    //  HttpURLConnection conn = null;
    int statusCode = method.getStatusCode();
    //需要用流的方式打开
    InputStream is = null;

    boolean wonted = true;
    StringBuffer msg = new StringBuffer();
    //    msg.append("成功连接到端口:" + port + ".\n");
    
    result.addPerfResult(new PerfResult(ITEMKDX_REPLYTIME, replyTime));
    if (p.isChkReplyTime()) {
      if (replyTime <= p.getReplyTime()) {
        wonted = wonted && true;
        msg.append("URL页面的响应时间为:" + replyTime + "秒。\n");
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
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        return closeAndReturn(result, method, is);
      }
    }
    
    String charset = null;
    String content = null;
    // 如果在上次监测已经判断过该页面的编码格式，直接取上次的编码格式
    if (lastURL != null && lastURL.equals(httpUrl) && lastCharset != null) {
      charset = lastCharset;
      method.getParams().setContentCharset(charset);
    } else {
      lastURL = httpUrl;
      lastCharset = null;
      // 从http头获取编码格式
      Header contentheader = method.getRequestHeader("Content-Type");
      if (contentheader != null) {
        HeaderElement[] values = contentheader.getElements();
        
        if (values.length == 1) {
          NameValuePair nv = values[0].getParameterByName("charset");
          if (nv != null)
            charset = nv.getValue();
        }
      }
      if (charset == null) {
        try {
          content = method.getResponseBodyAsString();
        } catch (IOException e) {
          result.setResultDesc("读取Web页面时发生错误。");
          result.setState(MonitorConstant.MONITORSTATE_FAILING);
          return closeAndReturn(result, method, is);
        }
        Pattern pattern = Pattern.compile(REGEX_CHARSET, Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find())
          charset = matcher.group(1);
        if (StringUtils.isNotBlank(charset)) {
          charset = charset.trim();
          method.getParams().setContentCharset(charset);
          content = null;
          try {
            content = method.getResponseBodyAsString();
          } catch (IOException e) {
            // this should not happen
          }
          lastCharset = charset;
        } else {
          charset = "UTF-8";
        }
      }
      lastCharset = charset;
    }

    //判断是否检查包含文本或不包含文本
    if (p.isChkContain() || p.isChkNotContain()) {
      try {
        is = method.getResponseBodyAsStream();
      } catch (IOException ex2) {
        result.setResultDesc("当前监测无法连接到Web服务的" + port + "端口。");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        return closeAndReturn(result, method, is);
      }
    } else {//两个都不检查,返回
      if (wonted) {//wonted 为false 说明超过了预设的响应时间
        result.setState(MonitorConstant.MONITORSTATE_NICER);
//        result.setResultDesc(MonitorConstant.MONITORSTATE_NICER_DESC);
        result.setResultDesc("HTTP URL " + httpUrl + "访问正常，页面响应时间为："
            + replyTime + "S.");
      } else {
        result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
        result.setResultDesc(msg.toString());
      }
      return closeAndReturn(result, method, is);
    }

    //为了优化速度，分开各种情况判断
    if (p.isChkContain() && p.isChkNotContain()) { //两个都检查
      if (content == null) {
        try {
          content = method.getResponseBodyAsString();
        } catch (IOException e) {
          result.setResultDesc("读取Web页面时发生错误。");
          result.setState(MonitorConstant.MONITORSTATE_FAILING);
          return closeAndReturn(result, method, is);
        }
      }
      content.replaceAll(REGEX_ALL, "");
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
      boolean matched;
      if (content != null) {
        matched = content.contains(p.getContain());
      } else {
        try {
          matched = chkContainByEach(is, p.getContain(), charset);
        } catch (IOException e) {
          result.setResultDesc("读取Web页面时发生错误。");
          result.setState(MonitorConstant.MONITORSTATE_FAILING);
          return closeAndReturn(result, method, is);
        }
      }
      wonted = wonted && matched;
      if (!matched)
        msg.append("URL（" + httpUrl + "）的页面内容未包含关键字“" + p.getContain() + "”.\n");
    } else if (p.isChkNotContain()) { //只检查不包含，为了提高速度，采用逐行扫描
      boolean matched;
      if (content != null) {
        matched = !content.contains(p.getNotContain());
      } else {
        try {
          matched = !chkContainByEach(is, p.getNotContain(), charset);
        } catch (IOException e) {
          result.setResultDesc("读取Web页面时发生错误。");
          result.setState(MonitorConstant.MONITORSTATE_FAILING);
          return closeAndReturn(result, method, is);
        }
      }
      wonted = wonted && matched;
      if (!matched)
        msg.append("URL（" + httpUrl + "）页面访问正常，但页面内容包含关键字“" + p.getNotContain() + "”.\n");
    }

    if (wonted) {//关键字检查
      result.setState(MonitorConstant.MONITORSTATE_NICER);
//      result.setResultDesc(MonitorConstant.MONITORSTATE_NICER_DESC);
      result.setResultDesc("HTTP URL " + httpUrl + "访问正常，页面响应时间为：" + replyTime + "S.");
    } else {
    	result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      result.setResultDesc(msg.toString());
    }

    return closeAndReturn(result, method, is);
  }
    
  @SuppressWarnings("deprecation")
  public long httpClientLoadTime(MonitorResult result, GetMethod method, String httpUrl, HTTPParameter p, String ip)
      throws Throwable {
    //创建一个client实例
    HttpClient client = new HttpClient();
    //设置socket超时30000ms！这是为了防止一些错误的配置
    //引起的阻塞，譬如这样的方式连接到一个ftp服务器，就会导致
    //正确连接但是无返回数据的情况，导致线程阻塞
    client.getParams().setSoTimeout(30000);

    //配置一个请求用户，对于部分服务器是必要的，譬如GOOGLE若是没有请求身份，将什么也不能得到
    //    UsernamePasswordCredentials upc =
    //        new UsernamePasswordCredentials("htmltest", "test");
    NTCredentials upc = new NTCredentials();
    upc.setUserName(p.getUsername() == null ? "foo" : p.getUsername());
    upc.setPassword(p.getPassword() == null ? "bar" : p.getPassword());
    upc.setDomain(ip);
    upc.setHost(ip);

    //暂时没有域，p.getRealm一定为null
    client.getState().setCredentials(p.getRealm(), null, upc);
    method.setDoAuthentication(true);
    //设置连接超时
    client.setConnectionTimeout(30000);
    //支持URL跳转
    method.setFollowRedirects(true);

    long startTime = System.currentTimeMillis();
    try {
      client.executeMethod(method);
    } catch (HttpException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      //url地址错误
      result.setResultDesc("无效的监测配置URL：" + p.getURL() + "，请确认地址是否可以访问.");
      throw e;
    } catch (UnknownHostException e) {
      //无效HTTP Web服务地址
      result.setResultDesc("无效HTTP Web服务地址：" + ip + "，请确认地址是否存在.");
      throw e;
    } catch (ConnectTimeoutException e) {
      //连接超时
      result.setResultDesc("系统连接目标地址" + ip + "时发生超时.");
      throw e;
    } catch (ConnectException e) {
      //无法到达目标地址
      result.setResultDesc("系统无法连接到Web服务器" + ip + "的" + p.getPort() + "端口，请确认是否可达.");
      throw e;
    } catch (SocketTimeoutException e) {
      //读取数据超时
      result.setResultDesc("系统成功连接到Web服务器的" + p.getPort() + "端口，但读取HTTP数据超时或HTTP协议有错误.");
//      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      throw e;
    } catch (IOException e) {
      //输入输出错误
      result.setResultDesc("系统访问" + httpUrl + "时发生错误，错误信息为未知的数据输入输出（IO）错误.");
      throw e;
    } catch (Throwable e) {
      //未知错误
      result.setResultDesc("系统访问" + httpUrl + "时发生错误，错误信息为：" + e.getMessage());
      e.printStackTrace();
      throw e;
    }
    startTime = System.currentTimeMillis() - startTime;
    if (startTime <= 0)
    	startTime = 1;
    return startTime;
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
  private boolean chkContainByEach(InputStream is, String txt, String charset) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
    try {
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