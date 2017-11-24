package com.broada.carrier.monitor.method.tomcat;

import com.broada.carrier.monitor.impl.mw.tomcat.AbstractTomcatManager;
import com.broada.carrier.monitor.impl.mw.tomcat.basic.TomcatBasicManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.text.MessageFormat;

public class TomcatTest4Probe {
  private static final Log logger = LogFactory.getLog(TomcatTest4Probe.class);
  
  private static final String URL = "http://{0}:{1}/manager/status?XML=true";
  
  public String test(String host, TomcatMonitorMethodOption method){
    logger.debug("runing tomcat monitor test in probe");
    String ret = null;
    try {
      AbstractTomcatManager manager = new TomcatBasicManager();
      GetMethod get = manager.fetchResponse(MessageFormat.format(URL, new Object[] { host,String.valueOf(method.getPort()) }), method.getUsername(), method.getPassword());

      int status = get.getStatusCode();
      if (status != HttpURLConnection.HTTP_OK) {
        ret = "Http状态码不是200(HTTp_OK)";
      }
    } catch (Throwable t) {
      ret = exception2String(t);
    }
    return ret;
    
  }
  
  public static String exception2String(Throwable ex){
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(bos);
    ex.printStackTrace(ps);
    ps.close();
    return new String(bos.toByteArray());
  }

}
