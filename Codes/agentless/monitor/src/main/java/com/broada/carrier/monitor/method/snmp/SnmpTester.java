package com.broada.carrier.monitor.method.snmp;

import org.snmp4j.mp.SnmpConstants;

import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.Snmp;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpTarget;
import com.broada.snmputil.SnmpTarget.AuthProtocol;
import com.broada.snmputil.SnmpTarget.PrivProtocol;
import com.broada.utils.StringUtil;

/**
 * SNMP访问测试器
 * @author Maico(panghf@broada.com)
 * Create By 2011-8-22 下午04:05:42
 */
public class SnmpTester {

  /**
   * 
   */
  public SnmpTester() {
  }

  /**
   * 进行SNMP测试
   * @param hostIpAddr
   * @param port
   * @param snmpVer
   * @param timeout
   * @param community
   * @param secLev
   * @param secName
   * @param authProt
   * @param authPass
   * @param privaProt
   * @param PrivaPass
   * @return
   */
  public String doTest(String hostIpAddr, Integer port, Integer snmpVer, Integer timeout, String community, String secLev,
      String secName, String authProt, String authPass, String privaProt, String privaPass) {
    SnmpTarget target = new SnmpTarget();
    target.setIp(hostIpAddr); //主机IP地址
    target.setPort(port); //端口
    target.setVersion(snmpVer); //SNMP协议版本
    target.setTimeout(timeout); //延时
    target.setRetryTime(0);

    SnmpResult[] results = null;
    if (snmpVer == SnmpConstants.version3) {
      target.setSecurityLevel(secLev);
      target.setSecurityName(secName);
      if (Snmp.SAFELEVEL_AUTHPRIV.equalsIgnoreCase(secLev)) {
        target.setAuthProtocol(AuthProtocol.valueOf(authProt));
        target.setAuthPassword(authPass);
        target.setPrivProtocol(PrivProtocol.valueOf(privaProt));
        target.setPrivPassword(privaPass);
      } else if (Snmp.SAFELEVEL_AUTHNOPRIV.equalsIgnoreCase(secLev)) {
        target.setAuthProtocol(AuthProtocol.valueOf(authProt));
        target.setAuthPassword(authPass);
      }
    } else {
      target.setReadCommunity(StringUtil.isNullOrBlank(community) ? "public" : community);
    }
    // 重试3次
    target.setRetryTime(3);
    SnmpWalk walk = new SnmpWalk(target);
    try {
      results = walk.snmpWalk("1.3.6.1.2.1.1"); //测试
    } catch (Exception e) {
      return "测试采集失败:"+e.getMessage();
    }finally{
      walk.close();
    }
    if(results==null){
      return "获取不到采集结果，请查看配置是否正确。";
    }else{
      return null;
    }
  }

}
