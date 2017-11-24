package com.broada.carrier.monitor.method.cli;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.entity.CLIErrorLine;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.method.cli.error.CLIWaitResponseTimeoutException;
import com.broada.carrier.monitor.method.cli.parser.EmptyCLIResult;
import com.broada.carrier.monitor.method.cli.pool.CollectorPool;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.component.utils.error.ErrorUtil;

public class CLIExecutor {

  private final static Log logger = LogFactory.getLog(CLIExecutor.class);
  private final static int MAX_RETRYTIMES = Integer.parseInt(System.getProperty("monitor.cli.retry")==null?"2":System.getProperty("monitor.cli.retry"));
  private final static int RETRY_WAITTIME = Integer.parseInt(System.getProperty("monitor.cli.retry.interval")==null?"15000":System.getProperty("monitor.cli.retry.interval"));
  private String serviceId = "-1";

  public CLIExecutor(String serviceId) {
    this.serviceId = serviceId;
  }

  public CLIResult execute(MonitorNode monitorNode, MonitorMethod option, String cmd) throws CLIConnectException,
      CLILoginFailException, CLIResultParseException, CLIException {
    return execute(monitorNode, option, cmd, null, 0, null);
  }

  public CLIResult execute(MonitorNode monitorNode, MonitorMethod option, String cmd,
      AfterExecuteListener afterExecuteListener) throws CLIConnectException, CLILoginFailException,
      CLIResultParseException, CLIException {
    return execute(monitorNode, option, cmd, null, 0, afterExecuteListener);
  }

  public CLIResult execute(MonitorNode monitorNode, MonitorMethod option, String cmd, String[] params)
      throws CLIConnectException, CLILoginFailException, CLIResultParseException, CLIException {
    return execute(monitorNode, option, cmd, params, 0, null);
  }

  public CLIResult execute(MonitorNode monitorNode, MonitorMethod option, String cmd, int tryTimes)
			throws CLIConnectException, CLILoginFailException, CLIResultParseException, CLIException {
		return execute(monitorNode, option, cmd, null, tryTimes, null);
	}
  
  public CLIResult execute(MonitorNode monitorNode, MonitorMethod option, String cmd, int tryTimes,
      AfterExecuteListener afterExecuteListener) throws CLIConnectException, CLILoginFailException,
      CLIResultParseException, CLIException {
    return execute(monitorNode, option, cmd, null, tryTimes, afterExecuteListener);
  }
  
  public CLIResult execute(MonitorNode monitorNode, MonitorMethod option, String cmd, int tryTimes, String[] params)
      throws CLIConnectException, CLILoginFailException, CLIResultParseException, CLIException {
    return execute(monitorNode, option, cmd, params, tryTimes, null);
  }
  
  /**
   * 供外部调用的方法采集调用方法
   * @param monitorNode
   * @param monitorMethodParamId
   * @param cmd
   * @param params
   * @param tryTimes
   * @param afterExecuteListener
   * @return
   * @throws CLIConnectException
   * @throws CLILoginFailException
   * @throws CLIResultParseException
   * @throws CLIException
   */
  public CLIResult execute(MonitorNode monitorNode, MonitorMethod option, String cmd, String[] params,
      int tryTimes, AfterExecuteListener afterExecuteListener) throws CLIConnectException, CLILoginFailException,
      CLIResultParseException, CLIException {
  	return innerExecute(monitorNode, option, cmd, params, tryTimes, afterExecuteListener);
  }

