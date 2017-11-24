package com.broada.carrier.monitor.impl.storage.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 监测返回结果存储类
 * @author ly
 *
 */
public class MonitorActionResult implements Serializable {
	private static final long serialVersionUID = 1L;
	private int priority;
	private List<Result> results = new ArrayList<Result>();

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public boolean isContinue() {
		return priority > 0;
	}
	
	public Result create(String key) {
		Result result = new Result(key);
		this.results.add(result);
		return result;
	}

	public Result create(String key, String clazz) {
		Result result = new Result(key, clazz);
		this.results.add(result);
		return result;
	}

	public List<Result> getResults() {
		return this.results;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Result result : results) {
			sb.append(result).append("\n");
		}
		return sb.toString();
	}
}
