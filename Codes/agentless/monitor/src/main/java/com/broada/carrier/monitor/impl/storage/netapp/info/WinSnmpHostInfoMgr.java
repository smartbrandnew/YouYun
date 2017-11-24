package com.broada.carrier.monitor.impl.storage.netapp.info;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.mp.SnmpConstants;

import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpValue;
import com.broada.utils.StringUtil;

/**
 * 
 * @author shoulw (shoulw@broada.com.cn) Create By 2016-5-9 下午03:18:48
 */
public class WinSnmpHostInfoMgr extends SnmpHostInfoMgr {
	  private static final Log logger = LogFactory.getLog(WinSnmpHostInfoMgr.class);
	  
	  private String sysDesc = null;

	  public WinSnmpHostInfoMgr(int version, String ipAddr, int port, String community, int timeout) {
	    super(version, ipAddr, port, community, timeout);
	  }

	  /*
	   * @see com.broada.srvmonitor.impl.host.snmphostinfo.SnmpHostInfoMgr#generateHardWareInfo()
	   */
	  protected String generateHardWareInfo() throws Exception {
	    if (sysDesc == null)
	      try {
	        sysDesc = getSysDesc();
	      } catch (SnmpException e) {
	        logger.error("机器型号信息获取失败。", e);
	        throw new Exception("机器型号获取失败，" + e.getMessage() + "。", e);
	      }
	    if (StringUtil.isNullOrBlank(sysDesc)) {
	      return "";
	    }
	    int startIdx = sysDesc.indexOf("Hardware: ") + "Hardware: ".length();
	    int endIdx = sysDesc.indexOf(" - ");
	    if (endIdx <= startIdx)
	      return "";
	    return sysDesc.substring(startIdx, endIdx);
	  }

	  /*
	   * @see com.broada.srvmonitor.impl.host.snmphostinfo.SnmpHostInfoMgr#generateSoftWareInfo()
	   */
	  protected String generateSoftWareInfo() throws Exception{
	    if (sysDesc == null)
	      try {
	        sysDesc = getSysDesc();
	      } catch (SnmpException e) {
	        logger.error("系统版本信息获取失败。", e);
	        throw new Exception("系统版本信息获取失败，" + e.getMessage() + "。", e);
	      }
	    if (StringUtil.isNullOrBlank(sysDesc)) {
	      return "";
	    }
	    int startIdx = sysDesc.indexOf(" - Software: ") + 13;
	    if (startIdx < 13)
	      return "";
	    return sysDesc.substring(startIdx);
	  }

	  private String getSysDesc() throws SnmpException {
	    String sysdesc = "";
	    SnmpValue snmpValue = getStrValueByOid(OID_SYS_DESC);
	    if (snmpValue != null) {
	      sysdesc = snmpValue.toString();
	    }
	    return sysdesc;
	  }
}
