package com.broada.carrier.monitor.impl.storage.netapp.info;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpValue;

/**
 * 
 * @author Shoulw (shoulw@broada.com.cn) Create By 2016-5-9 上午11:49:06
 */
public  class SnmpHostInfoMgr {
  private static final Log logger = LogFactory.getLog(SnmpHostInfoMgr.class);

  //系统描述OID
  protected static final String OID_SYS_DESC = ".1.3.6.1.2.1.1.1.0";
  
  //主机名称OID
  protected static final String OID_SYS_NAME = ".1.3.6.1.2.1.1.5.0";

  //主机所在地址OID
  protected static final String OID_SYS_LOCATION = ".1.3.6.1.2.1.1.6.0";

  //主机内存大小OID
//  protected static final String OID_MEM_SIZE = ".1.3.6.1.2.1.25.2.2.0";

  //主机处理器个数OID
//  protected static final String OID_PROCESS_COUNT = ".1.3.6.1.2.1.25.3.3.1.1";

  protected int version;
  
  protected String ipAddr;
  
  protected int port;
  
  protected String community;
  
  protected int timeout;

  protected SnmpWalk walk = null;

  public SnmpHostInfoMgr(int version, String ipAddr, int port, String community, int timeout) {
    this.version = version;
    this.ipAddr = ipAddr;
    this.port = port;
    this.community = community;
    this.timeout = timeout;
  }
  
  public SnmpHostInfoMgr(SnmpWalk walk) {
	  this.walk = walk;
	  }
  
  
  public void initSnmpWalk(){
    if(walk == null){
      walk = new SnmpWalk(version, ipAddr, port, community == null ? "public" : community);
      walk.setTimeout(timeout);
    }
  }
  
  public void close() {
    if (walk != null)
      walk.close();
  }

  /**
   * 主机信息获取
   * 
   * @return
   * @throws Exception 
   */
  public Map generateHostInfo() throws Exception {
    
    Map hostInfo = new HashMap();
    // 主机版本信息获取
//    String softwareInfo = generateSoftWareInfo();
//    hostInfo.put(HostBaseInfo.keys[0], StringUtil.convertNull2Blank(softwareInfo));
    // 主机机器型号信息获取
//    String hardwareInfo = generateHardWareInfo();
//    hostInfo.put(NETAPPInfo.keys[1], StringUtil.convertNull2Blank(hardwareInfo));
    
    //设备概况
    hostInfo.put(HostBaseInfo.keys[0],getDevInfo() );
    // 主机名获取
    hostInfo.put(HostBaseInfo.keys[1], getHostName());
    // 主机所在物理地址获取
    hostInfo.put(HostBaseInfo.keys[2], getSysLocation());
    // 主机内存大小(MB)
//    NumberFormat formatter = NumberFormat.getInstance();
//    formatter.setMaximumFractionDigits(2);
//    hostInfo.put(NETAPPInfo.keys[4], new BigDecimal(getMemSize()).setScale(2, 4).doubleValue());
    // 主机CPU个数获取
//    hostInfo.put(NETAPPInfo.keys[5], new Integer(getProcessCount()));
    return hostInfo;
  }
  /**
   * 设备概况
   */
  
  /**
   * 获取主机名
   * @return
   * @throws Exception 
   */
  private String getDevInfo() throws Exception {
    String hostName = "";
    try {
      SnmpValue snmpValue = getStrValueByOid(OID_SYS_DESC);
      if(snmpValue != null){
        hostName = snmpValue.toString();
      }
    } catch (SnmpException e) {
      logger.error("设备概况获取失败。", e);
      throw new Exception("设备概况获取失败，" + e.getMessage() + "。", e);
    }
    return hostName;
  }
  /**
   * 获取主机名
   * @return
   * @throws Exception 
   */
  private String getHostName() throws Exception {
    String hostName = "";
    try {
      SnmpValue snmpValue = getStrValueByOid(OID_SYS_NAME);
      if(snmpValue != null){
        hostName = snmpValue.toString();
      }
    } catch (SnmpException e) {
      logger.error("主机名获取失败。", e);
      throw new Exception("主机名获取失败，" + e.getMessage() + "。", e);
    }
    return hostName;
  }
  
  private String getSysLocation() throws Exception {
    String location = "";
    try {
      SnmpValue snmpValue = getStrValueByOid(OID_SYS_LOCATION);
      if (snmpValue != null) {
        snmpValue.isNull();
        location = snmpValue.toString();
      }
    } catch (SnmpException e) {
      logger.error("物理地址失败。", e);
      throw new Exception("物理地址失败，" + e.getMessage() + "。", e);
    }
    return location;
  }
  
//  private double getMemSize() throws Exception {
//    double memSize = 0;
//    try {
//      SnmpValue snmpValue = getStrValueByOid(OID_MEM_SIZE);
//      if (snmpValue != null) {
//        memSize = snmpValue.toLong()/1024d;
//      }
//    } catch (SnmpException e) {
//      logger.error("物理内存大小失败。", e);
//      throw new Exception("物理内存大小失败，" + e.getMessage() + "。", e);
//    }
//    return memSize;
//  }
  
//  private int getProcessCount() throws Exception{
//    SnmpResult[] results = null;
//    try {
//      results = walk.snmpWalk(OID_PROCESS_COUNT);
//    } catch (SnmpException e) {
//      logger.error("处理器个数失败。", e);
//      throw new Exception("处理器个数失败，" + e.getMessage() + "。", e);
//    }
//    if (results == null) {
//      return 0;
//    } else {
//      return results.length;
//    }
//  }
  
  protected SnmpValue getStrValueByOid(String oid) throws SnmpException {
    SnmpResult var = null;
    try {
      var = walk.snmpGet(oid);
    } catch (SnmpException e) {
      throw e;
    }
    if (var != null) {
      return var.getValue();
    } else {
      return null;
    }
  }

}
