package com.broada.carrier.monitor.method.websphere;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.mw.websphere.WASUtil;
import com.broada.carrier.monitor.impl.mw.websphere.conf.WebSphereGroupFacade;

public class WASTest4Probe {
  private static final Log logger = LogFactory.getLog(WASTest4Probe.class);

  public String test(String host, WASMonitorMethodOption method) {
    logger.debug("run websphere monitor test in probe");
    Map<String, String> params = new HashMap<String, String>();
    params.put("host", host);
    params.put("port", "" + method.getPort());
    params.put("username", method.getUsername());
    params.put("password", method.getPassword());
    params.put("connector_type", method.getConnectorType());
    params.put("connector_port", "" + method.getConnectorPort());
    params.put("connector_host", method.getConnectorHost());
    params.put("useSSL", String.valueOf(method.isChkSSL()));
    params.put("server_cer", method.getServerCerPath());
    params.put("client_key", method.getClientKeyPath());
    params.put("client_key_pwd", method.getClientKeyPwd());
    try {
      WASUtil.link(WebSphereGroupFacade.getDefaultLinkUrl(), params);
    } catch (Exception e1) {
      logger.error("无法连接到Websphere服务器或连接超时.",e1);
      return "无法连接到Websphere服务器或连接超时." + exception2String(e1);
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
