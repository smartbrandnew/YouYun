package com.broada.carrier.monitor.server.api.entity;

public class ExecuteParams {
	public static final int DEFAULT_TIMEOUT = 30000;
	private int timeout;
	
	public ExecuteParams() {
		this(DEFAULT_TIMEOUT);
	}

	public ExecuteParams(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * 单位ms
	 * @return
	 */
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
}
