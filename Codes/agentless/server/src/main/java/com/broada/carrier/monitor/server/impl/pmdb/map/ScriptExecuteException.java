package com.broada.carrier.monitor.server.impl.pmdb.map;

import com.broada.carrier.monitor.common.error.BaseException;

/**
 * Action的执行错误，表示在执行一个具体的Action时存在的错误
 */
public class ScriptExecuteException extends BaseException {
	private static final long serialVersionUID = 1L;
	private String file;
	private String error;
	private int line;

	public ScriptExecuteException(String file, int line, String error) {
		this(file, line, "运行错误", error);		
	}
	
	public ScriptExecuteException(String file, int line, String error, Throwable cause) {
		this(file, line, "运行错误", error, cause);		
	}	
	
	protected ScriptExecuteException(String file, int line, String errorType, String errorMsg) {
		this(file, line, errorType, errorMsg, null);
	}
	
	protected ScriptExecuteException(String file, int line, String errorType, String errorMsg, Throwable cause) {
		super(String.format("Script执行失败[%s%s %s：%s]", file, line > 0 ? " 行：" + line : "", errorType, errorMsg), cause);
		this.file = file;
		this.error = errorMsg;
		this.line = line;
	}

	/**
	 * action文件
	 * @return
	 */
	public String getFile() {
		return file;
	}

	/**
	 * action错误消息
	 * @return
	 */
	public String getError() {
		return error;
	}

	/**
	 * 错误的行，如果为0一般为非action脚本中产生错误
	 * @return
	 */
	public int getLine() {
		return line;
	}
}
