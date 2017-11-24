package com.broada.carrier.monitor.impl.mw.weblogic.snmp.jta;

import java.io.Serializable;
import java.math.BigDecimal;

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

/**
 * weblogic jta 监听器实现类
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: NMS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * @author amyson
 * @version 1.0
 */

public class WLSJTAMonitor extends BaseMonitor {
	private static final Logger logger = LoggerFactory.getLogger(WLSJTAMonitor.class);
  private static final String ITEMKDX_RC = "WLSJTA-1";// 资源错误导致的事务回滚数

  private static final String ITEMKDX_SYS = "WLSJTA-2";// 系统错误导致的事务回滚数

  private static final String ITEMKDX_APP = "WLSJTA-3";// 应用程序错误导致的事务回滚数

  private static final String ITEMKDX_TOT = "WLSJTA-4";// 已经处理的事务数

  private static final String ITEMKDX_RB_SCALE = "WLSJTA-5";// 全部的回滚事务比例

  private static final String ITEMKDX_RB_RC_SCALE = "WLSJTA-6";// 资源错误导致回滚的事务比例

  private static final String ITEMKDX_RB_SYS_SCALE = "WLSJTA-7";// 系统错误导致回滚的事务比例

  private static final String ITEMKDX_RB_APP_SCALE = "WLSJTA-8";// 应用程序错误导致回滚的事务比例
  
  private static final String ITEMKDX_TOT_PRE = "WLSJTA-9";// 每秒已经处理的事务数

  public WLSJTAMonitor() {
  }

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

    SnmpMethod method = new SnmpMethod(context.getMethod());
    SnmpWalk walk = new SnmpWalk(method.getTarget(context.getNode().getIp()));
    SnmpWLSManager mgr = new SnmpWLSManager(walk);
    // 监测结果信息和告警的当前值信息
    long startTime=System.currentTimeMillis();
    long respTime = 0;
    try {
      //获取服务已经运行的时间,单位秒
      double runtimeDouble=0;
      String runtime=mgr.getWlsJtaInfo(SnmpWLSManager.OID_WLS_BASIC_RUNTIME);
      if(runtime!=null&&(Double.parseDouble(runtime)/1000)!=0){
        runtimeDouble=Double.parseDouble(runtime)/1000;
      }
      String tmp = null;
      //get total transaction that has been processed
      tmp = mgr.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TATOTAL);
      respTime = calcResTime(result, startTime, respTime);
      double totCount = Double.parseDouble(tmp);
      result.addPerfResult(new PerfResult(ITEMKDX_TOT, totCount));

      double totCountPre = 0;
      if(runtimeDouble!=0){
      	totCountPre = new BigDecimal(Double.parseDouble(tmp)/runtimeDouble).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
      }
      result.addPerfResult(new PerfResult(ITEMKDX_TOT_PRE, totCountPre));
      
      tmp = mgr.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TA_RB_RC);
      double rcCount = Double.parseDouble(tmp);
      result.addPerfResult(new PerfResult(ITEMKDX_RC, rcCount));

      tmp = mgr.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TA_RB_SYS);
      double sysCount = Double.parseDouble(tmp);
      result.addPerfResult(new PerfResult(ITEMKDX_SYS, sysCount));
      
      tmp = mgr.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_TA_RB_APP);
      double appCount = Double.parseDouble(tmp);
      result.addPerfResult(new PerfResult(ITEMKDX_APP, appCount));
      
      //get roll backed transaction count
      tmp = mgr.getWlsJtaInfo(SnmpWLSManager.OID_WLS_JTA_RBTOTAL);
      double rbTotal = Double.parseDouble(tmp);
      //get scale of roll backed transaction
      double dtmp = Math.round(100 * rbTotal / totCountPre);
      result.addPerfResult(new PerfResult(ITEMKDX_RB_SCALE, dtmp));

      //get the scale of roll backed transaction because of resource error
      dtmp = Math.round(100 * rcCount / totCount);
      result.addPerfResult(new PerfResult(ITEMKDX_RB_RC_SCALE, dtmp));

      //get the scale of roll backed transaction because of system error
      dtmp = Math.round(100 * sysCount / totCount);
      result.addPerfResult(new PerfResult(ITEMKDX_RB_SYS_SCALE, dtmp));

      //get the scale of roll backed transaction because of application error
      dtmp = Math.round(100 * appCount / totCount);
      result.addPerfResult(new PerfResult(ITEMKDX_RB_APP_SCALE, dtmp));

      return result;
		} catch (SnmpNotFoundException ex) {
      result.setResultDesc("成功连接到Weblogic SNMP代理,但无法获取JVM性能");
      respTime = calcResTime(result, startTime, respTime);
      result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
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
      result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
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