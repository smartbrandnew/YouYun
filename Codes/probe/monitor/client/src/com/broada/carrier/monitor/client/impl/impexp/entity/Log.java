package com.broada.carrier.monitor.client.impl.impexp.entity;

public class Log {
	private LogLevel level;
	private String message;
	private Throwable error;

	public Log(LogLevel level, String message) {
		this(level, message, null);
	}

	public Log(LogLevel level, String message, Throwable error) {
		this.level = level;
		this.message = message;
		this.error = error;
	}

	public LogLevel getLevel() {
		return level;
	}

	public String getMessage() {
		return message;
	}

	public Throwable getError() {
		return error;
	}

	@Override
	public String toString() {
		return String.format("%s[level: %s message: %s error: %s]", getClass().getSimpleName(), level, message, error);
	}
}
