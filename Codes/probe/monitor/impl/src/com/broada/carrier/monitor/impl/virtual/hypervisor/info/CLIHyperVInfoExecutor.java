package com.broada.carrier.monitor.impl.virtual.hypervisor.info;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import edu.emory.mathcs.backport.java.util.Arrays;

import java.util.Properties;

/**
 * BCC中实现与资源连接的类
 * 
 * @author 
 * Create By 2015-12-15 下午3:06:32
 */
public class CLIHyperVInfoExecutor {
  
	public Map<String, CLIHyperVInfoMonitorCondition> getHyperVInfo(CollectContext context) throws Exception{
		CLIMonitorMethodOption options = new CLIMonitorMethodOption(context.getMethod());
		String sysName = options.getSysname();
		
		Map<String, CLIHyperVInfoMonitorCondition> hyperVConds = new HashMap<String, CLIHyperVInfoMonitorCondition>();
		
		if(sysName.equalsIgnoreCase("windows")){
			MonitorNode monitorNode = context.getNode();
			CLIResult result = null;
			try{
				CLIExecutor clte = new CLIExecutor("-1");
				result = clte.execute(monitorNode, options, "hyperV", 0);
			}catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			List<Properties> hyperV = result.getListTableResult();
			for(int index = 0; index < hyperV.size(); index++){
				Properties properties = (Properties) hyperV.get(index);			
				String name = properties.getProperty("ElementName").toString();
				String installDate = formateDate(properties.getProperty("InstallDate"));			
				String healthState = properties.getProperty("HealthState");
				String enabledState= properties.getProperty("EnabledState");
				String onTimeInMilliseconds=formateTimeMill(properties.getProperty("OnTimeInMilliseconds"));				
			  String caption=properties.getProperty("Caption");
			  String timeOfLastStateChange = formateDate(properties.getProperty("TimeOfLastStateChange"));
			  String guestOperatingSystem = properties.getProperty("GuestOperatingSystem");
			  String notes = properties.getProperty("Notes");
			  String memoryUsage = properties.getProperty("MemoryUsage");
			  String numberOfProcessors = properties.getProperty("NumberOfProcessors");
			  String processorLoad = properties.getProperty("ProcessorLoad");
				CLIHyperVInfoMonitorCondition c = (CLIHyperVInfoMonitorCondition) hyperVConds.get(name);
				if(c == null){
					CLIHyperVInfoMonitorCondition cond = new CLIHyperVInfoMonitorCondition();
					cond.setField(name);
					cond.setInstallDate(installDate);
					cond.setHealthState(healthState);
					cond.setCurrentState(enabledState);
					cond.setOnLineTime(onTimeInMilliseconds);
					cond.setCaption(caption);
					cond.setTimeOfLastStateChange(timeOfLastStateChange);
					cond.setGuestOperatingSystem(guestOperatingSystem);
					cond.setNotes(notes);
					cond.setMemoryUsage(memoryUsage);
					cond.setNumberOfProcessors(numberOfProcessors);
					cond.setProcessorLoad(processorLoad);
					hyperVConds.put(name, cond);
				}
			}
			if(hyperV.size() == 0)throw new Exception("服务器没有安装虚拟机或因权限不足无法连接服务器");
		}else{
			throw new RuntimeException("HyperV监测器只支持windows操作系统！");
		}
		
		return sort(hyperVConds);
	}

	private Map<String, CLIHyperVInfoMonitorCondition> sort(Map<String, CLIHyperVInfoMonitorCondition> hyperVConds) {
		String[] keys = hyperVConds.keySet().toArray(new String[0]);
		Arrays.sort(keys);
		
		Map<String, CLIHyperVInfoMonitorCondition> result = new LinkedHashMap<String, CLIHyperVInfoMonitorCondition>();
		for(String key:keys){
			result.put(key, hyperVConds.get(key));
		}
		return result;
	}

	//日期格式化
	private String formateDate(String str){
	  DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
	  String reTime=str;
    try {
      Date dDate = format.parse(str);
      DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
       reTime= format2.format(dDate);      
    } catch (ParseException e) {
      reTime=str;
    }  
    return reTime;
	  
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
