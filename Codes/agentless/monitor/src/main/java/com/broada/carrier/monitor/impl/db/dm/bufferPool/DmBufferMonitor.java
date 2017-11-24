package com.broada.carrier.monitor.impl.db.dm.bufferPool;

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

public class DmBufferMonitor extends BaseMonitor {
	 private static Log log = LogFactory.getLog(DmBufferMonitor.class);
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

	    List<DmBuffer> bufferList = null;
	    try {
	      long replyTime = System.currentTimeMillis();
	      bufferList = dm.getAllBufferPools();
	      replyTime = System.currentTimeMillis() - replyTime;
	      if (replyTime <= 0)
	        replyTime = 1L;
	      result.setResponseTime(replyTime);
	    } catch (Exception e) {
	      if (log.isDebugEnabled()) {
	        log.debug("无法获取数据库内存缓冲区信息.", e);
	      }
	      result.setResultDesc("无法获取数据库内存缓冲区信息.");
	      result.setState(MonitorConstant.MONITORSTATE_FAILING);
	      return result;
	    } finally {
	    	dm.close();
	    }

	    for (DmBuffer buf : bufferList) {
	    	MonitorResultRow row = new MonitorResultRow(buf.getBufName());
	    	row.setIndicator("DM-BUFFERPOOL-1", buf.getBufName());
	    	row.setIndicator("DM-BUFFERPOOL-2", buf.getPageSize());
	    	row.setIndicator("DM-BUFFERPOOL-3", buf.getPageNo());
	    	row.setIndicator("DM-BUFFERPOOL-4", buf.getUsePage());
	    	row.setIndicator("DM-BUFFERPOOL-5", buf.getFreePage());
	    	row.setIndicator("DM-BUFFERPOOL-6", buf.getDirty_page());
	    	row.setIndicator("DM-BUFFERPOOL-7", buf.getBusyPage());
	    	row.setIndicator("DM-BUFFERPOOL-8", buf.getMaxPage());
	    	row.setIndicator("DM-BUFFERPOOL-9", buf.getLogicReads());
	    	row.setIndicator("DM-BUFFERPOOL-10", buf.getDiscard());
	    	row.setIndicator("DM-BUFFERPOOL-11", buf.getPhyReads());
	    	row.setIndicator("DM-BUFFERPOOL-12", buf.getMultiReads());
	    	row.setIndicator("DM-BUFFERPOOL-13", buf.getHitRate() * 100);
	    	result.addRow(row);
	    }
	    return result;
	}

}
