package com.broada.carrier.monitor.impl.mw.resin.connPool;

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
import com.broada.monitor.agent.resin.server.entity.ResinConnectionPool;
import com.broada.utils.StringUtil;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ResinConnMonitor implements Monitor {

  public static final String INDEX_ACTIVE_COUNT = "RESIN-CONNPOOL-1";

  public static final String INDEX_IDLE_COUNT = "RESIN-CONNPOOL-2";

  public static final String INDEX_CONN_RATIO = "RESIN-CONNPOOL-3";

  public static final String INDEX_TOTAL_CONN_COUNT = "RESIN-CONNPOOL-4";

  public static final String INDEX_INIT_CONN_COUNT = "RESIN-CONNPOOL-5";

  public static final String INDEX_FAIL_CONN_COUNT = "RESIN-CONNPOOL-6";

  public static final String INDEX_FILNAL_CONN_TIME = "RESIN-CONNPOOL-7";

  public static final String INDEX_MAX_CONN_COUNT = "RESIN-CONNPOOL-8";

  public static final String INDEX_MAX_IDLE_TIME = "RESIN-CONNPOOL-9";

  @Override public MonitorResult monitor(MonitorContext context) {
    MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);

    List conns = null;
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
      conns = manager.getFirstConnsByUrl(url);
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

    Iterator it = new ArrayList(Arrays.asList(context.getInstances())).iterator();
    while (it.hasNext()) {
      MonitorInstance instance = (MonitorInstance) it.next();
      ResinConnectionPool conn = getConnPoolByInsKey(conns, instance.getCode());
      if (conn == null) {
        result.setResultDesc((StringUtil.isNullOrBlank(result.getResultDesc()) ? "" : (result.getResultDesc() + ","))
            + "连接池 " + instance.getCode() + "不存在");
        result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      } else {
        String connName = (String) conn.getName();

        PerfResult prefActiveCount = new PerfResult(INDEX_ACTIVE_COUNT, conn.getActiveCount());
        prefActiveCount.setInstanceKey(connName);

        PerfResult prefIdleCount = new PerfResult(INDEX_IDLE_COUNT, conn.getIdleCount());
        prefIdleCount.setInstanceKey(connName);

        PerfResult prefConnRatio = new PerfResult(INDEX_CONN_RATIO, conn.getCreate_ratio());
        prefConnRatio.setInstanceKey(connName);

        PerfResult prefTotalCount = new PerfResult(INDEX_TOTAL_CONN_COUNT, conn.getConnectionCountTotal());
        prefTotalCount.setInstanceKey(connName);

        PerfResult prefInitCount = new PerfResult(INDEX_INIT_CONN_COUNT, conn.getConnectionCreateCountTotal());
        prefInitCount.setInstanceKey(connName);

        PerfResult prefFailCount = new PerfResult(INDEX_FAIL_CONN_COUNT, conn.getConnectionFailCountTotal());
        prefFailCount.setInstanceKey(connName);

        PerfResult prefFinalTime = new PerfResult(INDEX_FILNAL_CONN_TIME, conn.getLastFailTime());
        prefFinalTime.setInstanceKey(connName);

        PerfResult prefMaxConnCount = new PerfResult(INDEX_MAX_CONN_COUNT, conn.getMaxConnections());
        prefMaxConnCount.setInstanceKey(connName);

        PerfResult prefMaxIdleTime = new PerfResult(INDEX_MAX_IDLE_TIME, conn.getIdle_time());
        prefMaxIdleTime.setInstanceKey(connName);

        perfs.add(prefActiveCount);
        perfs.add(prefIdleCount);
        perfs.add(prefConnRatio);
        perfs.add(prefTotalCount);
        perfs.add(prefInitCount);
        perfs.add(prefFailCount);
        perfs.add(prefFinalTime);
        perfs.add(prefMaxConnCount);
        perfs.add(prefMaxIdleTime);
      }
    }

    result.setPerfResults((PerfResult[]) perfs.toArray(new PerfResult[perfs.size()]));
    if (result.getState() == MonitorConstant.MONITORSTATE_NICER) {
      result.setResultDesc("监测一切正常");
    }

    return result;
  }

  private ResinConnectionPool getConnPoolByInsKey(List conns, String key) {
    for (int index = 0; index < conns.size(); index++) {
      ResinConnectionPool resinConn = (ResinConnectionPool) conns.get(index);
      if (resinConn.getName().equals(key)) {
        return resinConn;
      }
    }
    return null;
  }

  @Override public Serializable collect(CollectContext context) {
    List<ResinConnectionPool> conns = null;
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
      conns = manager.getFirstConnsByUrl(url);
    } catch (Exception e) {
      throw new CollectException("Resin连接失败,代理部署不正确。", e);
    }

    List<PerfResult> perfs = new ArrayList<PerfResult>();
    List<MonitorInstance> instances = new ArrayList<MonitorInstance>();

    for(ResinConnectionPool conn : conns) {
      String connName = (String) conn.getName();

      PerfResult prefActiveCount = new PerfResult(INDEX_ACTIVE_COUNT, conn.getActiveCount());
      prefActiveCount.setInstanceKey(connName);

      PerfResult prefIdleCount = new PerfResult(INDEX_IDLE_COUNT, conn.getIdleCount());
      prefIdleCount.setInstanceKey(connName);

      PerfResult prefConnRatio = new PerfResult(INDEX_CONN_RATIO, conn.getCreate_ratio());
      prefConnRatio.setInstanceKey(connName);

      PerfResult prefTotalCount = new PerfResult(INDEX_TOTAL_CONN_COUNT, conn.getConnectionCountTotal());
      prefTotalCount.setInstanceKey(connName);

      PerfResult prefInitCount = new PerfResult(INDEX_INIT_CONN_COUNT, conn.getConnectionCreateCountTotal());
      prefInitCount.setInstanceKey(connName);

      PerfResult prefFailCount = new PerfResult(INDEX_FAIL_CONN_COUNT, conn.getConnectionFailCountTotal());
      prefFailCount.setInstanceKey(connName);

      PerfResult prefFinalTime = new PerfResult(INDEX_FILNAL_CONN_TIME, conn.getLastFailTime());
      prefFinalTime.setInstanceKey(connName);

      PerfResult prefMaxConnCount = new PerfResult(INDEX_MAX_CONN_COUNT, conn.getMaxConnections());
      prefMaxConnCount.setInstanceKey(connName);

      PerfResult prefMaxIdleTime = new PerfResult(INDEX_MAX_IDLE_TIME, conn.getIdle_time());
      prefMaxIdleTime.setInstanceKey(connName);

      perfs.add(prefActiveCount);
      perfs.add(prefIdleCount);
      perfs.add(prefConnRatio);
      perfs.add(prefTotalCount);
      perfs.add(prefInitCount);
      perfs.add(prefFailCount);
      perfs.add(prefFinalTime);
      perfs.add(prefMaxConnCount);
      perfs.add(prefMaxIdleTime);

      MonitorInstance mi = new MonitorInstance();
      mi.setInstanceKey(connName);
      mi.setInstanceName(connName);
      instances.add(mi);
    }

    MonitorResult result = new MonitorResult();
    result.setPerfResults(perfs.toArray(new PerfResult[0]));
    return result;
  }
}
