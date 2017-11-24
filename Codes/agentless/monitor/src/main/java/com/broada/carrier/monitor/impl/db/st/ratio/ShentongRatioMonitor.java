package com.broada.carrier.monitor.impl.db.st.ratio;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.st.ShentongManager;
import com.broada.carrier.monitor.method.st.ShentongMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * 
 * 
 * @author Zhouqa
 * Create By 2016年4月13日 下午1:50:12
 */
public class ShentongRatioMonitor extends BaseMonitor {
  private static final Log logger = LogFactory.getLog(ShentongRatioMonitor.class);

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    ShentongMethod option = new ShentongMethod(context.getMethod());
    StringBuffer msgSB = new StringBuffer();
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

    try {
	    double ratio = 0d;
	    try {
	      ratio = sm.getCacheHitRatio();
	      ratio = new BigDecimal(ratio).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	      result.addPerfResult(new PerfResult("SHENTONG-RATIO-1", ratio));
	    } catch (SQLException ex) {
	      msgSB.append("高速缓存区命中率获取失败;");      
	    }
	
	    try {
	      ratio = sm.getDiskMemorySortRatio();
	      ratio = new BigDecimal(ratio).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	      result.addPerfResult(new PerfResult("SHENTONG-RATIO-2", ratio));
	    } catch (SQLException ex) {
	      msgSB.append("磁盘排序与内存排序比率获取失败;");
	    }
	    respTime = System.currentTimeMillis() - respTime;
	    if (respTime <= 0) {
	      respTime = 1;
	    }
	    result.setResponseTime(respTime);
    } finally {
    	sm.close();
    }

    return result;
  }
}
