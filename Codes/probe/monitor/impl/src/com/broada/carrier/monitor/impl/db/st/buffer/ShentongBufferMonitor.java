package com.broada.carrier.monitor.impl.db.st.buffer;

import java.io.Serializable;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.st.ShentongManager;
import com.broada.carrier.monitor.method.st.ShentongMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class ShentongBufferMonitor extends BaseMonitor {
	 private static Log log = LogFactory.getLog(ShentongBufferMonitor.class);
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
	    } catch (Exception e) {
	      String errMsg = e.getMessage();
	      if (log.isDebugEnabled()) {
	        log.debug(errMsg, e);
	      }
	      result.setState(MonitorConstant.MONITORSTATE_FAILING);
	      result.setResultDesc(errMsg);
	      sm.close();
	      return result;
	    }

	    List<ShentongBuffer> bufferList = null;
	    try {
	      long replyTime = System.currentTimeMillis();
	      bufferList = sm.getAllBufferPools();
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
	    	sm.close();
	    }

	    for (ShentongBuffer buf : bufferList) {
	    	MonitorResultRow row = new MonitorResultRow();
	    	row.setIndicator("SHENTONG-BUFFER-1", buf.getPageSize());
	    	row.setIndicator("SHENTONG-BUFFER-2", buf.getFreePage());
	    	row.setIndicator("SHENTONG-BUFFER-3", buf.getDirty_page());
	    	row.setIndicator("SHENTONG-BUFFER-4", buf.getReadBlock());
	    	row.setIndicator("SHENTONG-BUFFER-5", buf.getWriteBlock());
	    	result.addRow(row);
	    }
	    return result;
	}

}
