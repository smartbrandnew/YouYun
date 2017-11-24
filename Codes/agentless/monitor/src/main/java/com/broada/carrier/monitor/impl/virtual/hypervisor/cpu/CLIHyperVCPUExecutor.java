package com.broada.carrier.monitor.impl.virtual.hypervisor.cpu;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.Properties;

public class CLIHyperVCPUExecutor {
  
	public Map<String, CLIHyperVCPUBean> getHyperVCpuInfo(CollectContext context) throws Exception{
		CLIMonitorMethodOption options = new CLIMonitorMethodOption(context.getMethod());
		String sysName = options.getSysname();
		
		Map<String, CLIHyperVCPUBean> hyperVConds = new HashMap<String, CLIHyperVCPUBean>();
		
		if(sysName.equalsIgnoreCase("windows")){
			CLIResult result = new CLIExecutor("-1").execute(context.getNode(), options, "hyperVCPU");
			List<Properties> hyperV = result.getListTableResult();
			for(int index = 0; index < hyperV.size();index++){
				Properties properties = (Properties) hyperV.get(index);			
				String loadPercentage = properties.getProperty("LoadPercentage").toString();
				String name = properties.getProperty("Name");			
				String caption = properties.getProperty("Caption");
				CLIHyperVCPUBean hcp=new CLIHyperVCPUBean();
				hcp.setCaption(caption);
				hcp.setName(name);
				hcp.setLoadPercentage(loadPercentage);
				hyperVConds.put(name, hcp);
			}
			if(hyperV.size() == 0)throw new Exception("服务器没有安装虚拟机或因权限不足无法连接服务器");
		}else{
			throw new RuntimeException("HyperV监测器只支持windows操作系统！");
		}
		
		return sort(hyperVConds);
	}

	private Map<String,CLIHyperVCPUBean> sort(Map<String,CLIHyperVCPUBean> hyperVConds) {
		String[] keys = hyperVConds.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		
		Map<String,CLIHyperVCPUBean> result = new LinkedHashMap<String, CLIHyperVCPUBean>();
		for(String key:keys){
			result.put(key, hyperVConds.get(key));
		}
		return result;
	}

	//计算虚拟机运行时间	
	private String formateTimeMill(String timeMill){
	  String timeMills="0:0:0";
    try {
      long time = Long.parseLong(timeMill);
      long s = time / 1000;
      long H = s / 3600;
      s = s % 3600;
      long M = s / 60;
      s = s % 60;
      long second = s;
      timeMills=H+":"+M+":"+second;
    } catch (Exception e) {
      timeMills="0:0:0";
    }
    return timeMills;
	}
}
