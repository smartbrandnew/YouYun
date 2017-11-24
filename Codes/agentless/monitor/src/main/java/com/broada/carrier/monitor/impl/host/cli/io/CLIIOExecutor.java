package com.broada.carrier.monitor.impl.host.cli.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;

public class CLIIOExecutor {
  public static List procIO(String taskId, MonitorNode node, MonitorMethod method) throws CLIConnectException, CLILoginFailException, CLIResultParseException, CLIException {
    CLIResult result = null;
    result = new CLIExecutor(taskId).execute(node, method, CLIConstant.COMMAND_IO);
   
    List ioInfos = result.getListTableResult();
    List ioConds = new ArrayList();
    for (int index = 0; index < ioInfos.size(); index++) {
      Properties properties = (Properties) ioInfos.get(index);
      String device = (String) properties.get("device");
      String busy = (String) properties.get("busy");
      String avque = (String) properties.get("avque");
      String rwPerSecond = (String) properties.get("r+w/s");
      String blksPerSecond = (String) properties.get("blks/s");
      String avwait = (String) properties.get("avwait");
      String avserv = (String) properties.get("avserv");

      CLIIOMonitorCondition cond = new CLIIOMonitorCondition();
      cond.setField(device);
      cond.setBusy(Float.parseFloat(busy));
      cond.setAvque(Float.parseFloat(avque));
      cond.setRwPerSecond(Float.parseFloat(rwPerSecond));
      cond.setBlksPerSecond(Float.parseFloat(blksPerSecond));
      cond.setAvwait(Float.parseFloat(avwait));
      cond.setAvserv(Float.parseFloat(avserv));
      ioConds.add(cond);
    }
    return ioConds;
  }
  /**
   * 加入超时设置
   * @param service
   * @param monitorMethodParameterId
   * @return
   * @throws CLIConnectException
   * @throws CLILoginFailException
   * @throws CLIResultParseException
   * @throws CLIException
   */
  private static Map procIO(String taskId, MonitorNode node, MonitorMethod method, int tryTimes) throws CLIConnectException, CLILoginFailException, CLIResultParseException, CLIException {
    CLIResult result = null;
    result = new CLIExecutor(taskId).execute(node, method, CLIConstant.COMMAND_IO, tryTimes);
   
    List ioInfos = result.getListTableResult();
    Map ioConds = new HashMap();
    for (int index = 0; index < ioInfos.size(); index++) {
      Properties properties = (Properties) ioInfos.get(index);
      String device = (String) properties.get("device");
      String busy = (String) properties.get("busy");
      String avque = (String) properties.get("avque");
      String rwPerSecond = (String) properties.get("r+w/s");
      String blksPerSecond = (String) properties.get("blks/s");
      String avwait = (String) properties.get("avwait");
      String avserv = (String) properties.get("avserv");

      CLIIOMonitorCondition cond = new CLIIOMonitorCondition();
      cond.setField(device);
      cond.setBusy(Float.parseFloat(busy));
      cond.setAvque(Float.parseFloat(avque));
      cond.setRwPerSecond(Float.parseFloat(rwPerSecond));
      cond.setBlksPerSecond(Float.parseFloat(blksPerSecond));
      cond.setAvwait(Float.parseFloat(avwait));
      cond.setAvserv(Float.parseFloat(avserv));
      ioConds.put(device, cond);
    }
    return ioConds;
  }
  
  public static List procIO(String taskId, MonitorNode node, MonitorMethod method, List oldList, int tryTimes) throws CLIException{
    Map conds = procIO(taskId, node, method, tryTimes);
    if(conds==null){
      return oldList;
    }
    
    if (oldList != null && oldList.size() > 0) {
      for (int index = 0; index < oldList.size(); index++) {
        CLIIOMonitorCondition c1 = (CLIIOMonitorCondition) oldList.get(index);
        CLIIOMonitorCondition c2 = (CLIIOMonitorCondition) conds.get(c1.getField());
        if (c2 != null) {
          c2.setMaxAvque(c1.getMaxAvque());
          c2.setMaxAvserv(c1.getMaxAvserv());
          c2.setSelect(c1.getSelect());
        }
      }
    }
    List ios = new ArrayList();
    ios.addAll(conds.values());
    return ios;
  }
}
