package com.broada.carrier.monitor.impl.host.cli.directory.unix;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.broada.carrier.monitor.impl.host.cli.directory.CLIDirectory;
import com.broada.carrier.monitor.impl.host.cli.file.CLIFileExecutor;
import com.broada.carrier.monitor.impl.host.cli.file.CLIFileMonitorCondition;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;

public class CLIDirectoryExecutor {
  public static Map<String, CLIFile> getFilesMap(String taskId, MonitorNode node, MonitorMethod method, StringBuffer buff,CLIDirectory[] directories,
      Map<String, Integer> fileCntMap, boolean isfile) {
  	return getFilesMap(taskId, node, method, buff, directories, fileCntMap, isfile, 1);
  }
  
  public static Map<String, CLIFile> getFilesMap(String taskId, MonitorNode node, MonitorMethod method, StringBuffer buff,CLIDirectory[] directories,
      Map<String, Integer> fileCntMap, boolean isfile, int tryTimes) {
    Map<String, CLIFile> filesMap = new LinkedHashMap<String, CLIFile>();
    for (int i = 0; i < directories.length; i++) {
      CLIDirectory directory = directories[i];
      if (directory == null) {
        continue;
      }
      try {
        if (isfile) {
          getFileConditions(taskId, node, method, directory.getPath(), "/* | grep '^[^d]'", filesMap, fileCntMap, tryTimes);
        } else {
          getFileConditions(taskId, node, method, directory.getPath(), " | grep '^d'", filesMap, fileCntMap, tryTimes);
        }
      } catch (CLILoginFailException fe) {
        buff.append("登录目标服务器失败;\n");
      } catch (CLIConnectException ce) {
        buff.append("无法连接目标服务器，可能目标服务器没开启或者网络断开;\n");
      } catch (CLIResultParseException e) {
        buff.append("解析目录采集结果失败;\n");
      } catch (Throwable e) {
        buff.append(e.getMessage() + ";\n");
      }
    }
    return filesMap;
  }
  
  private static void getFileConditions(String taskId, MonitorNode node, MonitorMethod method, String dir, String param, Map<String, CLIFile> filesMap,
      Map<String, Integer> fileCntMap, int tryTimes) throws CLIException, ParseException {
    CLIDirectoryExecutor.getFileConditionMap(taskId, node, method, dir, param, filesMap, fileCntMap, tryTimes);
  }
  
  private static void getFileConditionMap(String taskId, MonitorNode node, MonitorMethod method, String dir, String param, Map<String, CLIFile> filesMap,
      Map<String, Integer> fileCntMap, int tryTimes) throws CLIConnectException, CLILoginFailException, CLIResultParseException, CLIException {
  	Map<String, CLIFileMonitorCondition> files = CLIFileExecutor.getFileConditionMap(taskId, node, method, new String[]{dir + param});
  	fileCntMap.put(dir, files.size());
  	for (CLIFileMonitorCondition file : files.values()) {
  		CLIFile newFile = new CLIFile(file);
  		filesMap.put(newFile.getName(), newFile);
  	}  	
  }
}
