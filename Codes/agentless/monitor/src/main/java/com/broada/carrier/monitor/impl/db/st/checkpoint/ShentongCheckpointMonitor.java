package com.broada.carrier.monitor.impl.db.st.checkpoint;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.st.ShentongManager;
import com.broada.carrier.monitor.method.st.ShentongMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class ShentongCheckpointMonitor extends BaseMonitor {
	private static final Logger logger = LoggerFactory.getLogger(ShentongCheckpointMonitor.class);

  private static final String CHECKPOINT_TOTAL_STARTED_DATA = "SHENTONG-CHECKPOINT-1";
  private static final String CHECKPOINT_TOTAL_COMPLETED_DATA = "SHENTONG-CHECKPOINT-2";

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
    ShentongMethod option = new ShentongMethod(context.getMethod());
    ShentongManager sm = new ShentongManager(context.getNode().getIp(), option);
    int[] values = new int[] { 0, 0 };
    try {
      long replyTime = System.currentTimeMillis();
      sm.initConnection();
      values = sm.getCheckpoint();
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
    	sm.close();
    }
    result.addPerfResult(new PerfResult(CHECKPOINT_TOTAL_STARTED_DATA, values[0]));
    result.addPerfResult(new PerfResult(CHECKPOINT_TOTAL_COMPLETED_DATA, values[1]));
    return result;
  }
}
