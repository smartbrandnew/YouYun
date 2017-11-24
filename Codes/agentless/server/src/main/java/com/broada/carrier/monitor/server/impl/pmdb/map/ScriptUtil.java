package com.broada.carrier.monitor.server.impl.pmdb.map;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.server.impl.config.Config;

public class ScriptUtil {
	private static final Logger logger = LoggerFactory.getLogger(ScriptUtil.class);
	private static final String ERROR_MSG_END1 = "\n";
	private static final String ERROR_MSG_END2 = "@ line ";	
	private static final String ERROR_MSG_PREFIX = ".groovy: ";	
	private static ScriptUtil instance;
	private CompilerConfiguration compilerConfiguration;
	private GroovyClassLoader groovyClassLoader;

	/**
	 * 获取默认实例
	 * @return
	 */
	public static ScriptUtil getDefault() {
		if (instance == null) {
			synchronized (ScriptUtil.class) {
				if (instance == null)
					instance = new ScriptUtil();
			}
		}
		return instance;
	}
	
	public ScriptUtil() {
		compilerConfiguration = new CompilerConfiguration();
		compilerConfiguration.setScriptBaseClass(Script.class.getName());		
		compilerConfiguration.setTargetDirectory(Config.getTempDir() + "/script-classes");		
		groovyClassLoader = new GroovyClassLoader(getClass().getClassLoader(), compilerConfiguration);
	}
	
	@SuppressWarnings("unchecked")
	public Class<Script> compileScript(String name, String script) {		
		try {			
			if (logger.isDebugEnabled())
				logger.debug("开始编译：" + name);			
			return groovyClassLoader.parseClass(script, name);
		} catch (Throwable e) {
			logger.debug("脚本语法错误，无法编译执行： " + name, e);
			throw createCompileException(name, e.getMessage());			
		}
	}
	
	/**
	 * 解析异常脚本，目前已知以下格式：
	 * @param e
	 * @return
	 */
	private static ScriptCompileException createCompileException(String name, String error) {		
		int line = 0;
		
		try {
			int msgPos = error.indexOf(ERROR_MSG_PREFIX);
			if (msgPos > 0) {
				int linePos = msgPos + ERROR_MSG_PREFIX.length();
				int linePosEnd = error.indexOf(": ", linePos);
				if (linePosEnd > 0) {
					line = Integer.parseInt(error.substring(linePos, linePosEnd));
				}
								
				int msgEndPos;
				int msgEnd1Pos = error.indexOf(ERROR_MSG_END1, linePosEnd);
				int msgEnd2Pos = error.indexOf(ERROR_MSG_END2, linePosEnd);
				if (msgEnd1Pos < 0 && msgEnd2Pos < 0) 
					msgEndPos = error.length();
				else if (msgEnd1Pos < 0)
					msgEndPos = msgEnd2Pos;
				else if (msgEnd2Pos < 0)
					msgEndPos = msgEnd1Pos;
				else
					msgEndPos = Math.min(msgEnd1Pos, msgEnd2Pos);
				error = error.substring(linePosEnd + 2, msgEndPos);
			}
		} catch (Throwable e1) {
			logger.warn(String.format("解析脚本语法错误信息失败[%s]。错误：%s", error, e1));
			logger.debug("堆栈：", e1);
		}
		
		return new ScriptCompileException(name, line, error);			
	}
}
