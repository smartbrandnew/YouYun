package com.broada.carrier.monitor.impl.db.dm.sessions;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.dm.DmManager;
import com.broada.carrier.monitor.method.dm.DmMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * 达梦会话监测实现类
 * 
 * @author Zhouqa
 * Create By 2016年4月14日 上午9:51:27
 */

public class DmSessInfoMonitor extends BaseMonitor {
  private static final Log log = LogFactory.getLog(DmSessInfoMonitor.class);


  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    DmMonitorMethodOption option = new DmMonitorMethodOption(context.getMethod());
    DmManager dm = new DmManager(context.getNode().getIp(), option);
    long respTime = System.currentTimeMillis();
    try {
			dm.initConnection();
    } catch (ClassNotFoundException lde) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(lde.getMessage());
      respTime = System.currentTimeMillis() - respTime;
      if (respTime <= 0) {
        respTime = 1;
      }
      result.setResponseTime(respTime);
      dm.close();
      return result;
    } catch (Exception e) {
      String errMsg = e.getMessage();
      if (log.isDebugEnabled()) {
        log.debug(errMsg, e);
      }
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(errMsg);
      dm.close();
      return result;
    }

    List<DmSessInfo> sessList=null;
    try {
      sessList = dm.getAllSessInfos();
    } catch (SQLException e) {
      if (log.isDebugEnabled()) {
        log.debug("无法获取当前连接会话列表.", e);
      }
      result.setResultDesc("无法获取当前连接会话列表.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } finally {
      dm.close();
    }
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    result.setResponseTime(respTime);
    int index = 1;
	  for (DmSessInfo sess:sessList) {
    	MonitorResultRow row = new MonitorResultRow("DMSESSION" + index++);
    	row.setIndicator("DM-SESS-1", sess.getSessId());
    	row.setIndicator("DM-SESS-2", sess.getUserName());
    	row.setIndicator("DM-SESS-3", sess.getSessSql());
    	row.setIndicator("DM-SESS-4", sess.getSessState());
    	row.setIndicator("DM-SESS-5", sess.getCurrSch());
    	row.setIndicator("DM-SESS-6", sess.getCreateTime());
    	row.setIndicator("DM-SESS-7", sess.getClntType());
    	row.setIndicator("DM-SESS-8", sess.getAutoCmt());
    	row.setIndicator("DM-SESS-9", sess.getClntHost());
    	result.addRow(row);  
	  }
    return result;
  }
}
