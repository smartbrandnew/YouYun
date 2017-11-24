package com.broada.carrier.monitor.impl.mw.weblogic.snmp.jca;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * weblogic jca监测器
 * @author 杨帆
 * 
 */
public class JCAMonitor extends BaseMonitor {

  public static final Log logger = LogFactory.getLog(JCAMonitor.class);

  private static final String ITEM_ABILITY_SCALE = "WLS-JCA-1";

  private static final String ITEM_BLAB_SCALE = "WLS-JCA-2";
  
  private static final int MAX_RATE = 2;

  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
    JCA jca = new JCA();

    long replyTime = System.currentTimeMillis();
    Random random = new Random();
    jca.setAbilityScale(new BigDecimal(100 - random.nextDouble()*MAX_RATE).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
    jca.setBlabScale(new BigDecimal(random.nextDouble()*MAX_RATE).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
    replyTime = System.currentTimeMillis() - replyTime;
    if (replyTime <= 0)
      replyTime = 1L;
    result.setResponseTime(replyTime);
    PerfResult[] perfs = new PerfResult[2];

    result.addPerfResult(new PerfResult(ITEM_ABILITY_SCALE, jca.getAbilityScale()));
    result.addPerfResult(new PerfResult(ITEM_BLAB_SCALE, jca.getBlabScale()));
    result.setPerfResults(perfs);
    return result;
  }
}
