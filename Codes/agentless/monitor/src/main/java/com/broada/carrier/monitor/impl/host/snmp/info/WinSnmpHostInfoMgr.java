package com.broada.carrier.monitor.impl.host.snmp.info;

import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpValue;
import com.broada.utils.StringUtil;

/**
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2007-12-2 下午03:28:48
 */
public class WinSnmpHostInfoMgr extends SnmpHostInfoMgr {
  private String sysDesc = null;

  public WinSnmpHostInfoMgr(String ip, SnmpMethod method) {
    super(ip, method);
  }

  /*
   * @see com.broada.carrier.monitor.impl.host.cli.snmphostinfo.SnmpHostInfoMgr#generateHardWareInfo()
   */
  protected String generateHardWareInfo() throws Exception {
    if (sysDesc == null)
      sysDesc = getSysDesc();
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
   * @see com.broada.carrier.monitor.impl.host.cli.snmphostinfo.SnmpHostInfoMgr#generateSoftWareInfo()
   */
  protected String generateSoftWareInfo() throws Exception{
    if (sysDesc == null)
      sysDesc = getSysDesc();
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
