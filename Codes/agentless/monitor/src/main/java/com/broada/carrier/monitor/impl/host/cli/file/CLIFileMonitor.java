package com.broada.carrier.monitor.impl.host.cli.file;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.cli.CLIDateFormat;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class CLIFileMonitor extends BaseMonitor {
  static MonitorInstance[] toInstances(List<CLIFileMonitorCondition> files) {
  	MonitorInstance[] insts = new MonitorInstance[files.size()];
  	for (int i = 0; i < files.size(); i++) {
			CLIFileMonitorCondition cliFileMonitorCondition = files.get(i);
			insts[i] = new MonitorInstance(cliFileMonitorCondition.getField());      
		}    
		return insts;
  }
  
  static List<CLIFileMonitorCondition> toList(MonitorInstance[] instances) {
  	List<CLIFileMonitorCondition> files = new ArrayList<CLIFileMonitorCondition>();
  	for (int i = 0; instances != null && i < instances.length; i++) {
			CLIFileMonitorCondition cliFileMonitorCondition = new CLIFileMonitorCondition();
      cliFileMonitorCondition.setField(instances[i].getCode());
      files.add(cliFileMonitorCondition);      
		}    
		return files;
  }
  
  static CLIFileMonitorCondition[] toArray(MonitorInstance[] instances) {
  	CLIFileMonitorCondition[] fileList = new CLIFileMonitorCondition[instances == null ? 0 : instances.length];
		for (int i = 0; i < fileList.length; i++) {
			CLIFileMonitorCondition cliFileMonitorCondition = new CLIFileMonitorCondition();
      cliFileMonitorCondition.setField(instances[i].getCode());
      fileList[i] = cliFileMonitorCondition;      
		}    
		return fileList;
  }

	@Override
	public MonitorResult monitor(MonitorContext context) {
		return collect(context.getTask().getId(), new CollectContext(context), toArray(context.getInstances()));
	}

	@Override
	public Serializable collect(CollectContext context) {				
		CLIFileMonitorCondition[] fileList = context.getParameterObject(CLIFileMonitorCondition[].class);
    return CLIFileExecutor.getFileConditions("-1", context.getNode(), context.getMethod(), fileList);
	}

	private MonitorResult collect(String taskId, CollectContext context, CLIFileMonitorCondition[] fileList) {		
    MonitorResult result = new MonitorResult();
    try {
      fileList = CLIFileExecutor.getFileConditions(taskId, context.getNode(), context.getMethod(), fileList);
    } catch (Throwable e) {
    	return CLIExecutor.processError(e);            
    }

    List perfs = new ArrayList();
    StringBuffer loseFiles =new StringBuffer();
    StringBuffer mtimeFaildFiles = new StringBuffer();
    StringBuffer modifiedFiles = new StringBuffer();
    for (int index = 0; index < fileList.length; index++) {
      CLIFileMonitorCondition condition = fileList[index];
      if (!condition.isExists()) {
        append(loseFiles, condition.getField());
        PerfResult perf = new PerfResult(condition.getField(), "CLI-HOSTFILE-3", "不存在");
        perfs.add(perf);
      } else {
      	PerfResult perf = new PerfResult(condition.getField(), "CLI-HOSTFILE-3", "存在");
        perfs.add(perf);
      	
        // 大小性能项
        perf = new PerfResult(condition.getField(), "CLI-HOSTFILE-1", condition.getSize());
        perfs.add(perf);
        
        // 最后修改时间性能项
        Object currMtime = condition.getModifiedTime();
        PerfResult perfMtime = new PerfResult(condition.getField(), "CLI-HOSTFILE-2", currMtime);        
        perfs.add(perfMtime);
      }
    }

    StringBuffer descBuf = new StringBuffer();
    if (mtimeFaildFiles.length() > 0) {
      if (descBuf.length() > 0)
        descBuf.append(";");
      descBuf.append("获取文件").append(mtimeFaildFiles).append("最后修改时间失败");
    }
    if (modifiedFiles.length() > 0) {
      if (descBuf.length() > 0)
        descBuf.append(";");
      descBuf.append("文件").append(modifiedFiles).append("被修改");
    }
    
    if (result.getState() == MonitorState.SUCCESSED) {
      if (descBuf.length() > 0)
        result.setMessage(descBuf.toString().replaceAll(";", "\n"));      
    } else {
      if (loseFiles.length() > 0) {
        descBuf.append("文件").append(loseFiles).append("不存在");
      }
      result.setMessage(descBuf.toString().replaceAll(";", "\n"));
    }
    
    result.setPerfResults((PerfResult[]) perfs.toArray(new PerfResult[perfs.size()]));
    return result;
  }
    
  /**
   * 将修改时间格式化成"2007-11-7 11:12:52"的格式; mtime 可能有三种格式: "2007-11-7_11:12:52"、"Dec 28 17:01 2006"、"28 Dec 17:01 2006"
   * 
   * @param mtime
   * @return
   */
  public static Date formateMtimeToDatetim(String strMmtime) {
  	return CLIDateFormat.format(strMmtime);
  }

  private void append(StringBuffer buf, String value) {
    if (buf.length() > 0) {
      buf.append("、");
    }
    buf.append(value);
  }
}
