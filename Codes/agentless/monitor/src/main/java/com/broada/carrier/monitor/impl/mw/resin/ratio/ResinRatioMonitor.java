package com.broada.carrier.monitor.impl.mw.resin.ratio;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.resin.ResinMonitorManager;
import com.broada.carrier.monitor.impl.mw.resin.ResinMonitorUtil;
import com.broada.carrier.monitor.method.resin.ResinJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ResinRatioMonitor implements Monitor {
  public static final String INDEX_RATIO = "RESIN-RATIO-1";

  public static final String INDEX_HIT = "RESIN-RATIO-2";

  public static final String INDEX_TOTAL = "RESIN-RATIO-3";

  @Override public MonitorResult monitor(MonitorContext context) {
    MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);

    List ratiosList = null;
    ResinJMXOption option = new ResinJMXOption(context.getMethod());
    String message = ResinMonitorUtil.testHostAndPort(context.getNode().getIp(),option);
    if (message != null) {
      result.setResultDesc(message);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }

    String url = null; // Resin管理Url

    try {
      url = ResinMonitorUtil.getAgentUrl(context.getNode().getIp(),option);
    } catch (UnsupportedEncodingException e) {
      result.setResultDesc("Resin连接失败,代理名称不正确。");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    try {
      ResinMonitorManager manager = new ResinMonitorManager();
      long replyTime = System.currentTimeMillis();
      ratiosList = manager.getRatiosByUrl(url);
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
    } catch (Exception e) {
      result.setResultDesc("Resin连接失败,代理部署不正确。");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }

    List<PerfResult> perfs = new ArrayList<PerfResult>();

    for (int index = 0; index < ratiosList.size(); index++) {
      ResinRatio resinRatio = (ResinRatio) ratiosList.get(index);
      PerfResult ratioPerf = new PerfResult(INDEX_RATIO, resinRatio.getRatio());

      PerfResult hitPerf = new PerfResult(INDEX_HIT, resinRatio.getHitCount());

      PerfResult totalPerf = new PerfResult(INDEX_TOTAL, resinRatio.getTotalCount());

      if (resinRatio.getName().equalsIgnoreCase(ResinMonitorUtil.RESIN_PROXY_RATIO)) {
        ratioPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_PROXY);
        hitPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_PROXY);
        totalPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_PROXY);
      } else if (resinRatio.getName().equalsIgnoreCase(ResinMonitorUtil.RESIN_BLOCK_RATIO)) {
        ratioPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_BLOCK);
        hitPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_BLOCK);
        totalPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_BLOCK);
      } else {
        ratioPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_INVOCATION);
        hitPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_INVOCATION);
        totalPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_INVOCATION);
      }
      perfs.add(ratioPerf);
      perfs.add(hitPerf);
      perfs.add(totalPerf);
    }

    if (result.getState() == MonitorConstant.MONITORSTATE_NICER) {
      result.setResultDesc("监测一切正常");
    }
    result.setPerfResults((PerfResult[]) perfs.toArray(new PerfResult[perfs.size()]));
    return result;
  }


  @Override public Serializable collect(CollectContext context) {
    List<ResinRatio> ratiosList = null;
    ResinJMXOption option = new ResinJMXOption(context.getMethod());
    String message = ResinMonitorUtil.testHostAndPort(context.getNode().getIp(),option);
    if (message != null) {
      throw new CollectException(message);
    }

    String url = null; // Resin管理Url

    try {
      url = ResinMonitorUtil.getAgentUrl(context.getNode().getIp(),option);
    } catch (UnsupportedEncodingException e) {
      throw new CollectException("Resin连接失败,代理名称不正确。", e);
    }
    try {
      ResinMonitorManager manager = new ResinMonitorManager();
      ratiosList = manager.getRatiosByUrl(url);
    } catch (Exception e) {
      throw new CollectException("Resin连接失败,代理部署不正确。", e);
    }

    List<PerfResult> perfs = new ArrayList<PerfResult>();
    List<MonitorInstance> instances = new ArrayList<MonitorInstance>();

    for (ResinRatio resinRatio : ratiosList) {
      PerfResult ratioPerf = new PerfResult(INDEX_RATIO, resinRatio.getRatio());

      PerfResult hitPerf = new PerfResult(INDEX_HIT, resinRatio.getHitCount());

      PerfResult totalPerf = new PerfResult(INDEX_TOTAL, resinRatio.getTotalCount());

      MonitorInstance mi = new MonitorInstance();

      if (resinRatio.getName().equalsIgnoreCase(ResinMonitorUtil.RESIN_PROXY_RATIO)) {
        ratioPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_PROXY);
        hitPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_PROXY);
        totalPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_PROXY);
        mi.setInstanceKey(ResinRatioMonitorCondition.FIELD_PROXY);
        mi.setInstanceName(ResinMonitorUtil.RESIN_PROXY_RATIO);
      } else if (resinRatio.getName().equalsIgnoreCase(ResinMonitorUtil.RESIN_BLOCK_RATIO)) {
        ratioPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_BLOCK);
        hitPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_BLOCK);
        totalPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_BLOCK);
        mi.setInstanceKey(ResinRatioMonitorCondition.FIELD_BLOCK);
        mi.setInstanceName(ResinMonitorUtil.RESIN_BLOCK_RATIO);
      } else {
        ratioPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_INVOCATION);
        hitPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_INVOCATION);
        totalPerf.setInstanceKey(ResinRatioMonitorCondition.FIELD_INVOCATION);
        mi.setInstanceKey(ResinRatioMonitorCondition.FIELD_INVOCATION);
        mi.setInstanceName(ResinMonitorUtil.RESIN_INV_RATIO);
      }
      perfs.add(ratioPerf);
      perfs.add(hitPerf);
      perfs.add(totalPerf);
      instances.add(mi);
    }
    MonitorResult result = new MonitorResult();
    result.setPerfResults(perfs.toArray(new PerfResult[perfs.size()]));
    return result;
  }
}
