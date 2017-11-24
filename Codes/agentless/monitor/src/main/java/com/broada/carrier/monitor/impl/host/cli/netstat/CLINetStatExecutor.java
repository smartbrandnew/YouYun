package com.broada.carrier.monitor.impl.host.cli.netstat;

import java.util.ArrayList;
import java.util.List;
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
/**
 * 执行netstat命令
 * 
 * @author zhoucy(zhoucy@broada.com.cn)
 * Create By May 5, 2008 9:48:44 AM
 */
public class CLINetStatExecutor {
  public static List getNetStatList(String taskId, MonitorNode node, MonitorMethod method) throws CLIConnectException, CLILoginFailException, CLIResultParseException, CLIException{
    CLIResult result = new CLIExecutor(taskId)
    .execute(node, method, CLIConstant.COMMAND_NETSTAT, new String[]{});
    List netstats = new ArrayList();
    if(result != null){
      List nsList = result.getListTableResult();
      for(int index = 0; index < nsList.size(); index++){
        Properties properties = (Properties) nsList.get(index);
        CLINetStatInfo info = new CLINetStatInfo();
        info.setName(properties.getProperty("name"));
        info.setMtu(Long.parseLong(properties.getProperty("mtu")));
        info.setNetwork(properties.getProperty("network"));
        info.setAddress(properties.getProperty("address"));
        info.setIpkts(Long.parseLong(properties.getProperty("ipkts", "-1")));
        info.setIerrs(Long.parseLong(properties.getProperty("ierrs", "-1")));
        info.setOpkts(Long.parseLong(properties.getProperty("opkts", "-1")));
        info.setOerrs(Long.parseLong(properties.getProperty("oerrs", "-1")));
        info.setColl(Long.parseLong(properties.getProperty("coll", "-1")));
        netstats.add(info);
      }
    }
    return netstats;
  }
  
  public static List getNetStatList(String taskId, MonitorNode node, MonitorMethod method, int tryTimes) throws CLIConnectException, CLILoginFailException, CLIResultParseException, CLIException{
    CLIResult result = new CLIExecutor(taskId)
    .execute(node, method, CLIConstant.COMMAND_NETSTAT,tryTimes, new String[]{});
    List netstats = new ArrayList();
    if(result != null){
      List nsList = result.getListTableResult();
			for (int index = 0; index < nsList.size(); index++) {
				Properties properties = (Properties) nsList.get(index);
				CLINetStatInfo info = new CLINetStatInfo();
				info.setName(properties.getProperty("name", ""));
				info.setMtu(Long.parseLong(properties.getProperty("mtu", "")));
				info.setNetwork(properties.getProperty("network", ""));
				info.setAddress(properties.getProperty("address", ""));
				info.setIpkts(Long.parseLong(properties.getProperty("ipkts", "-1")));
				info.setIerrs(Long.parseLong(properties.getProperty("ierrs", "-1")));
				info.setOpkts(Long.parseLong(properties.getProperty("opkts", "-1")));
				info.setOerrs(Long.parseLong(properties.getProperty("oerrs", "-1")));
				info.setColl(Long.parseLong(properties.getProperty("coll", "-1")));
				netstats.add(info);
			}
    }
    return netstats;
  }
}
