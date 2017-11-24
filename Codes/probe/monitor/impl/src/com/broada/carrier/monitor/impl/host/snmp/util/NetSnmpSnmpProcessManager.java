package com.broada.carrier.monitor.impl.host.snmp.util;

import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpValue;

public class NetSnmpSnmpProcessManager extends SnmpProcessManager {
  public static final String OID_NETSNMP_FREEMEMORY = ".1.3.6.1.4.1.2021.4.6.0";

  public static final String OID_NETSNMP_IDLECPU = ".1.3.6.1.4.1.2021.11.11.0";

  public NetSnmpSnmpProcessManager(SnmpWalk walk) {
    super(walk);
  }
  
  public Memory getMemory() throws SnmpException {
  	try {
	  	SnmpValue storageFree = getSnmpValue(OID_NETSNMP_FREEMEMORY);
	  	SnmpValue storageSize = getSnmpValue(OID_MEMORYSIZE + ".0");
			return new Memory("default", storageSize.toLong() * 1024, (storageSize.toLong() - storageFree.toLong()) * 1024);
  	} catch (SnmpException err) {
  	  if (err.getErrorStatus() == SnmpException.noSuchName)
  	    return super.getMemory();
  	  else
  	    throw err;
  	}		  	
	}

  /**
   * 返回值为0-100之间的值，除以100才是百分别
   * @return
   * @throws SnmpException
   * @throws  
   */
  public float getCpuPercentage() throws SnmpException  {
    SnmpResult var = walk.snmpGet(OID_NETSNMP_IDLECPU);

    int idleMem = 0;
    if (var != null && !var.getValue().isNull()) {
      idleMem = (int)var.getValue().toLong();
      return 100 - idleMem;
    } else {
      throw new SnmpException("取不到CPU空闲率。", 0 , SnmpException.noSuchName);
    }
  }
  
  public String getType() {
    return "net";
  }

}
