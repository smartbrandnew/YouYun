package com.broada.carrier.monitor.impl.host.snmp.info;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.Snmp;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpOID;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpRow;
import com.broada.snmputil.SnmpTable;
import com.broada.snmputil.SnmpTarget;
import com.broada.snmputil.SnmpValue;
import com.broada.utils.StringUtil;

/**
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2007-12-2 上午11:49:06
 */
public abstract class SnmpHostInfoMgr {
  private static final Log logger = LogFactory.getLog(SnmpHostInfoMgr.class);

  // 系统描述OID
  protected static final String OID_SYS_DESC = ".1.3.6.1.2.1.1.1.0";

  // 主机名称OID
  protected static final String OID_SYS_NAME = ".1.3.6.1.2.1.1.5.0";

  // 主机所在地址OID
  protected static final String OID_SYS_LOCATION = ".1.3.6.1.2.1.1.6.0";

  // 主机内存大小OID
  protected static final String OID_MEM_SIZE = ".1.3.6.1.2.1.25.2.2.0";

  // 主机处理器个数OID
  protected static final String OID_PROCESS_COUNT = ".1.3.6.1.2.1.25.3.3.1.1";

  // 主机物理地址
  protected static final String OID_PHYSICAL_ADDRESS = ".1.3.6.1.2.1.2.2.1.6";
  
  protected static final String OID_IF_DESCR = ".1.3.6.1.2.1.2.2.1.2";

  // 主机本地接口索引值
  protected static final String OID_IP_ROUTE_INTERFACE_INDEX = ".1.3.6.1.2.1.4.21.1.2";

  // 主机IP映射到物理地址
  protected static final String OID_IP_NET_TO_MEDIA_PHYSICAL_ADDRESS = ".1.3.6.1.2.1.4.22.1.2";
  
  protected static final SnmpOID[] OID_INTF = new SnmpOID[] {new SnmpOID(OID_IF_DESCR), new SnmpOID(OID_PHYSICAL_ADDRESS)};
  protected static final SnmpOID OID_IP_IF_INDEX = new SnmpOID("1.3.6.1.2.1.4.20.1.2");

  protected String ip;
  protected SnmpMethod method;
  protected SnmpWalk walk = null;

  public SnmpHostInfoMgr(String ip, SnmpMethod method) {
    this.ip = ip;
    this.method = method;
  }

  public void initSnmpWalk() {
    if (walk == null) 
      walk = new SnmpWalk(method.getTarget(ip));
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
    String softwareInfo = generateSoftWareInfo();
    hostInfo.put(HostBaseInfo.keys[0], StringUtil.convertNull2Blank(softwareInfo));
    // 主机机器型号信息获取
    String hardwareInfo = generateHardWareInfo();
    hostInfo.put(HostBaseInfo.keys[1], StringUtil.convertNull2Blank(hardwareInfo));
    // 主机名获取
    hostInfo.put(HostBaseInfo.keys[2], getHostName());
    // 主机内存大小(MB)
    double value = getMemSize();
    if (value > 0) 
    	hostInfo.put(HostBaseInfo.keys[4], new BigDecimal(value).setScale(2, 4).doubleValue());    
    // 主机CPU个数获取
    hostInfo.put(HostBaseInfo.keys[5], new Integer(getProcessCount()));
    
    hostInfo.put(HostBaseInfo.keys[6], getIntfList());
    // 主机所在物理地址获取
    hostInfo.put(HostBaseInfo.keys[3], getMinMacAddr((String)hostInfo.get(HostBaseInfo.keys[6])));    
    return hostInfo;
  }

  private static String getMinMacAddr(String intfList) {
		if (intfList == null) 
			return null;		
				
		String minMac = null;
		String[] intfs = intfList.split(";");
		for (String intf : intfs) {
			String[] fields = intf.split("=");
			if (fields.length > 1) {
				String[] fields2 = fields[1].split("/");
				if (fields2.length > 1) {
					if (fields2[1].equals("00:00:00:00:00:00") || fields2[1].equals("FF:FF:FF:FF:FF:FF"))
						continue;
					if (minMac == null || fields2[1].compareTo(minMac) < 0)					
						minMac = fields2[1];
				}				
			}
		}
		
		return minMac;
	}

