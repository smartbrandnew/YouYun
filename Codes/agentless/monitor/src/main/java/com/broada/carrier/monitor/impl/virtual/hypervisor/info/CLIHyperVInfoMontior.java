package com.broada.carrier.monitor.impl.virtual.hypervisor.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.utils.StringUtil;

public class CLIHyperVInfoMontior extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(CLIHyperVInfoMontior.class);
	
	@Override
	public Serializable collect(CollectContext collectcontext) {
		MonitorResult result = new MonitorResult();
		result.setState(MonitorState.SUCCESSED);
		Map<String, CLIHyperVInfoMonitorCondition> hyperV = null;
		try {
			CLIHyperVInfoExecutor executor = CLIHyperVInfoExecutorFactory.getHyperVExecutor(collectcontext.getTypeId());
			long replyTime = System.currentTimeMillis();
			hyperV = executor.getHyperVInfo(collectcontext);
			replyTime = System.currentTimeMillis() - replyTime;
			if(replyTime <= 0){  
				replyTime = 1L;
			}
			result.setResponseTime(replyTime);
		} catch (CLIConnectException e) {
		     result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开.");
		     result.setState(MonitorState.FAILED);
		     return result;
		} catch (CLILoginFailException e) {
		     result.setResultDesc("登录目标服务器失败，请检查监测配置的用户/密码等是否正确.");
		     result.setState(MonitorState.FAILED);
		     return result;
		} catch (CLIResultParseException e) {
		      result.setResultDesc("获取HyperV信息失败：" + e.getMessage());
		      if (logger.isErrorEnabled()) {
		        logger.error("获取HyperV信息失败.@"+collectcontext.getResource().getDomainId(), e);
		      }
		      result.setResultDesc("获取HyperV信息失败：" + e.getMessage());
		      result.setState(MonitorState.FAILED);
		      return result;
		} catch (Throwable e) {
			String msg = e.getMessage();
		      if (CLIExecutor.isErrorConnectFailed(msg)) {
		        result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开或者未启用RPC服务.");
		        result.setState(MonitorState.FAILED);
		        return result;
		      }
		      if (logger.isDebugEnabled()) {
		        logger.debug("获取HyperV信息失败", e);
		      }
		      result.setResultDesc("获取HyperV信息失败:" + e.getMessage());
		      result.setState(MonitorState.FAILED);
		      return result;
		}
		
		Iterator<Entry<String, CLIHyperVInfoMonitorCondition>> it = hyperV.entrySet().iterator();
		List<PerfResult> perfs = new ArrayList<PerfResult>();
		
		while(it.hasNext()){
			Entry<String, CLIHyperVInfoMonitorCondition> en = it.next();
			CLIHyperVInfoMonitorCondition c = en.getValue();
			if(c == null){
				result.setResultDesc((StringUtil.isNullOrBlank(result.getResultDesc()) ? "" : (result.getResultDesc() + ",")) + "HyperV:" + en.getKey() + "没有运行");
				result.setState(MonitorState.OVERSTEP);
			}else{
				PerfResult currentState = new PerfResult("CLI-hyperVState-1", c.getCurrentState());
				currentState.setInstanceKey(c.getField());
				perfs.add(currentState);
				
				PerfResult healthState = new PerfResult("CLI-hyperVState-2", transHealth(c.getHealthState()));
				healthState.setInstanceKey(c.getField());
				perfs.add(healthState);				
				
			  PerfResult onLineTime = new PerfResult("CLI-hyperVState-3", c.getOnLineTime());
			  onLineTime.setInstanceKey(c.getField());
        perfs.add(onLineTime);
        
        PerfResult installDate = new PerfResult("CLI-hyperVState-4", c.getInstallDate());
        installDate.setInstanceKey(c.getField());
        perfs.add(installDate);
        
        PerfResult guestOperatingSystem = new PerfResult("CLI-hyperVState-5", c.getGuestOperatingSystem());
        guestOperatingSystem.setInstanceKey(c.getField());
        perfs.add(guestOperatingSystem);
        
        PerfResult memoryUsage = new PerfResult("CLI-hyperVState-6", c.getMemoryUsage());
        memoryUsage.setInstanceKey(c.getField());
        perfs.add(memoryUsage);
        
        PerfResult numberOfProcessors = new PerfResult("CLI-hyperVState-7", c.getNumberOfProcessors());
        numberOfProcessors.setInstanceKey(c.getField());
        perfs.add(numberOfProcessors);
        
        PerfResult timeOfLastStateChange = new PerfResult("CLI-hyperVState-8", c.getTimeOfLastStateChange());
        timeOfLastStateChange.setInstanceKey(c.getField());
        perfs.add(timeOfLastStateChange);
        
        PerfResult notes = new PerfResult("CLI-hyperVState-9", c.getNotes());
        notes.setInstanceKey(c.getField());
        perfs.add(notes);
        
				if(!c.getHealthState().equals("5")){
					result.setResultDesc("管理的虚拟机：" + c.getField() + "运行时存在问题！");
					result.setState(MonitorState.OVERSTEP);
				}				
			}
		}
		result.setPerfResults(perfs.toArray(new PerfResult[perfs.size()]));
		
		return result;
	}
	
	/**
	 * 健康状态转换
	 * @param healthState
	 * @return
	 */
  public String transHealth(String healthState) {
    if ("5".equals(healthState)) {
      healthState = "正常";
    } else if ("20".equals(healthState)) {
      healthState = "存在故障";
    } else if ("25".equals(healthState)) {
      healthState = "严重错误";
    } else {
      healthState = "未知";
    }
    return healthState;
  }
	
	
}
