package com.broada.carrier.monitor.impl.mw.tongweb.conn;

import com.broada.carrier.monitor.impl.mw.tongweb.AbstractTongWebManager;
import com.broada.carrier.monitor.impl.mw.tongweb.TongWebTable;

import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;


public class TongWebConnManager extends AbstractTongWebManager {

  /**
   * 这个变量用于保存上一次保存的值 
   */
  private static final Map lastValue = new HashMap();
  
  public TongWebConnManager(String _ipAddr,String _name,int _port) {
    super.setIpAddr(_ipAddr);
    super.setName(_name);
    super.setPort(_port);
  }

  public void setMBeanNameInfo(Map map, ObjectName name) {
    map.put("connectionid", name.getKeyProperty("connectionid"));
    map.put("type", name.getKeyProperty("type"));
  }

  public static void calculateRatio(TongWebConnInfo connInfo, String lastValueKey){
    TongWebTable lastTable = (TongWebTable) lastValue.get(lastValueKey);

    if(lastTable==null){
      //第一次监测
      connInfo.setBytesReceivedRatio(new Double(0));
      connInfo.setBytesSentRatio(new Double(0));
      connInfo.setThroughPutRatio(new Double(0));
    }else{
      TongWebConnInfo lastConnInfo = (TongWebConnInfo)lastTable.getTable();
      double margin = (System.currentTimeMillis() - lastTable.getTime())/1000;
      
      double currSent = connInfo.getKBytesSent().doubleValue();
      double lastSent = lastConnInfo.getKBytesSent().doubleValue();
      double currRecv = connInfo.getKBytesReceived().doubleValue();
      double lastRecv = lastConnInfo.getKBytesReceived().doubleValue();
      double currPut = connInfo.getThroughPut().doubleValue();
      double lastPut = lastConnInfo.getThroughPut().doubleValue();
      if(currSent<lastSent||currRecv<lastRecv||currPut<lastPut){
        //TongWeb有重启过
        lastSent = 0;
        lastRecv = 0;
        lastPut = 0;
      }
      
      // 收发字节单位KB/秒,吞吐量单位次/分钟
      connInfo.setBytesReceivedRatio(new Double((currRecv-lastRecv)/margin));
      connInfo.setBytesSentRatio(new Double((currSent-lastSent)/margin));
      connInfo.setThroughPutRatio(new Double((currPut-lastPut)/(margin/60)));
    }
    
    TongWebTable nowTable = new TongWebTable();
    nowTable.setTime(System.currentTimeMillis());
    nowTable.setTable(connInfo);
    lastValue.put(lastValueKey, nowTable);    
  }
}
