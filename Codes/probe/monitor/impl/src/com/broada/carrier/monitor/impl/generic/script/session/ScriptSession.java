package com.broada.carrier.monitor.impl.generic.script.session;

import java.util.Properties;

import com.broada.carrier.monitor.impl.generic.script.ScriptException;

public interface ScriptSession {

	/**
	 * 检查与Agent连通与否
	 * @return
	 * @throws ScriptException
	 */
	boolean connectAgentSuccessed() throws ScriptException;
	
	/**
	 * 打开一个Script会话，调用该方法后会与目标服务器建立一个连接
	 * @param options 为登录所需要的各种参数，参数请参考ScriptConstants类的"OPTIONS_"开头的常量
	 */
	void open(Properties options);
	
	/**
	 * 关闭并销毁会话
	 */
	void close();
	
}