  /**
   * 加入retry机制
   * 
   * monify by caikang
   * 2011.07.08
   * 
   * 
   * @param monitorNode
   * @param monitorMethodParamId
   * @param cmd
   * @param params
   * @param tryTimes
   * @param afterExecuteListener
   * @param isRetry
   * @return
   * @throws CLIConnectException
   * @throws CLILoginFailException
   * @throws CLIResultParseException
   * @throws CLIException
   */
  private CLIResult innerExecute(MonitorNode monitorNode, MonitorMethod method, String cmd, String[] params,
      int tryTimes, AfterExecuteListener afterExecuteListener) throws CLIConnectException, CLILoginFailException,
      CLIResultParseException, CLIException {
  	CLIMonitorMethodOption options;
  	if (method instanceof CLIMonitorMethodOption)
  		options = (CLIMonitorMethodOption) method;
  	else
  		options = new CLIMonitorMethodOption(method);
  	
  	tryTimes++;
    CLICollector collector = null;    
    try {
      //MDF BY panghf 2011-10-12 为了控制日志的输出，如果没到最后一次的话将不输出错误日志，避免日志太多影响分析
      boolean isLogErr=false;
      if(tryTimes==0){
      	isLogErr=true;
      }      
      
    	if (logger.isDebugEnabled())
    		logger.debug(String.format("CLI采集[%s:%s]，第[%d]次采集尝试",  monitorNode.getIp(), options.getRemotePort(), tryTimes));
      
      collector = CollectorPool.getCLICollector(options.toOptions(monitorNode.getIp()), serviceId, isLogErr);
      
      //add panghf 2011-10-12 为了方便一些结果解析代码可以log节点、命令相关信息加入
      ThreadLocal<String> nodeInfo=new ThreadLocal<String>();
      nodeInfo.set("节点:"+monitorNode.getIp()+",命令:"+cmd);
      CLIResult result = collector.execute(cmd, params, afterExecuteListener,isLogErr);
      nodeInfo.remove();
    	collector.close();			
      if (result == null)
    	  result = new EmptyCLIResult();
              
      return result;
    } catch (CLIException e) {
    	if (collector != null) {
    		try {
    			collector.destroy();			
    		} catch (Exception e1) {
    			logger.warn(String.format("关闭CLI采集器失败。错误：%s", e1));
    			logger.debug("堆栈：", e1);
    		}  	           		
    	}       
    	
    	if (e instanceof CLILoginFailException || e instanceof CLIConnectException || e instanceof CLIWaitResponseTimeoutException) {	// 如果是采集失败或登录失败，则可以尝试重试
    		//达到最大重试次数，抛出异常
        if (tryTimes >= MAX_RETRYTIMES) {
        	logger.warn(String.format("CLI采集[%s:%s]，第[%d]次采集失败，达到最大重试次数。错误：%s",  monitorNode.getIp(), options.getRemotePort(), tryTimes, e));
  				logger.debug("堆栈：", e);
          throw e;
        } else {
        	logger.warn(String.format("CLI采集[%s:%s]，第[%d]次采集失败，等待指定时间继续尝试。错误：%s",  monitorNode.getIp(), options.getRemotePort(), tryTimes, e));
  				logger.debug("堆栈：", e);
  	    	try {
  	        Thread.sleep(RETRY_WAITTIME);
  	      } catch (InterruptedException e1) {
  	    	  String msg = "CLI采集等待被中断";
  	      	logger.warn(String.format("%s。错误：%s", msg, e1));
  					logger.debug("堆栈：", e1);
  	      	return new EmptyCLIResult(new CLIErrorLine[]{new CLIErrorLine(-1, msg)});
  	      }
  	    	return innerExecute(monitorNode, options, cmd, params, tryTimes, afterExecuteListener);
        }     
    	} else {
    		logger.warn(String.format("CLI采集[%s:%s]，执行CLI操作失败。错误：%s",  monitorNode.getIp(), options.getRemotePort(), e));
				logger.debug("堆栈：", e);
        throw e;
    	}
    }
  }
  
  /**
   * 判断一个错误字符串是否表示目标主机没有启动或无法连接导致监测失败
   * @param msg
   * @return
   */
  public static boolean isErrorConnectFailed(String msg) {
  	if (msg == null)
  		return false;
  	return msg.contains("Connection reset by peer") || msg.contains("RPC 服务器不可用") || msg.contains("RPC server is unavailable");
  }
  
	public static MonitorResult processError(Throwable e) {
		ErrorUtil.warn(logger, "监测采集时碰到异常", e);
		
		MonitorResult result = new MonitorResult();
		result.setState(MonitorState.FAILED);
		if (e instanceof CLILoginFailException) {
			result.setMessage("登录目标服务器失败，请检查监测配置的用户/密码等是否正确.");
		} else if (e instanceof CLIConnectException) {
			result.setMessage("无法连接目标服务器，可能目标服务器没开启或者网络断开.");
		} else if (e instanceof CLIResultParseException) {
			result.setMessage("解析采集结果失败：" + e.getMessage());
		} else {
			String msg = e.getMessage();
			if (CLIExecutor.isErrorConnectFailed(msg)) {
				result.setMessage("无法连接目标服务器，可能目标服务器没开启或者网络断开或者未启用RPC服务.");
			} else {
				result.setMessage("CLI监测失败:" + msg);
			}
		}
		return result;
	}
}