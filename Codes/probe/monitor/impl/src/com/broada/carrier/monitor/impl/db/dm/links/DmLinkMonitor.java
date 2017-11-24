package com.broada.carrier.monitor.impl.db.dm.links;

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

public class DmLinkMonitor extends BaseMonitor {
	 private static Log log = LogFactory.getLog(DmLinkMonitor.class);
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

	    List<DmLinks> linkList = null;
	    try {
	      long replyTime = System.currentTimeMillis();
	      linkList = dm.getAllLinks();
	      replyTime = System.currentTimeMillis() - replyTime;
	      if (replyTime <= 0)
	        replyTime = 1L;
	      result.setResponseTime(replyTime);
	    } catch (Exception e) {
	      if (log.isDebugEnabled()) {
	        log.debug("无法获取数据库链接信息.", e);
	      }
	      result.setResultDesc("无法获取数据库链接信息.");
	      result.setState(MonitorConstant.MONITORSTATE_FAILING);
	      return result;
	    } finally {
	    	dm.close();
	    }

	    for (DmLinks link : linkList) {
	    	MonitorResultRow row = new MonitorResultRow();
	    	row.setIndicator("DM-LINK-1", link.getLinkID());
	    	row.setIndicator("DM-LINK-2", link.getLinkName());
	    	row.setIndicator("DM-LINK-3", link.getIsPublic());
	    	row.setIndicator("DM-LINK-4", link.getLoginName());
	    	row.setIndicator("DM-LINK-5", link.getHostName());
	    	row.setIndicator("DM-LINK-6", link.getPort());
	    	row.setIndicator("DM-LINK-7", link.getLoggenOn());
	    	row.setIndicator("DM-LINK-8", link.getHeterrogeneous());
	    	row.setIndicator("DM-LINK-9", link.getProtocol());
	    	row.setIndicator("DM-LINK-10", link.getInUse());
	    	result.addRow(row);
	    }
	    return result;
	}

}
