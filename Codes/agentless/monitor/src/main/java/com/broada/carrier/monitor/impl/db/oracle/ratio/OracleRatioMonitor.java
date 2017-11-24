package com.broada.carrier.monitor.impl.db.oracle.ratio;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * <p>Title: OracleRatioMonitor</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.3
 */

public class OracleRatioMonitor extends BaseMonitor {
  private static final Log logger = LogFactory.getLog(OracleRatioMonitor.class);

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    OracleMethod option = new OracleMethod(context.getMethod());
    StringBuffer msgSB = new StringBuffer();
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
      if (logger.isDebugEnabled()) {
        logger.debug(errMsg, e);
      }
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(errMsg);
      om.close();
      return result;
    }

    try {
	    double ratio = 0d;
	    try {
	      ratio = om.getCacheHitRatio();
	      ratio = new BigDecimal(ratio).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	      result.addPerfResult(new PerfResult("ORACLE-RATIO-1", ratio));
	    } catch (SQLException ex) {
	      msgSB.append("高速缓存区命中率获取失败;");      
	    }
	
	    try {
	      ratio = om.getReloadsToPinsRatio();
	      ratio = new BigDecimal(ratio).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	      result.addPerfResult(new PerfResult("ORACLE-RATIO-2", ratio));
	      result.addPerfResult(new PerfResult("ORACLE-RATIO-6", 100 - ratio));
	    } catch (SQLException ex) {
	      msgSB.append("共享区库缓存区命中率和多次解析（重装）的条目比率获取失败;");
	    }
	
	    try {
	      ratio = om.getDictionaryCacheRatio();
	      ratio = new BigDecimal(ratio).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	      result.addPerfResult(new PerfResult("ORACLE-RATIO-3", ratio));
	    } catch (SQLException ex) {
	      msgSB.append("共享区字典缓存区命中率获取失败;");
	    }
	
	    try {
	      ratio = om.getRollbackSegmentHeaderRatio();
	      ratio = new BigDecimal(ratio).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	      result.addPerfResult(new PerfResult("ORACLE-RATIO-4", ratio));
	    } catch (SQLException ex) {
	      msgSB.append("回退段等待次数与获取次数比率获取失败;");
	    }
	
	    try {
	      ratio = om.getDiskMemorySortRatio();
	      ratio = new BigDecimal(ratio).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	      result.addPerfResult(new PerfResult("ORACLE-RATIO-5", ratio));
	    } catch (SQLException ex) {
	      msgSB.append("磁盘排序与内存排序比率获取失败;");
	    }
	    respTime = System.currentTimeMillis() - respTime;
	    if (respTime <= 0) {
	      respTime = 1;
	    }
	    result.setResponseTime(respTime);
    } finally {
    	om.close();
    }

    return result;
  }
}
