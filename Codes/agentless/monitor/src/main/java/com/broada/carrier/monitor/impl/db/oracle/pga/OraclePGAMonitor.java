package com.broada.carrier.monitor.impl.db.oracle.pga;

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
 * Oracle PGA 监测实现类
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-10-7 下午04:29:45
 */
public class OraclePGAMonitor extends BaseMonitor {
  private static final Log log = LogFactory.getLog(OraclePGAMonitor.class);
  public static final String SEPARATOR = "\u007F";

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    //监测参数实体
    OracleMethod option = new OracleMethod(context.getMethod());
    List<OraclePGAInfo> pgsInfos = new ArrayList<OraclePGAInfo>();
    OracleManager om = new OracleManager(context.getNode().getIp(), option);
    long respTime = System.currentTimeMillis();
    try {
      om.initConnection();
      pgsInfos = om.getPgaInfos();
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

    Iterator iter = OraclePGAParameter.items.keySet().iterator();
    int index = 1;
    while (iter.hasNext()) {    	
      String field = iter.next().toString();
      PerfResult pr = doCondition(pgsInfos, field, index);
      result.addPerfResult(pr);      
      index++;
    }
    return result;
  }

  /**
   * OraclePGA 性能比较
   * @param om
   * @param perfResult
   * @param condition
   * @param msg
   * @param desc
   * @param msgSB
   * @param valSB
   * @return
   */
  private PerfResult doCondition(List<OraclePGAInfo> pgsInfos, String field, int index) {
    double size = 0;
    for (int i = 0; i < pgsInfos.size(); i++) {
      OraclePGAInfo pga = pgsInfos.get(i);
      if (pga.getName().equalsIgnoreCase(field)) {
        size = pga.getCurrValue();
        break;
      }
    }
    size = new BigDecimal(size).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    return new PerfResult("ORACLE-PGA-" + index, size);
  }
}
