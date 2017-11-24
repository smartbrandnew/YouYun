package com.broada.carrier.monitor.server.impl.pmdb.map;

/**
 * Action脚本的编译错误
 */
public class ScriptCompileException extends ScriptExecuteException {
	private static final long serialVersionUID = 1L;

	public ScriptCompileException(String file, int line, String error) {
		super(file, line, "脚本语法错误", error);
	}
}
