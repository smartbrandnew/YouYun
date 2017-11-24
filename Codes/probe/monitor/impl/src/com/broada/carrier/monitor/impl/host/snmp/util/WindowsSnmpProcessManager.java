package com.broada.carrier.monitor.impl.host.snmp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;

public class WindowsSnmpProcessManager extends SnmpProcessManager {
  private static final Log logger = LogFactory.getLog(WindowsSnmpProcessManager.class);

  public static final String OID_WINDOWS_CPU = ".1.3.6.1.2.1.25.3.3.1.2";

  public WindowsSnmpProcessManager(SnmpWalk walk) {
    super(walk);
  }

  public float getCpuPercentage() throws SnmpException {
    SnmpResult[] results = walk.snmpWalk(OID_WINDOWS_CPU);
 
    if (results == null || results.length == 0) {
      throw new SnmpException("无法获取CPU使用率,可能代理不支持CPU信息采集!", 0, SnmpException.notFound);
    }
    int cpuUtility = 0;
    for (int index = 0; index < results.length; index++) {
      cpuUtility +=results[index].getValue().toLong();
    }

    float ret = (float) (cpuUtility * 0.1 / (results.length * 0.1));
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("%d个CPU, 平均使用率%.2f%%", results.length, ret));
		}
    return ret;
  }
  
  public String getType() {
    return "windows";
  }
}
