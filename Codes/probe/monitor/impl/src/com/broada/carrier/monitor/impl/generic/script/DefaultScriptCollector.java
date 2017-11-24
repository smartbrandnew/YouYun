package com.broada.carrier.monitor.impl.generic.script;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.generic.script.session.ScriptSession;

public class DefaultScriptCollector implements ScriptResultCollector {
	
	private static final Log logger = LogFactory.getLog(DefaultScriptCollector.class);
	
	private ScriptSession scriptSession;
	
	public void close() {
		if(scriptSession != null){
			scriptSession.close();
		}
	}
	
	/**
	 * 检查Session是否有效
	 */
	protected void checkSession(){
		if(scriptSession == null){
			throw new ScriptRuntimeException("Script会话为空，请先init()保证初始化成功");
		}
	}

	public boolean checkConnectWithAgent() throws ScriptException{
		checkSession();
		
		boolean connResult = false;
		//添加同步锁,防止多线程的相同监视服务操作同一scriptSession
		synchronized(scriptSession){
			connResult = scriptSession.connectAgentSuccessed();
			if(logger.isDebugEnabled()){
				logger.debug("与Agent端连通情况:" + (connResult == true?"已连通":"无法连通"));
			}
		}
		return connResult;
	}

	public void init(Properties options) {
		scriptSession = ScriptSessionFactory.createSession(options);
		scriptSession.open(options);
	}

}
