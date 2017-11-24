package com.broada.carrier.monitor.impl.virtual.hypervisor.cpu;

import java.io.Serializable;
import java.util.Iterator;
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
/**
 * CPU使用率监测器实现
 * 
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: NMS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author Maico Pang
 * @version 1.0
 */

public class CLIHyperVCPUMonitor extends BaseMonitor {

	private static final Log logger = LogFactory.getLog(CLIHyperVCPUMonitor.class);
  
	public CLIHyperVCPUMonitor() {
	}

	@Override
	public Serializable collect(CollectContext collectcontext) {
		MonitorResult result = new MonitorResult();
		result.setState(MonitorState.SUCCESSED);
		Map<String, CLIHyperVCPUBean>  hyperVcpu = null;
		try {
			CLIHyperVCPUExecutor executor = new CLIHyperVCPUExecutor();

			long replyTime = System.currentTimeMillis();
			hyperVcpu = executor.getHyperVCpuInfo(collectcontext);
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0) {
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
			result.setResultDesc("获取HyperVCPU信息失败：" + e.getMessage());
			if (logger.isErrorEnabled()) {
				logger.error("获取HyperVCPU信息失败.@" + collectcontext.getResource().getDomainId(), e);
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
				logger.debug("获取HyperVCPU信息失败", e);
			}
			result.setResultDesc("获取HyperV信息失败:" + e.getMessage());
			result.setState(MonitorState.FAILED);
			return result;
		}

		boolean state = true;
		StringBuffer msgSB = new StringBuffer();
		
		Iterator<Entry<String, CLIHyperVCPUBean>> it = hyperVcpu.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, CLIHyperVCPUBean> en = it.next();
			CLIHyperVCPUBean c = en.getValue();
			//MonitorCondition condition = p.getConditionByName(c.getName());
			//PerfResult name = new PerfResult("CLI-hyperVCPU-1", c.getName());
			//name.setInstanceKey(c.getName());
			//result.addPerfResult(name);

			PerfResult type = new PerfResult("CLI-hyperVCPU-2", c.getCaption());
			type.setInstanceKey(c.getName());
			result.addPerfResult(type);

			PerfResult cpuValue = new PerfResult("CLI-hyperVCPU-3", c.getLoadPercentage());
			cpuValue.setInstanceKey(c.getName());
			result.addPerfResult(cpuValue);

			//if (condition != null) {
			//	if (Double.valueOf(c.getLoadPercentage()) > Double.valueOf(condition.getValue())) {
			//		msgSB.append(c.getName() + "的CPU使用率为" + c.getLoadPercentage() + "% > " + condition.getValue() + "%;\n");
			//		state = false;
			//	}
			//}
		}
		if (!state) {
			result.setResultDesc(msgSB.toString());
		}
		result.setState(state ? MonitorState.SUCCESSED : MonitorState.OVERSTEP);

