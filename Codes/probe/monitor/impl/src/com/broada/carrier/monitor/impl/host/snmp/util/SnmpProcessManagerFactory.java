package com.broada.carrier.monitor.impl.host.snmp.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;
import com.broada.utils.StringUtil;

public class SnmpProcessManagerFactory {
  private static final Log logger = LogFactory.getLog(SnmpProcessManagerFactory.class);

  public static final String OID_OS = ".1.3.6.1.2.1.1.1.0";

  public static final String OS_SUNOS = "sunos";

  public static final String OS_LINUX = "linux";

  public static final String OS_WINDOWS = "windows";

  public static final String OS_HPUX = "hp-ux";
  private static Map<String, SnmpProcessManager> managers = new HashMap<String, SnmpProcessManager>();
  
	private static SnmpProcessManager getInstance(SnmpWalk walk) throws SnmpException {
		if (isNetSnmp(walk)) {
			return new NetSnmpSnmpProcessManager(walk);
		} else if (isHpux(walk)) {
			return new HPUXSnmpProcessManager(walk);
		} else if (isWindows(walk)) {
			return new WindowsSnmpProcessManager(walk);
		} else
			throw new IllegalArgumentException("暂不支持目标设备[%s]的CPU、RAM与进程等信息的SNMP获取，因为其不支持netsnmp、hpux与标准主机资源MIB");
	}

  /**
   * 根据snmp参数与nodeId来获取相应的SnmpProcessManager
   * 如果存在已缓存的SnmpProcessManager（以nodeId为key），则直接返回已存在的
   * @param walk
   * @param nodeId
   * @return
   * @throws SnmpException 
   */
  public static SnmpProcessManager getInstance(SnmpWalk walk, String nodeId) throws SnmpException {
  	SnmpProcessManager result = managers.get(nodeId);
  	if (result == null) {
  		result = getInstance(walk);
  		managers.put(nodeId, result);
  	}
  	result.setWalk(walk);
  	return result;    	
  }


  public static boolean isNetSnmp(SnmpWalk walk) throws SnmpException {
    SnmpResult var = null;
    try {
      //只要存在CPU空闲率的OID就认为是NetSnmp
      var = walk.snmpGet(".1.3.6.1.4.1.2021.11.11.0");
      if (var != null && !var.getValue().isNull()) {
        return true;
      }else{
        return false;
      }
    } catch (SnmpException e) {
      if (e.getErrorStatus() == SnmpException.noSuchName)
        return false;
      else
        throw e;
    }
  }

  public static boolean isWindows(SnmpWalk walk) throws SnmpException {
    //因为Windows是通过HostResMib去取的,所以只要能walk到
    //.1.3.6.1.2.1.25.3.3.1.2就采用Windows采集方式采集
    
    SnmpResult var = null;
    try {
      //只要存在.1.3.6.1.2.1.25.3.3.1.2就认为是支持标准的HostResMib
      var = walk.snmpGetNext(".1.3.6.1.2.1.25.3.3.1.2");
      if (var != null) {
        return true;
      }else{
        return false;
      }
    } catch (SnmpException e) {
      if (e.getErrorStatus() == SnmpException.noSuchName)
        return false;
      else
        throw e;
    }
  }

  public static boolean isHpux(SnmpWalk walk) throws SnmpException {
    String os = getOS(walk);
    if (StringUtil.isNullOrBlank(os)) {
      return false;
    }else{
      os = os.toLowerCase();
      return os.indexOf(OS_HPUX) > -1;
    }
  }

  public static String getOS(SnmpWalk walk) throws SnmpException {
    SnmpResult var = null;
    try {
      var = walk.snmpGet(OID_OS);
    } catch (SnmpException e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e);
      } else if (logger.isWarnEnabled()) {
        logger.warn("获取操作系统信息失败,节点:"+walk.getRemoteAddress()+"." + e.getMessage());
      }
      if (e.getErrorStatus() == SnmpException.noSuchName)
        return "";
      else
        throw e;
    }
    if (var != null) {
      return var.getValue().toString();
    } else {
      return "";
    }
  }
}
