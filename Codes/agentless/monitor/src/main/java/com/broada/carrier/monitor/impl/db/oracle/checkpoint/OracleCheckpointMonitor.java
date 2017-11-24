package com.broada.carrier.monitor.impl.db.oracle.checkpoint;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class OracleCheckpointMonitor extends BaseMonitor {
	private static final Logger logger = LoggerFactory.getLogger(OracleCheckpointMonitor.class);

  private static final String CHECKPOINT_TOTAL_STARTED_DATA = "ORACLE-CHECKPOINT-1";
  private static final String CHECKPOINT_TOTAL_COMPLETED_DATA = "ORACLE-CHECKPOINT-2";

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
    OracleMethod option = new OracleMethod(context.getMethod());
    OracleManager om = new OracleManager(context.getNode().getIp(), option);
    int[] values = new int[] { 0, 0 };
    try {
      long replyTime = System.currentTimeMillis();
      om.initConnection();
      values = om.getCheckpoint();
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
    	om.close();
    }
    result.addPerfResult(new PerfResult(CHECKPOINT_TOTAL_STARTED_DATA, values[0]));
    result.addPerfResult(new PerfResult(CHECKPOINT_TOTAL_COMPLETED_DATA, values[1]));
    return result;
  }
}
