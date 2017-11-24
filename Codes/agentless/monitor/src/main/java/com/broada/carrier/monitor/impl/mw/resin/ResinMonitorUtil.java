package com.broada.carrier.monitor.impl.mw.resin;

import com.broada.carrier.monitor.method.resin.ResinJMXOption;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.UnsupportedEncodingException;


/**
 * Resin监测单元类
 * @author 杨帆
 * 
 */
public class ResinMonitorUtil {
  public static final String RESIN_PROXY_RATIO = "代理缓存命中率";

  public static final String RESIN_BLOCK_RATIO = "块缓存命中率";

  public static final String RESIN_INV_RATIO = "调用命中率";

  public static final String RESIN_AGENT_PATH = "/service/agentService";

  public static String getAgentUrl(String host,ResinJMXOption webLogicJMXOption) throws UnsupportedEncodingException {
    return getUrl(host, webLogicJMXOption.getPort(), webLogicJMXOption.getAgentName(),
        ResinMonitorUtil.RESIN_AGENT_PATH);
  }

  public static String getUrl(String ipAddress, int port, String agentName, String path)
      throws UnsupportedEncodingException {
    StringBuffer buffer = new StringBuffer("http://");
    buffer.append(ipAddress).append(":").append(port).append("/").append(agentName).append(path);
    return buffer.toString();
  }

  /**
   * 测试服务器IP或域名、端口的正确性。
   * @return 将异常信息返回,为空则表示连接成功
   */

  public static String testHostAndPort(String host,ResinJMXOption option) {
    // 组建访问Resin的URL
    StringBuffer url = new StringBuffer("http://");
    url.append(host);
    url.append(":");
    url.append(option.getPort());

    HttpClient httpClient = new HttpClient();
    GetMethod getMethod = new GetMethod(url.toString());
    getMethod.getParams().setSoTimeout(3 * 1000); // 1s
    int statusCode = 0;

    try {
      statusCode = httpClient.executeMethod(getMethod);
    } catch (Exception e) {
      return "Resin连接失败,网络不通或者目标主机上的Resin没有启动。";
    }

    // 如果HTTP请求的返回码不正常
    if (statusCode != HttpStatus.SC_OK) {
      return "Resin连接失败,网络不通或者目标主机上的Resin没有启动。";
    }

    return null;
  }
}
