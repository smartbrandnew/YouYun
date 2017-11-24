package com.broada.carrier.monitor.client.impl.impexp.util;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.client.impl.impexp.entity.Log;

public class Logger {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Logger.class);
	private static List<Log> logs = new LinkedList<Log>();

	public static void log(Log log) {
		switch (log.getLevel()) {
		case ERROR:
			logger.error(log.getMessage(), log.getError());
			break;
		case WARN:
			logger.warn(log.getMessage(), log.getError());
			break;
		case INFO:
			logger.info(log.getMessage());
			break;
		default:
			throw new IllegalArgumentException(log.getLevel().toString());
		}
		logs.add(log);
	}
	
	public static void clear() {
		logs.clear();
	}

	public static List<Log> getLogs() {
		return logs;
	}
}
