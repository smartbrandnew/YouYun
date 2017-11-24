package com.broada.carrier.monitor.impl.db.st.fts;

import java.io.Serializable;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.st.ShentongManager;
import com.broada.carrier.monitor.method.st.ShentongMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * shentong全表扫描监测器实现类
 * 
 * @author Zhouqa
 * Create By 2016年4月13日 上午10:01:30
 */
public class ShentongFTSMonitor extends BaseMonitor {
  private static final Log logger = LogFactory.getLog(ShentongFTSMonitor.class);

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
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
      if (logger.isDebugEnabled()) {
        logger.debug(errMsg, e);
      }
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(errMsg);
      sm.close();
      return result;
    }
    
    ShentongFTSInfo info = null;
    StringBuffer msgSB = new StringBuffer();
    try {
      info = sm.getShentongFTSInfo();
    } catch (SQLException e) {
      logger.error("获取Shentong全表扫描信息失败", e);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(msgSB.append("获取Shentong全表扫描信息失败").toString());
      return result;
    } finally {
    	sm.close();
    }
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    result.setResponseTime(respTime);    
    result.addPerfResult(new PerfResult("SHENTONG-FTS-1", info.getRsRatio()));
    return result;
  }
}
