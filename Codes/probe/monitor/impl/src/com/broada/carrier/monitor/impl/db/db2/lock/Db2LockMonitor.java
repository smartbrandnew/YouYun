package com.broada.carrier.monitor.impl.db.db2.lock;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.db2.Db2LockManager;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.utils.JDBCUtil;

public class Db2LockMonitor extends BaseMonitor {
	
	private static final Logger logger = LoggerFactory.getLogger(Db2LockMonitor.class);
  public static final String[] CONDITION_NAME = { "死锁数","锁升级率","锁等待率" };
  //Monitor对象是重复利用的，所以不能出现与监测任务相关的类成员变量，这些类成员变量将不会改变，从而出现bug
  //private Db2LockManager db2LockManager = null;
  
  @Override
  public Serializable collect(CollectContext context) {
  	MonitorResult result = new MonitorResult();
    DB2MonitorMethodOption option = new DB2MonitorMethodOption(context.getMethod());
    String ip = context.getNode().getIp();
    long respTime = System.currentTimeMillis();
    Connection testRespCon = null;
    Db2LockManager db2LockManager = null;
    try {
      db2LockManager = new Db2LockManager(ip, option);
      db2LockManager.setOption(option);
      testRespCon = db2LockManager.getConnection();
    } catch (SQLException lde) {
			if (lde.getErrorCode() == -99999) {//用户名或密码出错
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
			} else {//当前先不判断其他的错误代码,看情况以后再增加
				result.setState(MonitorConstant.MONITORSTATE_FAILING);
			}
			result.setResultDesc(lde.getMessage());
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
			logger.error(lde.getMessage(), lde);
			return result;
		} catch (CLIException e) {
		  respTime = System.currentTimeMillis() - respTime;
		  if (respTime <= 0) {
        respTime = 1;
      }
      result.setResponseTime(respTime);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("通过agent方式获取数据出错:"+e.getMessage());
      logger.error(e.getMessage(), e);
      return result;
    } finally {
      JDBCUtil.close(testRespCon);
    }

    //获取被锁定表的信息
    Db2Lock lockInfo = null;
    try {
      lockInfo = db2LockManager.getLockInfo(context.getResource().getId());
    } catch (CLIException e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("通过agent方式获取数据出错:" + e.getMessage());
      return result;
    } catch (Exception e) {
      result.setResultDesc("无法获取当前连接锁信息.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      logger.error(e.getMessage(), e);
      return result;
    } finally {
    }
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    result.setResponseTime(respTime);
    result.addPerfResult(new PerfResult("DB2-LOCK-JDBC-1", lockInfo.getDeadLockCnt()));
    result.addPerfResult(new PerfResult("DB2-LOCK-JDBC-2", lockInfo.getEscalLockRatio()));
    result.addPerfResult(new PerfResult("DB2-LOCK-JDBC-3", lockInfo.getAppWaitingRatio()));    
    return result;
  }
}