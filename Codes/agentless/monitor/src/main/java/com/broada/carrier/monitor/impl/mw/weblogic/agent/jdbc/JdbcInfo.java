package com.broada.carrier.monitor.impl.mw.weblogic.agent.jdbc;

/**
 * Jdbc 信息
 * 
 * @author zhuhong
 * 
 */
public class JdbcInfo implements java.io.Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * 是否监控
	 */
	private boolean isWatched;
	/*
	 * jdbc 名称
	 */
	private String jdbcName;
	/*
	 * JDBC的状态
	 */
	private String status;
	/*
	 * 活动连接平均计数
	 */
	private int avgCount;
	/*
	 * 当前活动连接计数
	 */
	private int currCount;
	/*
	 * 最大活动连接计数
	 */
	private int highCount;
	/*
	 * JDBC的状态阈值
	 */
	private String statusEqual = "Running";

	public JdbcInfo() {
	}

	public JdbcInfo(boolean isWatched, String jdbcName, String status, int avgCount, int currCount,
			int highCount, String statusEqual) {
		this.isWatched = isWatched;
		this.jdbcName = jdbcName;
		this.status = status;
		this.avgCount = avgCount;
		this.currCount = currCount;
		this.highCount = highCount;
		this.statusEqual = statusEqual;
	}

	public boolean isWatched() {
		return isWatched;
	}

	public void setWatched(boolean isWatched) {
		this.isWatched = isWatched;
	}

	public String getJdbcName() {
		return jdbcName;
	}

	public void setJdbcName(String jdbcName) {
		this.jdbcName = jdbcName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusEqual() {
		return statusEqual;
	}

	public void setStatusEqual(String statusEqual) {
		this.statusEqual = statusEqual;
	}

	public int getAvgCount() {
		return avgCount;
	}

	public void setAvgCount(int avgCount) {
		this.avgCount = avgCount;
	}

	public int getCurrCount() {
		return currCount;
	}

	public void setCurrCount(int currCount) {
		this.currCount = currCount;
	}

	public int getHighCount() {
		return highCount;
	}

	public void setHighCount(int highCount) {
		this.highCount = highCount;
	}
}
