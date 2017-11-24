package com.broada.carrier.monitor.impl.host.cli.linuxio;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIErrorLine;
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
    if (ioInfos.isEmpty()) {
      if (result.getErrLines() != null && result.getErrLines().length > 0) {
        StringBuilder sb = new StringBuilder();
        for (CLIErrorLine line : result.getErrLines()) {          
          sb.append("解析第").append(line.getId()).append("行出错：").append(line.getContent()).append("/r/n");
        }
        throw new CLIResultParseException(sb.toString());
      }
    }
    List ioConds = new ArrayList();
    for (int index = 0; index < ioInfos.size(); index++) {
      Properties properties = (Properties) ioInfos.get(index);
      String device = (String) properties.get("device");
      String rrqm = (String) properties.get("rrqmPerSec");
      String wrqm = (String) properties.get("wrqmPerSec");
      String read = (String) properties.get("rPerSec");
      String write = (String) properties.get("wPerSec");
      String rsec = (String) properties.get("rsecPerSec");
      String wsec = (String) properties.get("wsecPerSec");
      String rkb = (String) properties.get("rkbPerSec");
      String wkb = (String) properties.get("wkbPerSec");
      String avgrqsz = (String) properties.get("avgrq-sz");
      String avgqusz = (String) properties.get("avgqu-sz");
      String await = (String) properties.get("await");
      String svctm = (String) properties.get("svctm");
      String util = (String) properties.get("%util");
      

      CLIIOMonitorCondition cond = new CLIIOMonitorCondition();
      cond.setField(device);
      cond.setRrqmPerSecond(parseFloat(rrqm));
      cond.setWrqmPerSecond(parseFloat(wrqm));
      cond.setReadPerSecond(parseFloat(read));
      cond.setWritePerSecond(parseFloat(write));
      cond.setRsecPerSecond(parseFloat(rsec));
      cond.setWsecPerSecond(parseFloat(wsec));
      cond.setRkbPerSecond(parseFloat(rkb));
      cond.setWkbPerSecond(parseFloat(wkb));
      cond.setAvgrqsz(parseFloat(avgrqsz));
      cond.setAvgqusz(parseFloat(avgqusz));
      cond.setAwait(parseFloat(await));
      cond.setSvctm(parseFloat(svctm));
      cond.setUtil(parseFloat(util));
      ioConds.add(cond);
    }
    return ioConds;
  }
  
  private static float parseFloat(String value) {
    return value == null ? 0 : Float.parseFloat(value);
  }
}
