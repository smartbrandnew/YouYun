package com.broada.carrier.monitor.impl.host.cli.directory.win;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.host.cli.directory.CLIDirectory;
import com.broada.carrier.monitor.impl.host.cli.file.CLIFileMonitor;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.component.utils.error.ErrorUtil;

public class CLIDirectoryExecutor {
	private static final Logger logger = LoggerFactory.getLogger(CLIDirectoryExecutor.class);
	
  public static Map getFilesMap(String taskId, MonitorNode node, MonitorMethod method, CLIDirectory[] directories,
      Map fileCntMap, StringBuffer buff) {
    Map filesMap = new HashMap();
    for (int i = 0; i < directories.length; i++) {
      CLIDirectory directory = directories[i];
      if (directory == null) {
        continue;
      }
      String[] params = new String[] { directory.getPath(), directory.getSubdirLayerCount().toString() };
      try {
        getFileConditions(taskId, node, method, params, filesMap, fileCntMap);
      } catch (Throwable e) {
        buff.append(e.getMessage() + ";\n");
      }
    }
    return filesMap;
  }

  private static void getFileConditions(String taskId, MonitorNode node, MonitorMethod method, String[] params, Map filesMap,
      Map fileCntMap) throws CLIException, ParseException {
    CLIDirectoryExecutor.getFileConditionMap(taskId, node, method, params, filesMap, fileCntMap);
  }

  private static void getFileConditionMap(String taskId, MonitorNode node, MonitorMethod method, String[] directories, Map filesMap,
      Map fileCntMap) throws CLIException, ParseException {
    CLIResult result = new CLIExecutor(taskId).execute(node, method,
        CLIConstant.COMMAND_DIRECTORY, directories);
    List fileListProperties = result == null ? new ArrayList<Object>(0) : result.getListTableResult();
    fileCntMap.put(directories[0], new Integer(fileListProperties.size()));
    for (int index = 0; index < fileListProperties.size(); index++) {
      Properties properties = (Properties) fileListProperties.get(index);
      CLIFile cond = buildFileCondition(properties);
      if (cond != null) {
        filesMap.put(cond.getName(), cond);
      }
    }
  }

  private static CLIFile buildFileCondition(Properties properties) {
    String filePath = (String) properties.get("filePath");
    String size = (String) properties.get("size");
    String createTime = (String) properties.get("createTime");
    String modifyTime = (String) properties.get("modifyTime");
    if (filePath == null) {
      return null;
    }

    CLIFile cond = new CLIFile();
    cond.setName(filePath);
    cond.setSize(new Double(size));
    Date cTime = null;
    Date mTime = null;
    try {
      cTime = CLIFileMonitor.formateMtimeToDatetim(createTime);
      mTime = CLIFileMonitor.formateMtimeToDatetim(modifyTime);
    } catch (IllegalArgumentException e) {
    	ErrorUtil.warn(logger, "解析时间失败：" + createTime + "/" + modifyTime, e);
    }
    cond.setCreateTime(cTime);
    cond.setModifyTime(mTime);
    cond.setFiletype((String) properties.get("fileType"));
    cond.setStatus((String) properties.get("status"));
    return cond;
  }

  public static Map getFilesMap(String taskId, MonitorNode node, MonitorMethod method, CLIDirectory[] directories,
      Map fileCntMap, int tryTimes, StringBuffer buff) {
    Map filesMap = new HashMap();
    for (int i = 0; i < directories.length; i++) {
      CLIDirectory directory = directories[i];
      if (directory == null) {
        continue;
      }
      String[] params = new String[] { directory.getPath(), directory.getSubdirLayerCount().toString() };
      try {
        getFileConditions(taskId, node, method, params, filesMap, fileCntMap, tryTimes);
      } catch (Throwable e) {
        buff.append(e.getMessage() + ";\n");
      }
    }
    return filesMap;
  }

  private static void getFileConditions(String taskId, MonitorNode node, MonitorMethod method, String[] params, Map filesMap,
      Map fileCntMap, int tryTimes) throws CLIException, ParseException {
    CLIDirectoryExecutor.getFileConditionMap(taskId, node, method, params, filesMap, fileCntMap, tryTimes);
  }

  private static void getFileConditionMap(String taskId, MonitorNode node, MonitorMethod method, String[] directories, Map filesMap,
      Map fileCntMap, int tryTimes) throws CLIException, ParseException {
    CLIResult result = new CLIExecutor(taskId).execute(node, method,
        CLIConstant.COMMAND_DIRECTORY, tryTimes, directories);
    List fileListProperties = result.getListTableResult();
    fileCntMap.put(directories[0], new Integer(fileListProperties.size()));
    for (int index = 0; index < fileListProperties.size(); index++) {
      Properties properties = (Properties) fileListProperties.get(index);
      CLIFile cond = buildFileCondition(properties);
      if (cond != null) {
        filesMap.put(cond.getName(), cond);
      }
    }
  }

}
