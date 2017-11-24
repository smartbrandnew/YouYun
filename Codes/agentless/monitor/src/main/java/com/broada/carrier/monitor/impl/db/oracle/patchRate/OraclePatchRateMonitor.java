package com.broada.carrier.monitor.impl.db.oracle.patchRate;

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
 * Oracle碎片监测器
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-10-9 下午01:32:28
 */
public class OraclePatchRateMonitor extends BaseMonitor {

  private static Log log = LogFactory.getLog(OraclePatchRateMonitor.class);

  public static final String DRIVER_CLASS = "oracle.jdbc.driver.OracleDriver";

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    OracleMethod option = new OracleMethod(context.getMethod());    
    OracleManager om = new OracleManager(context.getNode().getIp(), option);
    long respTime = System.currentTimeMillis();
    try {
      om.initConnection();
      respTime = System.currentTimeMillis() - respTime;
      if (respTime <= 0) {
        respTime = 1;
      }
      result.setResponseTime(respTime);
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
      if (log.isDebugEnabled()) {
        log.debug(errMsg, e);
      }
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(errMsg);
      om.close();
      return result;
    }

    List<OraclePatchRate> patchRateList = null;
    try {
      long replyTime = System.currentTimeMillis();
      patchRateList = om.getAllPatchRate();
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
    } catch (SQLException e) {
      if (log.isDebugEnabled()) {
        log.debug("无法获取数据库表空间碎片信息.", e);
      }
      result.setResultDesc("无法获取数据库表空间碎片信息.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } finally {
      om.close();
    }

    for (OraclePatchRate pr : patchRateList) {
    	MonitorResultRow row = new MonitorResultRow(pr.getTsName());
    	row.setIndicator("ORACLE-PATCHRATE-1", pr.getCurrFSFI());
    	result.addRow(row);
    }
    return result;
  }
}
