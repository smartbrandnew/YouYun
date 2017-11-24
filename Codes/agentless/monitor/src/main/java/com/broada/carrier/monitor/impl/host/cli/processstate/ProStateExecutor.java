package com.broada.carrier.monitor.impl.host.cli.processstate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.component.utils.error.ErrorUtil;

public class ProStateExecutor {
	private static final Logger logger = LoggerFactory.getLogger(ProStateExecutor.class);
	
  public Map getProcesses(String taskId, MonitorNode node, MonitorMethod method) throws CLIException {
  	CLIMonitorMethodOption options;
  	if (method instanceof CLIMonitorMethodOption)
  		options = (CLIMonitorMethodOption) method;
  	else
  		options = new CLIMonitorMethodOption(method);
  	String sysName = options.getSysname();
  	
  	Map processConds = new HashMap();  	
  	Throwable firstError = null;  	
    //如果是sysName = linux
  	int deadProcessCount = 0;
  	if(sysName.equalsIgnoreCase("linux")) {
  		CLIResult result = new CLIExecutor(taskId).execute(node, method, CLIConstant.COMMAND_PROCESSSTATE);
  		List processes = result.getListTableResult();//tables  		
	    for (int index = 0; index < processes.size(); index++) {
	      Properties properties = (Properties) processes.get(index);
	      String provsize = (String) properties.get("provsize");	      
	      String promonth = (String) properties.get("promonth");
	      String proday = (String) properties.get("proday");
	      String protime = (String) properties.get("protime");
	      String proyear = (String) properties.get("proyear");
	      String proetime = (String) properties.get("proetime");
	      String proname = (String) properties.get("proname");
	      if (proname == null)
	      	continue;
	    	if (proname.contains("<defunct") 
	    			|| proname.contains("<idle")) {
	    		deadProcessCount++;
	    		continue;
	    	}
	      //处理启动时间
	      String prolstart = dealLstart(proyear,promonth,proday,protime);
	      //处理持续时间
	      proetime = proetime.replace("-", "天 ");
	      
	      CLIProStateMonitorCondition c = (CLIProStateMonitorCondition) processConds.get(proname);
	      try {
	        if (c == null) {
	          CLIProStateMonitorCondition cond = new CLIProStateMonitorCondition();
	          cond.setField(proname);//表示要进行比较的字段
	          cond.setCurrentVsize(provsize);
	          cond.setVsize("100000");
	          cond.setCurrentLstart(prolstart);
	          cond.setCurrentEtime(proetime);
	          processConds.put(proname, cond);
	        } else {
	          if (Float.parseFloat(c.getCurrentVsize()) < Float.parseFloat(provsize)) {
	            c.setCurrentVsize(provsize);
	          }
	        }
	      } catch (Throwable e) {
	      	if (firstError == null)
	      		firstError = e;
	      	ErrorUtil.warn(logger, "解析进程指标失败：" + properties, e);				
	      }
	    }
  	} else if(sysName.equalsIgnoreCase("windows")) {    //如果是sysName = windows
  		CLIResult result = new CLIExecutor(taskId).execute(node, method, CLIConstant.COMMAND_PROCESSSTATE);
  		List processes = result.getListTableResult();//tables
  		for (int index = 0; index < processes.size(); index++) {
	      Properties properties = (Properties) processes.get(index);
	      if(properties.size() < 4){
          continue;
        }
	      String proname = (String) properties.get("proname");
	      String provsize = (String) properties.get("provsize");
	      String prolstart = (String) properties.get("prolstart");
	      String systime = (String) properties.get("systime");

	      //处理启动时间
	      if (prolstart == "empty" || prolstart.equals("empty")) {
	      	prolstart = "";
	      } else {
		  		prolstart = prolstart.split("\\.")[0];
		      StringBuilder sb=new StringBuilder(prolstart);
		      sb = sb.insert(4, "-").insert(7, "-").insert(10, " ").insert(13, ":").insert(16, ":");
		      prolstart = sb.toString();
	      }
	      //处理持续时间
	      String proetime = dealEtime(prolstart, systime);
	      
	      CLIProStateMonitorCondition c = (CLIProStateMonitorCondition) processConds.get(proname);
	      try {
	        if (c == null) {
	          CLIProStateMonitorCondition cond = new CLIProStateMonitorCondition();
	          cond.setField(proname);//表示要进行比较的字段
	          cond.setCurrentVsize(provsize);
	          cond.setVsize("100000");
	          cond.setCurrentLstart(prolstart);
	          cond.setCurrentEtime(proetime);
	          processConds.put(proname, cond);
	        } else {
	          if (Float.parseFloat(c.getCurrentVsize()) < Float.parseFloat(provsize)) {
	            c.setCurrentVsize(provsize);
	          }
	        }
	      } catch (Throwable e) {
	      	if (firstError == null)
	      		firstError = e;
	      	ErrorUtil.warn(logger, "解析进程指标失败：" + properties, e);				
	      }
	    }
  	} else {
  		//获取系统当前时间
  		CLIResult resultSysTime = new CLIExecutor(taskId).execute(node, method, CLIConstant.COMMAND_SYSTEMTIME);
  		Properties propertiesSysTime = resultSysTime.getPropResult();//block//返回properties
  		String systemTime = (String) propertiesSysTime.get("systemtime");
      
      //获取进程名称、虚拟内存，持续时间
  		CLIResult result = new CLIExecutor(taskId).execute(node, method, CLIConstant.COMMAND_PROCESSSTATE);
  		List processes = result.getListTableResult();//tables
	    for (int index = 0; index < processes.size(); index++) {
	      Properties properties = (Properties) processes.get(index);
	      if(properties.size() < 3){
          continue;
        }
	      String proname = (String) properties.get("proname");
	      if (proname == null)
	      	continue;
	    	if (proname.contains("<defunct") 
	    			|| proname.contains("<idle")) {
	    		deadProcessCount++;
	    		continue;
	    	}
	    	
	      String provsize = (String) properties.get("provsize");
	      String proetime = (String) properties.get("proetime");	      
	      //处理启动时间
	      String prolstart = countStime(systemTime, proetime);
	      //处理持续时间
	      proetime = proetime.replace("-", "天 ");
	      
	      CLIProStateMonitorCondition c = (CLIProStateMonitorCondition) processConds.get(proname);
	      try {
	        if (c == null) {
	          CLIProStateMonitorCondition cond = new CLIProStateMonitorCondition();
	          cond.setField(proname);//表示要进行比较的字段
	          cond.setCurrentVsize(provsize);
	          cond.setVsize("100000");
	          cond.setCurrentLstart(prolstart);
	          cond.setCurrentEtime(proetime);
	          processConds.put(proname, cond);
	        } else {
	          if (Float.parseFloat(c.getCurrentVsize()) < Float.parseFloat(provsize)) {
	            c.setCurrentVsize(provsize);
	          }
	        }
	      } catch (Throwable e) {
	      	if (firstError == null)
	      		firstError = e;
	      	ErrorUtil.warn(logger, "解析进程指标失败：" + properties, e);				
	      }
	    }
  	} 
  	
  	if (processConds.size() == 0 && firstError != null)
    	throw new CLIResultParseException(ErrorUtil.createMessage("解析进程指标失败", firstError), firstError);    
    processConds.put("deadProcessThreshold", new Integer(deadProcessCount));
    return sortProcess(processConds);  	
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

	/**
   * 处理进程启动时间
   * @param proyear
   * @param promonth
   * @param proday
   * @param protime
   * @return
   */
  private String dealLstart(String proyear, String promonth, String proday, String protime) {
    if(promonth.equalsIgnoreCase("Jan")) {
    	promonth = "1";
    } else if(promonth.equalsIgnoreCase("Feb")) {
    	promonth = "2";
    } else if(promonth.equalsIgnoreCase("Mar")) {
    	promonth = "3";
    } else if(promonth.equalsIgnoreCase("Apr")) {
    	promonth = "4";
    } else if(promonth.equalsIgnoreCase("May")) {
    	promonth = "5";
    } else if(promonth.equalsIgnoreCase("Jun")) {
    	promonth = "6";
    } else if(promonth.equalsIgnoreCase("Jul")) {
    	promonth = "7";
    } else if(promonth.equalsIgnoreCase("Aug")) {
    	promonth = "8";
    } else if(promonth.equalsIgnoreCase("Sep") || promonth.equalsIgnoreCase("Sept")) {
    	promonth = "9";
    } else if(promonth.equalsIgnoreCase("Oct")) {
    	promonth = "10";
    } else if(promonth.equalsIgnoreCase("Nov")) {
    	promonth = "11";
    } else if(promonth.equalsIgnoreCase("Dec")){
    	promonth = "12";
    } else {
    	return "";
    }
    String[] times = protime.split(":");
    GregorianCalendar gc = new GregorianCalendar(
    		Integer.parseInt(proyear),
    		Integer.parseInt(promonth),
    		Integer.parseInt(proday),
    		Integer.parseInt(times[0]),
    		Integer.parseInt(times[1]),
    		Integer.parseInt(times[2])
    );
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
    return df.format(gc.getTime());
  }
  
  private String countStime(String systemTime, String etime) {
  	int indexOf = etime.indexOf("-");		
		int second = 0;
		long milliSecond = 0;
    if(indexOf == -1) {
  		String[] strs = etime.split(":");
  		if(strs.length == 2) {
  			second = Integer.parseInt(strs[0])*60 + Integer.parseInt(strs[1]);
  			milliSecond = second * 1000;
  		} else if (strs.length == 3) {
  			second = Integer.parseInt(strs[0])*3600 + Integer.parseInt(strs[1])*60 + Integer.parseInt(strs[2]);
  			milliSecond = second * 1000;
  		}
    } else {
    	String[] strs = etime.split("-");
    	second = Integer.parseInt(strs[0])*24*3600;
    	String[] strs2 = strs[1].split(":");
    	second = second + Integer.parseInt(strs2[0])*3600 + Integer.parseInt(strs2[1])*60 + Integer.parseInt(strs2[2]);
    	milliSecond = second*1000;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    long millisecondSystime;
		try {
			millisecondSystime = sdf.parse(systemTime).getTime();
	    long millisecondStime = millisecondSystime - milliSecond;
			return sdf.format((new Date(millisecondStime)));
		} catch (ParseException e) {
			
		}
		return "";
  }
  
  private String dealEtime(String prolstart, String proetime) {
  	if(prolstart == null || prolstart.equals("")) {
  		return "";
  	}
  	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long lstartMilliSecond = 0;
		long sysMilliSecond = 0;
		try {
			lstartMilliSecond = sdf.parse(prolstart).getTime();
			sysMilliSecond = sdf.parse(proetime).getTime();
		} catch (ParseException e) {
			return "";
		}
		long differenceSecond = (sysMilliSecond - lstartMilliSecond)/1000;
		
		int day = (int)(differenceSecond/3600/24);
		int hour = (int)((differenceSecond - day*24*3600)/3600);
		int minite = (int)((differenceSecond - day*24*3600 - hour*3600)/60);
		int second = (int)(differenceSecond - day*24*3600 - hour*3600 - minite*60);
		if(day<=0 && hour<=0 && minite<=0 && second<=0) {
			return "00:00";
		} else if (day<=0 && hour<=0 && minite<=0) {
			String secondStr = String.valueOf(second);
			if(secondStr.length()==1) {
				secondStr = "0" + secondStr;
			}
			return "00:" + secondStr;
		} else if (day<=0 && hour<=0) {
			String miniteStr = String.valueOf(minite);
			String secondStr = String.valueOf(second);
			if(miniteStr.length()==1) {
				miniteStr = "0" + miniteStr;
			}
			if(secondStr.length()==1) {
				secondStr = "0" + secondStr;
			}
			return miniteStr + ":" + secondStr;
		} else if (day<=0) {
			String hourStr = String.valueOf(hour);
			String miniteStr = String.valueOf(minite);
			String secondStr = String.valueOf(second);
			if(hourStr.length()==1) {
				hourStr = "0" + hourStr;
			}
			if(miniteStr.length()==1) {
				miniteStr = "0" + miniteStr;
			}
			if(secondStr.length()==1) {
				secondStr = "0" + secondStr;
			}
			return hourStr+":"+miniteStr+":"+secondStr;
		} else {
			String hourStr = String.valueOf(hour);
			String miniteStr = String.valueOf(minite);
			String secondStr = String.valueOf(second);
			if(hourStr.length()==1) {
				hourStr = "0" + hourStr;
			}
			if(miniteStr.length()==1) {
				miniteStr = "0" + miniteStr;
			}
			if(secondStr.length()==1) {
				secondStr = "0" + secondStr;
			}
			return day+"天 "+hourStr+":"+miniteStr+":"+secondStr;
		}
  }  
}