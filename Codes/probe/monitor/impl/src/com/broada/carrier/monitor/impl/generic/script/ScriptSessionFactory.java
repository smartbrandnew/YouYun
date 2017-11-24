package com.broada.carrier.monitor.impl.generic.script;

import java.util.Properties;

import com.broada.carrier.monitor.impl.generic.script.session.ScriptSession;
import com.broada.carrier.monitor.impl.generic.script.session.agent.AgentScriptSession;
import com.broada.carrier.monitor.method.script.ScriptExecuteSide;

public class ScriptSessionFactory {

	/**
	 * 根据options获取执行方式，并根据该方式返回一个会话
	 * 目前只支持Agent端会话
	 * @param options
	 * @return
	 */
	public static ScriptSession createSession(Properties options) {
		ScriptSession session;
		ScriptExecuteSide exeMode = getExecutionModeByOptions(options);
		if(exeMode == null){
			throw new ScriptRuntimeException("没有指定执行方式");
		}
		if(exeMode == ScriptExecuteSide.AGENT){
			session = new AgentScriptSession();
		}else{
			throw new ScriptRuntimeException("不支持的执行方式："+ exeMode);
		}
		session.open(options);
		return session;
	}
	
	private static ScriptExecuteSide getExecutionModeByOptions(Properties options){
		return ScriptExecuteSide.checkById(options.getProperty(ScriptConstants.EXECUTION_MODE));
	}
	
}
