package com.broada.carrier.monitor.impl.mw.resin.baseInfo;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.resin.ResinMonitorManager;
import com.broada.carrier.monitor.impl.mw.resin.ResinMonitorUtil;
import com.broada.carrier.monitor.method.resin.ResinJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.monitor.agent.resin.server.entity.ResinBasic;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ResinBaseInfoMonitor extends BaseMonitor{
  public static final String ITEMIDX_SERVERID = "RESIN-BASEINFO-1";

  public static final String ITEMIDX_VERSION = "RESIN-BASEINFO-2";

  public static final String ITEMIDX_PATH = "RESIN-BASEINFO-3";

  public static final String ITEMIDX_HOME = "RESIN-BASEINFO-4";

  public static final String ITEMIDX_ROOT = "RESIN-BASEINFO-5";

  public static final String ITEMIDX_HOSTNAME = "RESIN-BASEINFO-6";

  public static final String ITEMIDX_STATE = "RESIN-BASEINFO-7";

  public static final String ITEMIDX_UPTIME = "RESIN-BASEINFO-8";

  public static final String ITEMIDX_TOTAL_MEM = "RESIN-BASEINFO-9";

  public static final String ITEMIDX_IDLE_MEM = "RESIN-BASEINFO-10";

  public static final String ITEMIDX_CPU = "RESIN-BASEINFO-11";

  @Override public Serializable collect(CollectContext context) {

    MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);

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
    ResinBasic basic = null;
    try {
      ResinMonitorManager manager = new ResinMonitorManager();
      long replyTime = System.currentTimeMillis();
      basic = manager.getBaseInfoByUrl(url);
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
    perfs.add(new PerfResult(ITEMIDX_SERVERID, basic.getServerId()));
    perfs.add(new PerfResult(ITEMIDX_VERSION, basic.getVersion()));
    perfs.add(new PerfResult(ITEMIDX_PATH, basic.getConfigFile()));
    perfs.add(new PerfResult(ITEMIDX_HOME, basic.getResinHome()));
    perfs.add(new PerfResult(ITEMIDX_ROOT, basic.getRootDirectory()));
    perfs.add(new PerfResult(ITEMIDX_HOSTNAME, basic.getLocalHost()));
    perfs.add(new PerfResult(ITEMIDX_STATE, basic.getState()));
    perfs.add(new PerfResult(ITEMIDX_UPTIME, basic.getUptime()));
    perfs.add(new PerfResult(ITEMIDX_TOTAL_MEM, basic.getToalMemory()));
    perfs.add(new PerfResult(ITEMIDX_IDLE_MEM, basic.getFreeMemory()));
    perfs.add(new PerfResult(ITEMIDX_CPU, basic.getCPULoad()));
    result.setState(MonitorState.SUCCESSED);
    if (result.getState() == MonitorConstant.MONITORSTATE_NICER) {
      result.setResultDesc("监测一切正常");
    }
    result.setPerfResults((PerfResult[]) perfs.toArray(new PerfResult[perfs.size()]));
    return result;
  }

}
