package com.broada.carrier.monitor.impl.host.snmp.winservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;

/**
 * Windows服务监测工具类
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-10-12 下午03:32:03
 */
public class WinServiceUtil {
  public static final String OID_WIN_SERVICE_NAME = ".1.3.6.1.4.1.77.1.2.3.1.1";
  public static final String OID_WIN_SERVICE_STATE = ".1.3.6.1.4.1.77.1.2.3.1.3";

  private WinServiceUtil(){
    
  }
  
  /**
   * 获取目标设备所有运行的服务
   * @param walk
   * @return
   * @throws SnmpException
   */
  public static List getAllWinServices(SnmpWalk walk) throws SnmpException {
    List winServices = new ArrayList();
    Map winServiceStates = new HashMap();
    String oid = "";
    String state = "";

    SnmpResult[] pduStates = walk.snmpWalk(OID_WIN_SERVICE_STATE);
    if (pduStates != null && pduStates.length >= 1) {
      for (int i = 0; i < pduStates.length; i++) {
        oid = pduStates[i].getOid().toString();
        oid = oid.substring(OID_WIN_SERVICE_STATE.length());
        state = pduStates[i].getValue().toString();
        winServiceStates.put(oid, state);
      }
    }

    SnmpResult[] pduNames = walk.snmpWalk(OID_WIN_SERVICE_NAME);
    if (pduNames != null && pduNames.length >= 1) {
      for (int i = 0; i < pduNames.length; i++) {
        oid = pduNames[i].getOid().toString();
        oid = oid.substring(OID_WIN_SERVICE_NAME.length());
        String name = pduNames[i].getValue().toText("UTF-8");
        if (winServiceStates.containsKey(oid)) {
          WinService ws = new WinService();
          //mdf by maico 2007-10-12 key和name都统一修改为name值
          ws.setWinServiceKey(name);
          ws.setWinServiceName(name);
          ws.setWinServiceState(new Integer((String) winServiceStates.get(oid)));
          winServices.add(ws);
        }
      }
    }

    return winServices;
  }
}