	private String getIntfList() throws Exception {  	  	
  	StringBuffer sb = new StringBuffer();
    try {
    	SnmpTarget target = method.getTarget(ip);
      SnmpTable intfList = Snmp.walkTable(target, OID_INTF);
      
      SnmpResult[] ips = Snmp.walk(target, OID_IP_IF_INDEX);
      
      for (Object o : intfList.getRows()) {
      	SnmpRow row = (SnmpRow)o;
      	      	
      	long ifIndex = row.getInstance().getValue()[0];
      	String mac = "";
      	byte[] macByte = row.getCell(1).getValue().toByteArray();
      	if (macByte != null && macByte.length == 6) {
      		mac = String.format("%02X:%02X:%02X:%02X:%02X:%02X", 
      				macByte[0], macByte[1], macByte[2], macByte[3], macByte[4], macByte[5]);
      	} else
      		continue;
      	
      	boolean hadIP = false;
      	for (int i = 0; i < ips.length; i++) {
      		if (ips[i].getValue().toLong() == ifIndex) {
      			int ipStart = ips[i].getOid().getValue().length - 4;
      			int[] oids = ips[i].getOid().getValue();
      			if (sb.length() > 0)
      				sb.append(";");
      			sb.append(row.getCell(0).getValue().toText()).append("=");
      			sb.append(oids[ipStart]).append(".");
      			sb.append(oids[ipStart + 1]).append(".");
      			sb.append(oids[ipStart + 2]).append(".");
      			sb.append(oids[ipStart + 3]);      			
      			sb.append("/").append(mac);
      			hadIP = true;
      		}
      	}      	
      	
      	if (!hadIP) {
      		if (sb.length() > 0)
    				sb.append(";");
    			sb.append(row.getCell(0).getValue().toText()).append("=");
    			sb.append("/").append(mac);
      	}
      }      
    } catch (Exception e) {
      logger.error("获取IntfList失败。", e);
      throw new Exception("获取IntfList失败，" + e.getMessage() + "。", e);
    }
    return sb.toString();
	}

	/**
   * 获取主机名
   * 
   * @return
   * @throws Exception
   */
  private String getHostName() throws Exception {
    String hostName = "";
    try {
      SnmpValue snmpValue = getStrValueByOid(OID_SYS_NAME);
      if (snmpValue != null) {
        hostName = snmpValue.toString();
      }
    } catch (SnmpException e) {
      logger.error("主机名获取失败。", e);
      throw new Exception("主机名获取失败，" + e.getMessage() + "。", e);
    }
    return hostName;
  }

  private double getMemSize() throws Exception {
    double memSize = 0;
    try {
      SnmpValue snmpValue = getStrValueByOid(OID_MEM_SIZE);
      if (snmpValue != null && snmpValue.isNumber()) {
        memSize = snmpValue.toLong() / 1024d;
      }
    } catch (SnmpException e) {
      logger.error("物理内存大小失败。", e);
      throw new Exception("物理内存大小失败，" + e.getMessage() + "。", e);
    }
    return memSize;
  }

  private int getProcessCount() throws Exception {
    SnmpResult[] results = null;
    try {
      results = walk.snmpWalk(OID_PROCESS_COUNT);
    } catch (SnmpException e) {
      logger.error("处理器个数失败。", e);
      throw new Exception("处理器个数失败，" + e.getMessage() + "。", e);
    }
    if (results == null) {
      return 0;
    } else {
      return results.length;
    }
  }

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

  /**
   * 子类实现该方法返回主机系统版本信息
   * 
   * @return
   */
  protected abstract String generateSoftWareInfo() throws Exception;

  /**
   * 子类实现该方法返回主机机器型号信息
   * 
   * @return
   */
  protected abstract String generateHardWareInfo() throws Exception;
}