		return result;
	}

	/*
	public MonitorResult doMonitor(MonitorService srv) {
	  MonitorResult result = new MonitorResult();
	  result.setState(MonitorConstant.MONITORSTATE_NICER);
	 Map hyperVcpu=null;
	  CLIHyperVCPUParameter p=new CLIHyperVCPUParameter(srv.getParameter(),true);
	  try {
      CLIHyperVCputExecutor executor = new CLIHyperVCputExecutor();
    
      long replyTime = System.currentTimeMillis();
      hyperVcpu =   executor.getHyperVCpu(srv, p.getMonitorMethodParamId());
      replyTime = System.currentTimeMillis() - replyTime;
      if(replyTime <= 0){  
        replyTime = 1L;
      }
      result.setResponseTime(replyTime);
    } catch (CLIConnectException e) {
       result.setCurrentVal("获取HyperVCPU信息失败,无法连接目标服务器.");
         result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开.");
         result.setState(MonitorConstant.MONITORSTATE_FAILING);
         return result;
    } catch (CLILoginFailException e) {
       result.setCurrentVal("获取HyperVCPU信息失败,无法登录目标服务器.");
         result.setResultDesc("登录目标服务器失败，请检查监测配置的用户/密码等是否正确.");
         result.setState(MonitorConstant.MONITORSTATE_FAILING);
         return result;
    } catch (CLIResultParseException e) {
        result.setCurrentVal("获取HyperVCPU信息失败,解析采集结果失败.");
          result.setResultDesc("获取HyperVCPU信息失败：" + e.getMessage());
          if (logger.isErrorEnabled()) {
            logger.error("获取HyperVCPU信息失败.@"+srv.getMonitorNode().getIpAddress(), e);
          }
          result.setResultDesc("获取HyperVCPU信息失败：");
          result.setState(MonitorConstant.MONITORSTATE_CANCEL);
          return result;
    } catch (Throwable e) {
      String msg = e.getMessage();
          if (CLIExecutor.isErrorConnectFailed(msg)) {
            result.setCurrentVal("获取HyperVCPU信息失败,无法连接目标服务器.");
            result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开或者未启用RPC服务.");
            result.setState(MonitorConstant.MONITORSTATE_FAILING);
            return result;
          }
          if (logger.isDebugEnabled()) {
            logger.debug("获取HyperVCPU信息失败", e);
          }
          result.setCurrentVal("获取HyperVCPU信息失败.");
          result.setResultDesc("获取HyperVCPU信息失败.");
          result.setState(MonitorConstant.MONITORSTATE_FAILING);
          return result;
    } 
    

    List<PerfResult> perfs = new ArrayList<PerfResult>();
    boolean state = true;
    StringBuffer msgSB = new StringBuffer();
    if (hyperVcpu != null && hyperVcpu.size()>0) {
      for (Object object : hyperVcpu.entrySet()) {
        Map.Entry entry = (Map.Entry) object;
        HyperVCpuBean c = (HyperVCpuBean) entry.getValue();
        MonitorCondition condition = p.getConditionByName(c.getName());
        PerfResult name = new PerfResult("CLI-hyperVCPU-1",c.getName(),true);
        name.setInstanceKey(c.getName());
        perfs.add(name);
        
        PerfResult type = new PerfResult("CLI-hyperVCPU-2",c.getCaption(),true);
        type.setInstanceKey(c.getName());
        perfs.add(type);
        
        PerfResult cpuValue = new PerfResult("CLI-hyperVCPU-3",c.getLoadPercentage(),true);
        cpuValue.setInstanceKey(c.getName());
        perfs.add(cpuValue);
        
        if (condition != null) {
          if (Double.valueOf(c.getLoadPercentage()) > Double.valueOf(condition.getValue())) {
            msgSB.append(c.getName() + "的CPU使用率为" + c.getLoadPercentage() + "% > " + condition.getValue() + "%;\n");
            state = false;
          }
        }
      }
    } else {
      msgSB.append("获取HyperVCPU信息失败,可能未启动虚拟机。");
      state = false;
    }
      if(!state){
        result.setResultDesc(msgSB.toString());
        result.setCurrentVal(msgSB.toString());
      }
      result.setPerfResults(perfs.toArray(new PerfResult[perfs.size()]));
      result.setState(state ? MonitorConstant.MONITORSTATE_NICER : MonitorConstant.MONITORSTATE_OVERSTEP);
   
		return result;
	}

	

	@Override
	public CollectResult doCollect(MonitorNode node, MonitorResource resource, MonitorMethodParameter parameter,
			CallParameter[] callParams) throws CollectException {	  
	  int srvId = callParams[0].asIntValue();
    CLIHyperVCputExecutor executor = new CLIHyperVCputExecutor();	 
    MonitorService monitorService = new MonitorService(srvId);
    monitorService.setMonitorNode(node);
    Map map;
    try {
      map = executor.getHyperVCpu(monitorService, parameter.getMonitorMethodParameterId());
    } catch (CLIException e) {
      logger.error("系统错误", e);
      throw new CollectException(e);
    }
    
    MonitorInstance[] monitorInstances = new MonitorInstance[map.size()];
    List resultList = new ArrayList();
    int i = 0;
    for (Object object : map.entrySet()) {
      Map.Entry entry = (Map.Entry) object;
      HyperVCpuBean c = (HyperVCpuBean) entry.getValue();
      String index = String.valueOf(i);
      monitorInstances[i] = new MonitorInstance(srvId, c.getName(), c.getName());
      PerfResult result1 = new PerfResult("name", c.getName(), true);
      PerfResult result2 = new PerfResult("caption", c.getCaption(), true);
      PerfResult result3 = new PerfResult("loadPercentage", c.getLoadPercentage(), true);
      
      result1.setInstanceKey(c.getName());   
      result2.setInstanceKey(c.getName());
      result3.setInstanceKey(c.getName());
      
      resultList.add(result1);      
      resultList.add(result2);
      resultList.add(result3);
      i++;
    }
    CollectResult collectResult = new CollectResult();
    collectResult.setMonitorInstances(monitorInstances);
    PerfResult[] perfResults = new PerfResult[resultList.size()];
    resultList.toArray(perfResults);
    collectResult.setPerformances(perfResults);
    logger.debug("Probe数据采集结束");
	  return collectResult;	
	}
	*/
	
	
	
}
