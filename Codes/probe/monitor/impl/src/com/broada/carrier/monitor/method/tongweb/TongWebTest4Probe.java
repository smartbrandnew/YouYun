package com.broada.carrier.monitor.method.tongweb;

import com.broada.carrier.monitor.impl.mw.tongweb.conn.TongWebConnManager;

import javax.naming.NamingException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TongWebTest4Probe {
  
  public String test(String host, TongWebMonitorMethodOption method){
    String ret = null;
    TongWebConnManager manager=new TongWebConnManager(host, method.getJndiName(), method.getPort());
    try {
      manager.init();
    } catch (NamingException e1) {
      return "测试TongWeb链接失败,IP:" + host + ",PORT:" + manager.getPort() + ",JNDI NAME=" + manager.getName() + " " + exception2String(e1);
    }finally{
      manager.close();
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
