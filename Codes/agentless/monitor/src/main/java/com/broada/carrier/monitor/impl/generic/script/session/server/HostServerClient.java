package com.broada.carrier.monitor.impl.generic.script.session.server;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.generic.script.Executer;
import com.broada.carrier.monitor.impl.generic.script.context.NetCLIContext;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.numen.agent.script.context.Context;
import com.broada.numen.agent.script.context.cli.CLIContext;
import com.broada.numen.agent.script.entity.Parameter;
import com.broada.numen.agent.script.entity.Result;
import com.broada.numen.agent.script.impl.ExecuteServiceImpl;
import com.broada.numen.agent.script.service.ExecuteException;

/**
 * 服务端执行脚本
 * 
 * @author Sting
 *
 */
public class HostServerClient {

	private static final Log logger = LogFactory.getLog(ExecuteServiceImpl.class);

	public HostServerClient() {
	}

	/**
	 * 执行脚本，并控制超时执行
	 * @param scriptFilePath 完整的脚本文件路径
	 * @param param 主要取超时时间
	 * @return
	 * @throws Throwable 
	 * @throws ExecuteException
	 */
	public static Result executeInTime(final String scriptFilePath, final Parameter param, final MonitorMethod method, final String ip) throws ExecuteException {
		Callable<Result> eval = new Callable<Result>() {
			public Result call() throws Exception {
				return execute(scriptFilePath, param, method, ip);
			}
		};

		FutureTask<Result> task = new FutureTask<Result>(eval);
		//启动执行任务
		task.run();
		try {
			return task.get(param.getTimeout(), TimeUnit.SECONDS);
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "脚本执行失败：" + scriptFilePath, e);
			if (!task.isDone())
				task.cancel(true);            	
			if (e instanceof InterruptedException)
				throw ErrorUtil.createRuntimeException("监测任务在执行过程中被中断", e);    		
			else if (e instanceof ExecutionException && e.getCause() != null && e.getCause() instanceof ExecuteException)
				throw (ExecuteException)e.getCause();
			else if (e instanceof TimeoutException) 				
				throw ErrorUtil.createRuntimeException("监测任务执行超时", e);    	
			throw ErrorUtil.createRuntimeException("监测任务执行失败", e);
		}
	}

	private static Result execute(String scriptFilePath, Parameter param, MonitorMethod method,String ip) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("To Execute Script....");
		}
		Context[] contexts = new Context[] { new CLIContext(), new NetCLIContext() };
		return new Executer().execute(scriptFilePath, param, contexts, method, ip);
	}

}
