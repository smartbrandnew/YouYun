package com.broada.carrier.monitor.impl.db.oracle.fts;

import java.io.Serializable;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * Oracle 全表扫描监测器实现类
 * 
 * @author lvhs (lvhs@broada.com.cn)
 * Create By 2008-10-11 下午02:51:19
 */
public class OracleFTSMonitor extends BaseMonitor {
  private static final Log logger = LogFactory.getLog(OracleFTSMonitor.class);

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    OracleMethod option = new OracleMethod(context.getMethod());
    OracleManager om = new OracleManager(context.getNode().getIp(), option);
    long respTime = System.currentTimeMillis();
    try {
      om.initConnection();
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
    
    OracleFTSInfo info = null;
    StringBuffer msgSB = new StringBuffer();
    try {
      info = om.getOracleFTSInfo();
    } catch (SQLException e) {
      logger.error("获取Oracle全表扫描信息失败", e);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(msgSB.append("获取Oracle全表扫描信息失败").toString());
      return result;
    } finally {
    	om.close();
    }
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    result.setResponseTime(respTime);    
    result.addPerfResult(new PerfResult("ORACLE-FTS-1", info.getLtScanRatio()));
    result.addPerfResult(new PerfResult("ORACLE-FTS-2", info.getRsRatio()));    
    return result;
  }
}
