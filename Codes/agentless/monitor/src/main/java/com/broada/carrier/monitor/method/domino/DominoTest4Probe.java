package com.broada.carrier.monitor.method.domino;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.ew.domino.basic.DominoUtil;

public class DominoTest4Probe {
  private static final Log logger = LogFactory.getLog(DominoTest4Probe.class);

  public String test(String host, DominoMonitorMethodOption option) {
    Session sess = null;
    try {
      try {
        sess = NotesFactory.createSession(host, option.getUsername(), option.getPassword());
      } catch (Exception ex) {
        String ior = DominoUtil.getIOR(host, option.getPort());// "IOR:0101ea002900000049444c3a6c6f7475732f646f6d696e6f2f636f7262612f494f626a6563745365727665723a312e3000000000010000000000000070000000010101000d00000031302e3133362e3231322e310000acf6310000000438353235363531612d656336382d313036632d656565302d303037653264323233336235004c6f7475734e4f490100010000000100000001000000140000000101ea0001000105000000000001010000000000";
        sess = NotesFactory.createSessionWithIOR(ior, option.getUsername(), option.getPassword());
      }
      sess.createName(sess.getServerName());
      return null;
    } catch (NotesException e) {
      return "连接Domino服务器出错,请检查配置!" + exception2String(e);
    } catch (IOException e) {
      return "通过" + option.getPort() + "端口获取IOR失败!" + exception2String(e);
    }finally{
      if(sess!=null){
        try {
          sess.recycle();
        } catch (NotesException e) {
          logger.error(e);
        }
      }
    }
  }

  public static String exception2String(Throwable ex) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(bos);
    ex.printStackTrace(ps);
    ps.close();
    return new String(bos.toByteArray());
  }

}
