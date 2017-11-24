package com.broada.carrier.monitor.impl.db.st.patchRate;

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
 * shentong碎片监测器
 * 
 * @author Zhouqa
 * Create By 2016年4月13日 上午10:35:58
 */
public class ShentongPatchRateMonitor extends BaseMonitor {

  private static Log log = LogFactory.getLog(ShentongPatchRateMonitor.class);

  public static final String DRIVER_CLASS = "com.oscar.Driver";

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    ShentongMethod option = new ShentongMethod(context.getMethod());    
    ShentongManager sm = new ShentongManager(context.getNode().getIp(), option);
    long respTime = System.currentTimeMillis();
    try {
      sm.initConnection();
      respTime = System.currentTimeMillis() - respTime;
      if (respTime <= 0) {
        respTime = 1;
      }
      result.setResponseTime(respTime);
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

    List<ShentongPatchRate> patchRateList = null;
    try {
      long replyTime = System.currentTimeMillis();
      patchRateList = sm.getAllPatchRate();
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
      sm.close();
    }

    for (ShentongPatchRate pr : patchRateList) {
    	MonitorResultRow row = new MonitorResultRow(pr.getTsName());
    	row.setIndicator("SHENTONG-PATCHRATE-1", pr.getCurrFSFI());
    	result.addRow(row);
    }
    return result;
  }
}
