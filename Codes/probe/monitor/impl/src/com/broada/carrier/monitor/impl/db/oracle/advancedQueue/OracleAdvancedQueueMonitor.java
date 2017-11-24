package com.broada.carrier.monitor.impl.db.oracle.advancedQueue;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * Oracle 高级队列监测类
 * 
 * @author zhangwj (zhangwj@broada.com)
 * Create By 2008-10-15
 */
public class OracleAdvancedQueueMonitor extends BaseMonitor {
  private static final Log logger = LogFactory.getLog(OracleAdvancedQueueMonitor.class);
  
  @Override
	public Serializable collect(CollectContext context) {
  	//监测结果信息
    MonitorResult result = new MonitorResult(); 
    //连接参数的获取
    OracleMethod option = new OracleMethod(context.getMethod());
    OracleManager om = new OracleManager(context.getNode().getIp(), option);
    //测试连接
    long respTime = System.currentTimeMillis();
    try {
      om.initConnectionIgnoreRole();
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
      String errMsg1 = e.getMessage();
      if (logger.isDebugEnabled()) {
        logger.debug(errMsg1, e);
      }
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(errMsg1);
      respTime = System.currentTimeMillis() - respTime;
      if (respTime <= 0) {
        respTime = 1;
      }
      result.setResponseTime(respTime);
      om.close();
      return result;
    }
    //从DB获取所有队列信息
    List<OracleAdvanceQue> queues = null;
    try {
      queues =  om.getAllOracleQueues(true);
    } catch (SQLException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("无法获取队列信息.", e);
      }
      result.setResultDesc("无法获取队列信息.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } finally {
      om.close();
    }
    //监测部分
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    result.setResponseTime(respTime);
    
    for (OracleAdvanceQue queue : queues) {
        MonitorResultRow row = new MonitorResultRow();        
        row.setInstCode(queue.getQid());
        row.setInstName(queue.getQueName());
        if (queue.getMsgTotalNum() >= 0)
        	row.setIndicator("ORACLE-ADVANCEDQUE-1", queue.getMsgTotalNum());
        if (queue.getReadyMsgNum() >= 0)
        	row.setIndicator("ORACLE-ADVANCEDQUE-2", queue.getReadyMsgNum());
        if (queue.getArvWaitTime() >= 0)
        	row.setIndicator("ORACLE-ADVANCEDQUE-4", queue.getArvWaitTime());
        if (queue.getErrMsgNum() >= 0)
        	row.setIndicator("ORACLE-ADVANCEDQUE-3", queue.getErrMsgNum());
        result.addRow(row);
      }

    om.close();
    return result;
  }
}
