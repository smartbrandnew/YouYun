package com.broada.carrier.monitor.impl.db.oracle.recursion;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class OracleRecursionMonitor implements Monitor {
	private static final Logger logger = LoggerFactory.getLogger(OracleRecursionMonitor.class);	
	
  @Override
	public MonitorResult monitor(MonitorContext context) {
  	OracleRecursionTemp temp = context.getTempData(OracleRecursionTemp.class);
  	if (temp == null)
  		temp = new OracleRecursionTemp();
  	MonitorResult result = collect(new CollectContext(context), temp);
		context.setTempData(temp);
		return result;
	}
  
  @Override
	public Serializable collect(CollectContext context) {
  	return collect(context, new OracleRecursionTemp());
  }

	private MonitorResult collect(CollectContext context, OracleRecursionTemp temp) {		
    MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    OracleMethod option = new OracleMethod(context.getMethod());    
    OracleManager om = new OracleManager(context.getNode().getIp(), option);
    long respTimeTotal = 0L;
    long respTime = System.currentTimeMillis();
    try {
      om.initConnection();
      respTime = System.currentTimeMillis() - respTime;
      if (respTime < 0)
        respTime = 1L;
      respTimeTotal += respTime;
    } catch (LogonDeniedException lde) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(lde.getMessage());
      respTime = System.currentTimeMillis() - respTime;
      if (respTime <= 0) {
        respTime = 1;
      }
      result.setResponseTime(respTime);
      om.close();
      return result;
    } catch (SQLException e) {
      String errMsg = e.getMessage();
      if (logger.isDebugEnabled()) {
        logger.debug(errMsg, e);
      }
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(errMsg);
      om.close();
      return result;
    }
    
    double seconds = 0.0;
    double lastRecursionCallNum = 0.0;
    double lastUserCallNum = 0.0;
    long nowTime = System.currentTimeMillis();
    //由于监测功能与server端分离，而lastConsumeTime不保存至库中
    //故根据最后运行时间进行判断
    if(temp.getTime() > 0){
      seconds = (double)(nowTime - temp.getTime())/1000;
      lastRecursionCallNum = temp.getLastRecursionCallNum();
      lastUserCallNum = temp.getLastUserCallNum();
    }
    
    // 从数据库获取数据
    OracleRecursionInfo info = null;
    try {
      respTime = System.currentTimeMillis();
      info = om.getRecursionInfo();
      respTime = System.currentTimeMillis() - respTime;
      if (respTime < 0)
        respTime = 0L;
      respTimeTotal += respTime;
      
      if(info.getRecursionCallVelocity(lastRecursionCallNum, seconds) < 0){
        // 屏蔽实例停止，然后启动造成指标为负数情况
        try {
          lastRecursionCallNum = info.getRecursion_call_num();
          lastUserCallNum = info.getUser_call_num();
          Thread.sleep(5000);
          respTime = System.currentTimeMillis();
          info = om.getRecursionInfo();
          respTime = System.currentTimeMillis() - respTime;
          if (respTime < 0)
            respTime = 0L;
          respTimeTotal += respTime;
          seconds = (double)(System.currentTimeMillis() - nowTime)/1000;
        } catch (SQLException e) {
          logger.error("获取Oracle递归调用信息失败。", e);
          result.setState(MonitorConstant.MONITORSTATE_FAILING);
          result.setResultDesc("获取Oracle递归调用信息失败。");
          return result;
        } catch (InterruptedException e) {
          logger.error("线程睡眠被中断。", e);
        }
      }
    } catch (SQLException e) {
      logger.error("获取Oracle递归调用信息失败。", e);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("获取Oracle递归调用信息失败。");
      return result;
    } finally {
    	om.close();
    }
    
    result.setResponseTime(respTimeTotal);
    
    double recursionRatioInTimes = info.getRecursionCallRatioInTimes(lastRecursionCallNum, lastUserCallNum);
    result.addPerfResult(new PerfResult("ORACLE-RECURSION-1", new BigDecimal(recursionRatioInTimes).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
    
    double recursionRatio = info.getRecursionCallRatio();
    result.addPerfResult(new PerfResult("ORACLE-RECURSION-2", new BigDecimal(recursionRatio).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));

    double recursionVelocity = info.getRecursionCallVelocity(lastRecursionCallNum, seconds);
    result.addPerfResult(new PerfResult("ORACLE-RECURSION-3", new BigDecimal(recursionVelocity).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));
        
    double recursionUserRatio = info.getRecursionUserRatio();
    result.addPerfResult(new PerfResult("ORACLE-RECURSION-4", new BigDecimal(recursionUserRatio).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()));

    result.addPerfResult(new PerfResult("ORACLE-RECURSION-5", info.getRecursion_call_num()));   
    result.addPerfResult(new PerfResult("ORACLE-RECURSION-6", info.getUser_call_num()));
    return result;
  }
}
