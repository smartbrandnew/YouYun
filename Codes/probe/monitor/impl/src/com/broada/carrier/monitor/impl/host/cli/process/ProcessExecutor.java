package com.broada.carrier.monitor.impl.host.cli.process;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.component.utils.error.ErrorUtil;

public class ProcessExecutor {
	private static final Logger logger = LoggerFactory.getLogger(ProcessExecutor.class);
	
  private float getTotalMemory(String taskId, MonitorNode node, MonitorMethod method) throws CLIException {
    try {
      CLIResult result = new CLIExecutor(taskId).execute(node, method, CLIConstant.COMMAND_TOTALMEMORY);      
    	if (result == null || result.getPropResult() == null)
    		return 0;
    	Object value = result.getPropResult().get("totalMemory");
    	if (value == null)
    		return 0;     
      try {
        return Float.parseFloat(value.toString());
      } catch (Throwable e) {
				throw new CLIResultParseException("解析总内存失败:" + value + ".", e);
      }      
    } catch (CLIException e) {      
      throw e;
    }
  }

  public Map<String, Object> getProcesses(String taskId, MonitorNode node, MonitorMethod method) throws CLIException {
  	return getProcesses(taskId, node, method, 0);
  }
  
  private static Map<String, Object> sortProcess(Map<String, Object> processConds) {
  	String[] keys = processConds.keySet().toArray(new String[0]);
  	Arrays.sort(keys);
  	
  	Map<String, Object> result = new LinkedHashMap<String, Object>();
  	for (String key : keys) {
  		result.put(key, processConds.get(key));
  	}
  	
  	return result;
	}  
  
  public Map<String, Object> getProcesses(String taskId, MonitorNode node, MonitorMethod method, int tryTimes) throws CLIException {
  	float memorySize = getTotalMemory(taskId, node, method);
  	
    CLIResult result = new CLIExecutor(taskId).execute(node, method, CLIConstant.COMMAND_PROCESS, tryTimes);
    List<Properties> processes = result.getListTableResult();
    
    Map<String, Object> processConds = new HashMap<String, Object>();
    DecimalFormat format = new DecimalFormat("#0.00");
    Throwable firstError = null;
    int deadProcessCount = 0;
    for (int index = 0; index < processes.size(); index++) {
      Properties properties = (Properties) processes.get(index);
      if(properties.size() < 4){
        continue;
      }
      String command = (String) properties.get("command");
      if (command == null)
      	continue;
    	if (command.contains("<defunct") 
    			|| command.contains("<idle")) {
    		deadProcessCount++;
    		continue;
    	}
      String cpu = (String) properties.get("cpu");
      String memory = (String) properties.get("memory");
      String status = (String) properties.get("status");
      

      CLIProcessMonitorCondition c = (CLIProcessMonitorCondition) processConds.get(command);
      try {
        if (c == null) {
          CLIProcessMonitorCondition cond = new CLIProcessMonitorCondition();
          cond.setField(command);
          cond.setCurrcpu(cpu);
          cond.setCurrmemory(memory);
          cond.setCpu("90");
          cond.setMemory("500");
          cond.setMemoryUtil("70");
          cond.setStatus(status);
          cond.setCurrMemoryUtil(memorySize > 0 ? format.format(Float.parseFloat(memory) *100/ memorySize) : "0");                    
          processConds.put(command, cond);
        } else {
          if (Float.parseFloat(c.getCurrcpu()) < Float.parseFloat(cpu))
            c.setCurrcpu(cpu);
          if (Float.parseFloat(c.getCurrmemory()) < Float.parseFloat(memory)){
            c.setCurrmemory(memory);
            c.setCurrMemoryUtil(memorySize > 0 ? format.format(Float.parseFloat(memory) * 100/ memorySize) : "0");
          }
        }        
      } catch (Throwable e) {
      	if (firstError == null)
      		firstError = e;
      	ErrorUtil.warn(logger, "解析进程指标失败：" + properties, e);				
      }
    }
    if (processConds.size() == 0 && firstError != null)
    	throw new CLIResultParseException(ErrorUtil.createMessage("解析进程指标失败", firstError), firstError);
    processConds.put("deadProcessThreshold", new Integer(deadProcessCount));
    return sortProcess(processConds);
  }
}
