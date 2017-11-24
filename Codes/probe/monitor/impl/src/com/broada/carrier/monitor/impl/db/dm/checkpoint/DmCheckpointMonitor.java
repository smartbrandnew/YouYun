package com.broada.carrier.monitor.impl.db.dm.checkpoint;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.dm.DmManager;
import com.broada.carrier.monitor.method.dm.DmMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class DmCheckpointMonitor extends BaseMonitor {
	private static final Logger logger = LoggerFactory.getLogger(DmCheckpointMonitor.class);
	private static final String CHECKPOINT_TOTAL_COUNT = "DM-CHECKPOINT-1";
	private static final String CHECKPOINT_BY_REDO_RESERVE = "DM-CHECKPOINT-2";
	private static final String CHECKPOINT_TIME_MS_USED = "DM-CHECKPOINT-3";
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
    DmMonitorMethodOption option = new DmMonitorMethodOption(context.getMethod());
    DmManager dm = new DmManager(context.getNode().getIp(), option);
    int[] values = new int[] { 0, 0, 0 };
    try {
      long replyTime = System.currentTimeMillis();
      dm.initConnection();
      values = dm.getCheckpoint();
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
    } catch (Exception e) {
      logger.debug("查询数据库失败:" + e.getMessage(), e);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(e.getMessage());
      return result;
    } finally {
    	dm.close();
    }
    result.addPerfResult(new PerfResult(CHECKPOINT_TOTAL_COUNT, values[0]));
    result.addPerfResult(new PerfResult(CHECKPOINT_BY_REDO_RESERVE, values[1]));
    result.addPerfResult(new PerfResult(CHECKPOINT_TIME_MS_USED, values[2]));
    return result;
	}

}
