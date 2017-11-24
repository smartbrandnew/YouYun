package com.broada.carrier.monitor.impl.db.oracle.redolog;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
 * Oracle Redo 日志信息监测
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-10-16 上午09:10:41
 */
public class OracleRedoLogMonitor extends BaseMonitor {
  private static final Log log = LogFactory.getLog(OracleRedoLogMonitor.class);

  public static final String SEPARATOR = "\u007F";
  
  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    OracleMethod option = new OracleMethod(context.getMethod());
    List<RedoLogInfo> redoInfos = new ArrayList<RedoLogInfo>();
    OracleManager om = new OracleManager(context.getNode().getIp(), option);
    long respTime = System.currentTimeMillis();
    try {
      om.initConnection();
      redoInfos = om.getRedoLog();
      respTime = System.currentTimeMillis() - respTime;
      if (respTime <= 0) {
        respTime = 1;
      }
      result.setResponseTime(respTime);
    } catch (LogonDeniedException lde) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(lde.getMessage());
      respTime = System.currentTimeMillis() - respTime;
      if (respTime <= 0) {
        respTime = 1;
      }
      result.setResponseTime(respTime);
      return result;
    } catch (SQLException e) {
      String errMsg = e.getMessage();
      if (log.isDebugEnabled()) {
        log.debug(errMsg, e);
      }
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(errMsg);
      return result;
    } finally {
      om.close();
    }

    Iterator<String> iter = OracleRedoLogParameter.items.keySet().iterator();
    int index = 1;
    while (iter.hasNext()) {
      String field = iter.next().toString();      
      PerfResult pr = doCondition(redoInfos, field, index);
      result.addPerfResult(pr);
      index++;
    }
    return result;
  }

  /**
   * Oracle Redo Log 性能比较
   * @param redoInfos 
   * @param perfResult
   * @param field
   * @param condition
   * @param msg
   * @param desc
   * @param msgSB
   * @param valSB
   * @return
   */
  private PerfResult doCondition(List<RedoLogInfo> redoInfos, String field, int index) {
    double size = 0;
    for (int i = 0; i < redoInfos.size(); i++) {
      RedoLogInfo redo = redoInfos.get(i);
      if (redo.getName().equalsIgnoreCase(field)) {
        size = redo.getCurrValue();
        break;
      }
    }
    size = new BigDecimal(size).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
    //设置性能值
    return new PerfResult("ORACLE-REDOLOG-" + index, size);    
  }
}
