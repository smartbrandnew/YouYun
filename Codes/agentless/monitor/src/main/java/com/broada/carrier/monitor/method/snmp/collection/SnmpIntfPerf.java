package com.broada.carrier.monitor.method.snmp.collection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.snmp.SnmpGet;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpTarget;
import com.broada.snmputil.SnmpValue;

public class SnmpIntfPerf {
  private static final Log logger = LogFactory.getLog(SnmpIntfPerf.class);

  public static final String IF_NUMBER = "ifNumber";

  public static final String IF_INDEX = "ifIndex";

  public static final String IF_DESCR = "ifDescr";

  public static final String IF_SPEED = "ifSpeed";

  public static final String IF_OPER_STATUS = "ifOperStatus";

  public static final String IF_INOCTETS = "ifInOctets";

  public static final String IF_OUTOTETS = "ifOutOctets";

  public static final String IF_INDISCARDS = "ifInDiscards";

  public static final String IF_OUTDISCARDS = "ifOutDiscards";

  public static final String IF_INERRORS = "ifInErrors";

  public static final String IF_OUTERRORS = "ifOutErrors";

  public static final String IF_INNUCASTPKTS = "ifInNUcastPkts";

  public static final String IF_OUTNUCASTPKTS = "ifOutNUcastPkts";

  public static final String IF_INUCASTPKTS = "ifInUcastPkts";

  public static final String IF_OUTUCASTPKTS = "ifOutUcastPkts";

  public static final String IF_NAME = "ifName";

  public static final String IF_HCINOCTETS = "ifHCInOctets";

  public static final String IF_HCINUCASTPKTS = "ifHCInUcastPkts";

  public static final String IF_HCINMULTICASTPKTS = "ifHCInMulticastPkts";

  public static final String IF_HCINBROADCASTPKTS = "ifHCInBroadcastPkts";

  public static final String IF_HCOUTOCTETS = "ifHCOutOctets";

  public static final String IF_HCOUTUCASTPKTS = "ifHCOutUcastPkts";

  public static final String IF_HCOUTMULTICASTPKTS = "ifHCOutMulticastPkts";

  public static final String IF_HCOUTBROADCASTPKTS = "ifHCOutBroadcastPkts";

  public static final String IF_HIGHSPEED = "ifHighSpeed";

  private static Map<String, String> oids = new HashMap<String, String>();

  {
    oids.put("ifNumber", ".1.3.6.1.2.1.2.1");
    oids.put("ifIndex", ".1.3.6.1.2.1.2.2.1.1");
    oids.put("ifDescr", ".1.3.6.1.2.1.2.2.1.2");
    oids.put("ifSpeed", ".1.3.6.1.2.1.2.2.1.5");
    oids.put("ifOperStatus", ".1.3.6.1.2.1.2.2.1.8");
    oids.put("ifInOctets", ".1.3.6.1.2.1.2.2.1.10");
    oids.put("ifOutOctets", ".1.3.6.1.2.1.2.2.1.16");
    oids.put("ifInDiscards", ".1.3.6.1.2.1.2.2.1.13");
    oids.put("ifOutDiscards", ".1.3.6.1.2.1.2.2.1.19");

    oids.put("ifInErrors", ".1.3.6.1.2.1.2.2.1.14");
    oids.put("ifOutErrors", ".1.3.6.1.2.1.2.2.1.20");
    oids.put("ifInNUcastPkts", ".1.3.6.1.2.1.2.2.1.12");
    oids.put("ifOutNUcastPkts", ".1.3.6.1.2.1.2.2.1.18");
    oids.put("ifInUcastPkts", ".1.3.6.1.2.1.2.2.1.11");
    oids.put("ifOutUcastPkts", ".1.3.6.1.2.1.2.2.1.17");

    oids.put("ifName", ".1.3.6.1.2.1.31.1.1.1.1");
    oids.put("ifHCInOctets", ".1.3.6.1.2.1.31.1.1.1.6");
    oids.put("ifHCInUcastPkts", ".1.3.6.1.2.1.31.1.1.1.7");
    oids.put("ifHCInMulticastPkts", ".1.3.6.1.2.1.31.1.1.1.8");
    oids.put("ifHCInBroadcastPkts", ".1.3.6.1.2.1.31.1.1.1.9");
    oids.put("ifHCOutOctets", ".1.3.6.1.2.1.31.1.1.1.10");
    oids.put("ifHCOutUcastPkts", ".1.3.6.1.2.1.31.1.1.1.11");
    oids.put("ifHCOutMulticastPkts", ".1.3.6.1.2.1.31.1.1.1.12");
    oids.put("ifHCOutBroadcastPkts", ".1.3.6.1.2.1.31.1.1.1.13");
    oids.put("ifHighSpeed", ".1.3.6.1.2.1.31.1.1.1.15");

  }
  
