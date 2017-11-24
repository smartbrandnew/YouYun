package com.broada.carrier.monitor.impl.mw.weblogic.snmp.jvm;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.SnmpWLSManager;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpNotFoundException;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.utils.NumberUtil;

/**
 * <p>Title: </p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 1.0
 */
public class WLSJVMMonitor extends BaseMonitor {
	private static final Logger logger = LoggerFactory.getLogger(WLSJVMMonitor.class);
  private static final String ITEMIDX_WLSJVMHEAPSIZE = "WLSJVM-1";

  private static final String ITEMIDX_WLSJVMHEAPUTIL = "WLSJVM-2";

  boolean[] resultArray = new boolean[2];

  //记录heapUtil连续超标的次数,目前没有一个机制检测配置改变,所以如果修改了配置,该counter不会清为0
  //private int heapUtilKeepOverFlowCounter = 0;

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    SnmpMethod method = new SnmpMethod(context.getMethod());    
    SnmpWalk walk = new SnmpWalk(method.getTarget(context.getNode().getIp()));

    SnmpWLSManager mgr = new SnmpWLSManager(walk);
    //监测结果信息和告警的当前值信息
    long startTime=System.currentTimeMillis();
    long respTime = 0;
    try {
    	double heapSize = NumberUtil.round(mgr.getJVMHeapSize() / (1024 * 1024), 2);
    	result.addPerfResult(new PerfResult(ITEMIDX_WLSJVMHEAPSIZE, heapSize));
    	
    	double heapUtil = NumberUtil.round(mgr.getJVMHeapUtil(), 2);
    	result.addPerfResult(new PerfResult(ITEMIDX_WLSJVMHEAPUTIL, heapUtil));
    	
      return result;
		} catch (SnmpNotFoundException ex) {
      result.setResultDesc("成功连接到Weblogic SNMP代理,但无法获取JVM性能");
      respTime = calcResTime(result, startTime, respTime);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } catch (SnmpException ex) {
			result.setResultDesc("目标节点不支持SNMP代理,无法监测Weblogic服务器JVM,请检查配置参数！");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
    } catch (Exception ex) {
      if (logger.isDebugEnabled()) {
        logger.debug("未知异常", ex);
      }
      result.setResultDesc(ex.getClass().getName() + ":" + ex.getMessage());
      respTime = calcResTime(result, startTime, respTime);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } finally {
      walk.close();
    }
  }

	private long calcResTime(MonitorResult result, long startTime, long respTime) {
    if(respTime > 0) return respTime;
    respTime = System.currentTimeMillis() - startTime;
    if(respTime<=0){
      respTime=1;
    }
    result.setResponseTime(respTime);
    return respTime;
  }
}