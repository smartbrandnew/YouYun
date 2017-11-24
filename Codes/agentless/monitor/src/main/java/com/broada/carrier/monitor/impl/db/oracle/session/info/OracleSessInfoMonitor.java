package com.broada.carrier.monitor.impl.db.oracle.session.info;

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
 * Oracle 会话监测实现类
 * 
 * @author Lixy (lixy@broada.com.cn)
 * Create By 2006-11-10 14:21:25
 */

public class OracleSessInfoMonitor extends BaseMonitor {
  private static final Log log = LogFactory.getLog(OracleSessInfoMonitor.class);

  private static final int ITEMIDX_LENGTH = 10;

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
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
      if (log.isDebugEnabled()) {
        log.debug(errMsg, e);
      }
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(errMsg);
      om.close();
      return result;
    }

    List<OracleSessInfo> sessList=null;
    try {
      sessList = om.getAllSessInfos();
    } catch (SQLException e) {
      if (log.isDebugEnabled()) {
        log.debug("无法获取当前连接会话列表.", e);
      }
      result.setResultDesc("无法获取当前连接会话列表.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } finally {
      om.close();
    }
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    result.setResponseTime(respTime);
    assembleData(sessList, result);
    return result;
  }

	private void assembleData(List<OracleSessInfo> sessList, MonitorResult result) {
    for (int i = 0, perfIndex = 0; i < sessList.size(); perfIndex = perfIndex + ITEMIDX_LENGTH, i++) {
      OracleSessInfo sess = (OracleSessInfo) sessList.get(i);
      
      MonitorResultRow row = new MonitorResultRow(sess.getSessId(), sess.getUserName());

      //性能项监测
      for (int j = 0; j < ITEMIDX_LENGTH; j++) {
      	Object value = null;
      	if (j < 2) {
          value = (String) getSessValue(sess, j);
          if (value != null && value instanceof Double && ((Double) value).doubleValue() < 0)
          	value = null;          	
      	}
          
      	if (value != null)
      		row.setIndicator("ORACLE-SESS-" + (j + 1), value);
      }    
    }
  }

  /**
   * 根据索引取得属性值
   * @param sess
   * @param colIndex
   * @return
   */
  private Object getSessValue(OracleSessInfo sess, int colIndex) {
    switch (colIndex) {
    case 0:
      return sess.getSessId();
    case 1:
      return sess.getUserName();
    case 2:
      return sess.getSessCpu();
    case 3:
      return sess.getSessSorts();
    case 4:
      return sess.getTableScans();
    case 5:
      return sess.getSessReads();
    case 6:
      return sess.getSessWrites();
    case 7:
      return sess.getSessCommits();
    case 8:
      return sess.getSessCursors();
    case 9:
      return sess.getSessRatio();
    default:
      return null;
    }
  }
}