  private SnmpTarget target;


  public SnmpIntfPerf(SnmpTarget target) {
    this.target = target;
  }

  public Vector<Map<String, Object>> getIfPerf(String[] columns) throws Exception {
    if (columns == null || columns.length == 0 || !oids.keySet().containsAll(Arrays.asList(columns))) {
      throw new Exception("无效的选项");
    }
    Vector<Map<String, Object>> v = new Vector<Map<String, Object>>();
    int num = -1;
    num = getInNumber(target);

    if (num <= 0)
      throw new Exception("端口数为零");
    for (int i = 0; i < num; i++) {
      v.add(new HashMap<String, Object>(columns.length));
    }
    for (int i = 0; i < columns.length; i++) {
      String oid = (String) oids.get(columns[i]);
      if (!walk(target, oid, v, columns[i])) {
        throw new Exception("SNMP WALK失败");
      }
    }
    
    //整理Vector,去掉一些空的map,主要是避免端口数(ifNumber)与实际内容不一致导致的错误
		for (int i = num - 1; i >= 0; i--) {
			Map<String, Object> map = v.get(i);
			if (map == null || map.size() == 0) {
				v.remove(i);
			}
		}
    
    return v;

  }

  protected int getInNumber(SnmpTarget target) throws Exception {
    //target.setObjectID(".1.3.6.1.2.1.2.1.0");
		SnmpGet get = new SnmpGet();
		get.setSnmpTarget(target);
		SnmpResult result = get.snmpSynGet(".1.3.6.1.2.1.2.1.0");
    if (result == null) {
      throw new SnmpException("无法获取到设备的端口数.");
    }
    String num = result.getValue().toString();

    if (num == null)
      return 0;

    int inum = 0;
    try {
      inum = Integer.parseInt(num);
    } catch (Exception e) {
      logger.error("获取到的端口数非法:" + num + ",节点:" + target.getIp() + ".");
      throw new SnmpException("端口数:" + num + "非法.", e);
    }
    return inum;
  }


  protected boolean walk(SnmpTarget target, String oid, Vector<Map<String, Object>> v, String name) {
    SnmpWalk walk = new SnmpWalk(target);
    SnmpResult[] results = null;
    try {
      results = walk.snmpWalk(oid);
      //Snmp.walkTable(param, columns)
    } catch (SnmpException e) {
      logger.error("根据" + oid + "进行Walk失败.", e);
      //如果OID是.1.3.6.1.2.1.31开头的可以忽略,主要是老的设备不支持
      if(oid.startsWith(".1.3.6.1.2.1.31.")){
        return true;
      }else{
        return false;
      }
    }
    if (results == null) {
      //如果OID是.1.3.6.1.2.1.31开头的可以忽略,主要是老的设备不支持
      if(oid.startsWith(".1.3.6.1.2.1.31.")){
        return true;
      }
      return false;
    } else {
      for (int index = 0; index < results.length; index++) {
        Map<String, Object> m = v.get(index);
        if(m==null){
          if(logger.isDebugEnabled()){
            logger.debug("获取OID="+oid+"的值时不存在INDEX="+index+"的MAP,可能是端口数与实际的端口不一致.");
          }
          continue;
        } 
        SnmpValue value = results[index].getValue();
        if (value.isNumber())
        	m.put(name, value.toLong());
        else
        	m.put(name, value.toString());
      }
      return true;
    }
  }
}
