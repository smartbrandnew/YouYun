package com.broada.carrier.monitor.impl.db.dm.ramPool;

import java.io.Serializable;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.dm.DmManager;
import com.broada.carrier.monitor.method.dm.DmMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class DmRamPoolMonitor extends BaseMonitor {
	 private static Log log = LogFactory.getLog(DmRamPoolMonitor.class);
	@Override
	public Serializable collect(CollectContext context) {
		 MonitorResult result = new MonitorResult();
	    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
	    DmMonitorMethodOption option = new DmMonitorMethodOption(context.getMethod());    
	    DmManager dm = new DmManager(context.getNode().getIp(), option);
	    long respTime = System.currentTimeMillis();
	    try {
	      dm.initConnection();
	      respTime = System.currentTimeMillis() - respTime;
	      if (respTime <= 0) {
	        respTime = 1;
	      }
	      result.setResponseTime(respTime);
	    } catch (LoginException lde) {
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

	    List<DmRamPool> ramList = null;
	    try {
	      long replyTime = System.currentTimeMillis();
	      ramList = dm.getAllRamPools();
	      replyTime = System.currentTimeMillis() - replyTime;
	      if (replyTime <= 0)
	        replyTime = 1L;
	      result.setResponseTime(replyTime);
	    } catch (Exception e) {
	      if (log.isDebugEnabled()) {
	        log.debug("无法获取数据库内存池信息.", e);
	      }
	      result.setResultDesc("无法获取数据库内存池信息.");
	      result.setState(MonitorConstant.MONITORSTATE_FAILING);
	      return result;
	    } finally {
	    	dm.close();
	    }
	    int index = 1;
	    for (DmRamPool rampool : ramList) {
	    	MonitorResultRow row = new MonitorResultRow("RAMPOOL" + index++);
	    	row.setIndicator("DM-RAMPOOL-1", rampool.getCounts());
	    	row.setIndicator("DM-RAMPOOL-2", rampool.getTotalSize());
	    	row.setIndicator("DM-RAMPOOL-3", rampool.getFreeSize());
	    	row.setIndicator("DM-RAMPOOL-4", rampool.getUsedSize());
	    	row.setIndicator("DM-RAMPOOL-5", rampool.getUseRate());
	    	result.addRow(row);
	    }
	    return result;
	}

}
