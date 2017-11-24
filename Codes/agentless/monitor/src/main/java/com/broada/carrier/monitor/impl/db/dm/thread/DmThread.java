package com.broada.carrier.monitor.impl.db.dm.thread;

/**
 * DM线程监测
 * 
 * @author Zhouqa Create By 2016年4月15日 下午2:36:02
 */
public class DmThread {
	private Boolean isWacthed = Boolean.FALSE;
	private Long ID; // 
	private String threadName;
	private String startTime;

	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public Long getID() {
		return ID;
	}

	public void setID(Long iD) {
		ID = iD;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

}