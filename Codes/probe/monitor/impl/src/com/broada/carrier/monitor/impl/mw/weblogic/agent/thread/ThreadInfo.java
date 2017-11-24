package com.broada.carrier.monitor.impl.mw.weblogic.agent.thread;
/**
 * weblogic thread 监测
 * @author zhuhong
 *
 */
public class ThreadInfo implements java.io.Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5083711054791817034L;
	
	/*
	 * 是否监控
	 */
	private boolean isWatched=true;
	/*
	 * 关键字
	 */
	private String instKey;
	/*
	 * 总线程数
	 */
	private Double totalThread;
	/*
	 * 空线程数
	 */
	private Double idleThread;
	/*
	 * 吞吐量
	 */
	private Double throughPut;
	/*
	 * 运行状况
	 */
	private String health;
	
	public boolean isWatched() {
		return isWatched;
	}

	public void setWatched(boolean isWatched) {
		this.isWatched = isWatched;
	}

	public String getInstKey() {
		return instKey;
	}

	public void setInstKey(String instKey) {
		this.instKey = instKey;
	}

	public Double getTotalThread() {
		return totalThread;
	}

	public void setTotalThread(Double totalThread) {
		this.totalThread = totalThread;
	}

	public Double getIdleThread() {
		return idleThread;
	}

	public void setIdleThread(Double idleThread) {
		this.idleThread = idleThread;
	}

	public Double getThroughPut() {
		return throughPut;
	}

	public void setThroughPut(Double throughPut) {
		this.throughPut = throughPut;
	}

	public String getHealth() {
		return health;
	}

	public void setHealth(String health) {
		this.health = health;
	}

}
