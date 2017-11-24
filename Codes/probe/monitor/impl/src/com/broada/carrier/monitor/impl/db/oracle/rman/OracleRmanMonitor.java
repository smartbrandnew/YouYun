package com.broada.carrier.monitor.impl.db.oracle.rman;

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
import com.broada.component.utils.error.ErrorUtil;

/**
 * <p>Title: OracleRmanMonitor</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Broada</p>
 * @author plx (panlx@broada.com.cn)
 * @version 2.4
 */
public class OracleRmanMonitor extends BaseMonitor {
  private static final Log log = LogFactory.getLog(OracleRmanMonitor.class);

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    OracleMethod method = new OracleMethod(context.getMethod());
    OracleManager om = new OracleManager(context.getNode().getIp(), method);
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
      if (log.isDebugEnabled()) {
        log.debug(errMsg, e);
      }
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(errMsg);
      om.close();
      return result;
    }
        
		try {
			double fullBakValue = om.getRmanFullBak();
			fullBakValue = new BigDecimal(fullBakValue).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	    result.addPerfResult(new PerfResult("ORACLE-RMAN-1", fullBakValue));
	    
	    double incBakValue = om.getRmanIncBak();
	    incBakValue = new BigDecimal(incBakValue).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();    
	    result.addPerfResult(new PerfResult("ORACLE-RMAN-2", incBakValue));
		} catch (SQLException e) {
			throw ErrorUtil.createRuntimeException("获取RMAN信息失败", e);
		} finally {
			om.close();
		}
    
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    result.setResponseTime(respTime);
    return result;
  }
}
