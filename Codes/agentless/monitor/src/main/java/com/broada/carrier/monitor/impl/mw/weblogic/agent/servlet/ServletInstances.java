package com.broada.carrier.monitor.impl.mw.weblogic.agent.servlet;

/**
 * servlet 监测实体
 * 
 * @author zhuhong
 * 
 */
public class ServletInstances implements java.io.Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 8772857767452501277L;
	/*
	 * 是否监控
	 */
	private boolean isWatched;
	/*
	 * servlet名称
	 */
	private String servletName;
	/*
	 * 关键字
	 */
	private String instKey;
	/*
	 * 最长执行时间
	 */
	private double maxTime;

	/*
	 * 平均执行时间
	 */
	private double avgTime;

	/*
	 * 总调用次数
	 */
	private String invokeTimes;
	/*
	 * 最长执行时间阈值
	 */
	private double maxTimeLimit = 10000;
	/*
	 * 平均执行时间阈值
	 */
	private double avgTimeLimit = 10000;

	public ServletInstances(boolean isWatched, String servletName, String instKey, double maxTime, double avgTime,
			String invokeTimes, double maxTimeLimit, double avgTimeLimit) {
		this.isWatched = isWatched;
		this.servletName = servletName;
		this.instKey = instKey;
		this.maxTime = maxTime;
		this.avgTime = avgTime;
		this.invokeTimes = invokeTimes;
		this.maxTimeLimit = maxTimeLimit;
		this.avgTimeLimit = avgTimeLimit;
	}

	public ServletInstances() {
	}

	public boolean isWatched() {
		return isWatched;
	}

	public void setWatched(boolean isWatched) {
		this.isWatched = isWatched;
	}

	public String getServletName() {
		return servletName;
	}

	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	public String getInstKey() {
		return instKey;
	}

	public void setInstKey(String instKey) {
		this.instKey = instKey;
	}

	public double getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(double maxTime) {
		this.maxTime = maxTime;
	}

	public double getAvgTime() {
		return avgTime;
	}

	public void setAvgTime(double avgTime) {
		this.avgTime = avgTime;
	}

	public String getInvokeTimes() {
		return invokeTimes;
	}

	public void setInvokeTimes(String invokeTimes) {
		this.invokeTimes = invokeTimes;
	}

	public double getMaxTimeLimit() {
		return maxTimeLimit;
	}

	public void setMaxTimeLimit(double maxTimeLimit) {
		this.maxTimeLimit = maxTimeLimit;
	}

	public double getAvgTimeLimit() {
		return avgTimeLimit;
	}

	public void setAvgTimeLimit(double avgTimeLimit) {
		this.avgTimeLimit = avgTimeLimit;
	}

}
