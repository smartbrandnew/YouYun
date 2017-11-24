package com.broada.carrier.monitor.method.snmp.collection.perfmon.discover;

import java.util.ArrayList;
import java.util.List;

import com.broada.snmputil.Snmp;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpOID;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpTarget;

public class DiscoverUtil {

  private static final String OID_SYSOBJID = ".1.3.6.1.2.1.1.2.0";
  private static final String OID_ENTERPRISESOID = ".1.3.6.1.4.1";

  /**
   * 根据指定的表达式，在目标结点上搜索其所有实例 
   * @param target
   * @param timeout 超时时间(毫秒）
   * @param instExp
   * @return
   * @throws SnmpException 当网络访问错误时抛出
   */
  public static String[] discoverInstances(SnmpTarget target,int timeout, String instExp) throws SnmpException {    
    SnmpOID request = new SnmpOID(instExp);
    try {
      List<String> ret = new ArrayList<String>();
      SnmpResult[] results = Snmp.walk(target, request);
      for (int i = 0; i < results.length; i++) {
        if (request.isChild(results[i].getOid())){
          ret.add(results[i].getOid().suboid(request.length()).toString());
        }
      }
      return (String[]) ret.toArray(new String[ret.size()]);
    } catch (SnmpException e) {
      throw e;
    }
  }

  /**
   * 获取产品号
   * @param node
   * @return -1 表示获取不到
   * @throws SnmpException 当网络访问错误时抛出
   */
  public static long getProducerCode(SnmpTarget target) throws SnmpException {
    String enOid = getSysObjectID(target);
    return parseProducerCode(enOid);
  }
  
  /**
   * 根据sysObjectID解析出ProducerCode
   * @param sysObjectID
   * @return -1 表示无法解析出有效的ProducerCode
   */
  public static long parseProducerCode(String sysObjectID){
    String enOid = sysObjectID;
    if (enOid == null) {
      return -1;
    }
    if (enOid.length() < OID_ENTERPRISESOID.length() + 1) {
      return -1;
    } else {
      enOid = enOid.substring(OID_ENTERPRISESOID.length());
      int idx = enOid.indexOf(".");
      if(idx == -1)
      	idx = enOid.length();
      enOid = enOid.substring(0, idx);
      return Long.parseLong(enOid);
    }
  }

  /**
   * 获取SysObjectID
   * @param node
   * @return null 表示无法获取
   * @throws SnmpException 当网络访问错误时抛出
   */
  public static String getSysObjectID(SnmpTarget target) throws SnmpException {
    SnmpOID request = new SnmpOID(OID_SYSOBJID);
    SnmpResult result = null;
    try {
    	result = Snmp.get(target, request);
      if (result == null) {
        return null;
      } else {
        return result.getValue().toString();
      }
    } catch (SnmpException e) {
      throw e;
    }
  }
}
