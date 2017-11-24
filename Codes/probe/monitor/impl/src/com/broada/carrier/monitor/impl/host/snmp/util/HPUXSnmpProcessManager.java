package com.broada.carrier.monitor.impl.host.snmp.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpValue;

public class HPUXSnmpProcessManager extends SnmpProcessManager {
  private static final Log logger = LogFactory.getLog(HPUXSnmpProcessManager.class);

  /**
   * HP-UX 内存
   */
  private static final String OID_MEMORY_TOTAL = ".1.3.6.1.4.1.11.2.3.1.1.8.0";

  private static final String OID_MEMORY_AVAILABLE = ".1.3.6.1.4.1.11.2.3.1.1.7.0";

  /**
   * HP-UX CPU
   */
  private static final String OID_CPU_USER = ".1.3.6.1.4.1.11.2.3.1.1.13.0";

  private static final String OID_CPU_SYSTEM = ".1.3.6.1.4.1.11.2.3.1.1.14.0";

  private static final String OID_CPU_NICE = ".1.3.6.1.4.1.11.2.3.1.1.16.0";

  private static final String OID_CPU_IDLE = ".1.3.6.1.4.1.11.2.3.1.1.15.0";

  public HPUXSnmpProcessManager(SnmpWalk walk) {
    super(walk);
  }
  
  public Memory getMemory() throws SnmpException {
  	try {
	  	SnmpValue storageFree = getSnmpValue(OID_MEMORY_AVAILABLE);
	  	SnmpValue storageSize = getSnmpValue(OID_MEMORY_TOTAL);
			return new Memory("default", storageSize.toLong() * 1024, (storageSize.toLong() - storageFree.toLong()) * 1024);
  	} catch (SnmpException err) {
      if (err.getErrorStatus() == SnmpException.noSuchName)
        return super.getMemory();
      else
        throw err;
  	}		
	}

  /**
   * CPU Usage = [(CPU Used by Users + CPU Used by System + CPU Nice) ÷ (CPU Idle + CPU Used by Users + CPU Used by System + CPU Nice)] × 100
   * @return
   * @throws SnmpException 
   */
  public float getCpuPercentage() throws SnmpException {
    long user = getLongvalue(OID_CPU_USER);
    long system = getLongvalue(OID_CPU_SYSTEM);
    long nice = getLongvalue(OID_CPU_NICE);
    long idle = getLongvalue(OID_CPU_IDLE);
    if (logger.isDebugEnabled()) {
      logger.debug("CPU用户时间：" + user);
      logger.debug("CPU系统时间：" + system);
      logger.debug("CPU Nice时间：" + nice);
      logger.debug("CPU空闲时间：" + idle);
    }
    return (float) ((user + system + nice) * 100.0 / (user + system + nice + idle));
  }

  private long getLongvalue(String oid) throws SnmpException {
    long user;
    SnmpResult var = walk.snmpGet(oid);
    if (var == null) {
      throw new SnmpException("获取不到值：" + oid, 0, SnmpException.noSuchName);
    }
    user = var.getValue().toLong();
    return user;
  }
  
  public String getType() {
    return "hp-ux";
  }
}
