package com.broada.carrier.monitor.impl.db.dm.redolog;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.dm.DmManager;
import com.broada.carrier.monitor.method.dm.DmMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class DmRedoLogMonitor extends BaseMonitor {
	private static final Log log = LogFactory.getLog(DmRedoLogMonitor.class);
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    DmMonitorMethodOption option = new DmMonitorMethodOption(context.getMethod());
    List<RedoLogInfo> redoInfos = new ArrayList<RedoLogInfo>();
    DmManager dm = new DmManager(context.getNode().getIp(), option);
    long respTime = System.currentTimeMillis();
    try {
    	dm.initConnection();
      redoInfos = dm.getRedoLog();
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
      return result;
    } catch (Exception e) {
      String errMsg = e.getMessage();
      if (log.isDebugEnabled()) {
        log.debug(errMsg, e);
      }
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(errMsg);
      return result;
    } finally {
    	dm.close();
    }

    Iterator<String> iter = DmRedoLogParameter.items.keySet().iterator();
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
	 * DM RedoLog性能比较
	 * @param redoInfos
	 * @param field
	 * @param index
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
    return new PerfResult("DM-REDOLOG-" + index, size);    
  }

}
