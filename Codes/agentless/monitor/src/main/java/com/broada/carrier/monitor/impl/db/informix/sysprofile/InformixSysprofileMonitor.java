package com.broada.carrier.monitor.impl.db.informix.sysprofile;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.informix.InformixManager;
import com.broada.carrier.monitor.impl.db.informix.strategy.InformixStrategyFacade;
import com.broada.carrier.monitor.impl.db.informix.strategy.entity.InformixStrategy;
import com.broada.carrier.monitor.impl.db.informix.strategy.entity.InformixStrategyResult;
import com.broada.carrier.monitor.method.informix.InformixMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

import java.io.Serializable;
import java.sql.SQLException;

/**
 * @author lixy Sep 3, 2008 11:17:35 AM
 */
public class InformixSysprofileMonitor implements Monitor {


  private PerfResult[] getPerfResultArr(String typeId) {
    PerfResult[] prs = new PerfResult[InformixStrategyFacade.getStrategyCount(typeId)];
    for (int i = 0; i < prs.length; i++) {
      prs[i] = new PerfResult(typeId +"-" + (i + 1), false);
    }
    return prs;
  }

  @Override public MonitorResult monitor(MonitorContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    InformixMonitorMethodOption option = new InformixMonitorMethodOption(context.getMethod());
    String ip = context.getNode().getIp();

    int port = option.getPort();
    String srvName = option.getServername();
    String userName = option.getUsername();
    String passwd = option.getPassword();

    StringBuffer msgSB = new StringBuffer();
    StringBuffer valSB = new StringBuffer();

    InformixManager im = new InformixManager(ip, port, srvName, userName, passwd);
    long replyTime = System.currentTimeMillis();
    try {
      im.initConnection();
    } catch (SQLException ex) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(ex.getMessage());
      im.close();
      return result;
    }

      PerfResult[] perfs = getPerfResultArr(context.getTask().getTypeId());
    result.setPerfResults(perfs);

    InformixStrategyResult strategyResult = null;
    try {
      strategyResult = im.getStrategyResult(context.getTask().getTypeId(), context.getTask().getId());
    } catch (SQLException e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(e.getMessage());
      return result;
    } finally {
      im.close();
    }
    replyTime = System.currentTimeMillis() - replyTime;
    if (replyTime <= 0) {
      replyTime = 1;
    }
    result.setResponseTime((int) replyTime);

    boolean state = true;
    boolean getFaild = false;
    for (int i = 0; i < perfs.length; i++) {
      String itemCode = perfs[i].getItemCode();

      InformixStrategy strategy = InformixStrategyFacade.getStrategy(context.getTask().getTypeId(), itemCode);
      double currValue = strategyResult.getResult(itemCode);
      if (currValue == -1) {
        msgSB.append(strategy.getName() + "获取失败;\n");
        valSB.append(strategy.getName() + "获取失败;");
        getFaild = true;
        continue;
      }
      perfs[i].setValue(currValue);
    }
    result.setPerfResults(perfs);
    if (!state || getFaild) {
      result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      result.setResultDesc(msgSB.toString());
    } else {
      result.setState(MonitorConstant.MONITORSTATE_NICER);
    }
    return result;
  }

  @Override public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    InformixStrategyResult strategyResult = null;
    InformixMonitorMethodOption option = new InformixMonitorMethodOption(context.getMethod());
    String ip = context.getNode().getIp();
    int port = option.getPort();
    String dbServerName = option.getServername();
    String user = option.getUsername();
    String pass = option.getPassword();
    InformixManager im = new InformixManager(ip,port,dbServerName,user,pass);
    try {
      im.initConnection();
      //todo
      strategyResult = im.getStrategyResult(context.getTypeId(), "-1");
    } catch (SQLException ex) {
      throw new CollectException("无法获取数据空间信息,请检查配置信息是否正确!", ex);
    } finally {
      im.close();
    }
    PerfResult[] perfs = getPerfResultArr(context.getTypeId());
    for (PerfResult perf : perfs) {
      String itemCode = perf.getItemCode();
      double currValue = strategyResult.getResult(itemCode);
      if (currValue == -1) {
        perf.setValue(currValue);
      } else {
        perf.setValue(currValue);
      }
    }
    result.setPerfResults(perfs);
    return result;
  }
}
