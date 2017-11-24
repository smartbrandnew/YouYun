package com.broada.carrier.monitor.impl.db.st.session;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.st.ShentongManager;
import com.broada.carrier.monitor.method.st.ShentongMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * Shentong会话监测实现类
 * 
 * @author Zhouqa
 * Create By 2016年4月13日 下午3:30:12
 */

public class ShentongSessInfoMonitor extends BaseMonitor {
  private static final Log log = LogFactory.getLog(ShentongSessInfoMonitor.class);

  private static final int ITEMIDX_LENGTH = 6;

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    ShentongMethod option = new ShentongMethod(context.getMethod());
    ShentongManager sm = new ShentongManager(context.getNode().getIp(), option);
    long respTime = System.currentTimeMillis();
    try {
      sm.initConnection();
    } catch (ClassNotFoundException lde) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(lde.getMessage());
      respTime = System.currentTimeMillis() - respTime;
      if (respTime <= 0) {
        respTime = 1;
      }
      result.setResponseTime(respTime);
      sm.close();
      return result;
    } catch (SQLException e) {
      String errMsg = e.getMessage();
      if (log.isDebugEnabled()) {
        log.debug(errMsg, e);
      }
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(errMsg);
      sm.close();
      return result;
    }

    List<ShentongSessInfo> sessList=null;
    try {
      sessList = sm.getAllSessInfos();
    } catch (SQLException e) {
      if (log.isDebugEnabled()) {
        log.debug("无法获取当前连接会话列表.", e);
      }
      result.setResultDesc("无法获取当前连接会话列表.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } finally {
      sm.close();
    }
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    result.setResponseTime(respTime);
    result.setResultDesc("长度：" + sessList.size());
    assembleData(sessList, result);
    return result;
  }

	private void assembleData(List<ShentongSessInfo> sessList, MonitorResult result) {
    for (int i = 0, perfIndex = 0; i < sessList.size(); perfIndex = perfIndex + ITEMIDX_LENGTH, i++) {
      ShentongSessInfo sess = (ShentongSessInfo) sessList.get(i);
      
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
      		row.setIndicator("SHENTONG-SESS-" + (j + 1), value);
      }    
    }
  }

  /**
   * 根据索引取得属性值
   * @param sess
   * @param colIndex
   * @return
   */
  private Object getSessValue(ShentongSessInfo sess, int colIndex) {
    switch (colIndex) {
    case 0:
      return sess.getSessId();
    case 1:
      return sess.getUserName();
    case 2:
      return sess.getSessSorts();
    case 3:
      return sess.getTableScans();
    case 4:
      return sess.getSessReads();
    case 5:
      return sess.getSessWrites();
    case 6:
      return sess.getSessCommits();
    default:
      return null;
    }
  }
}
