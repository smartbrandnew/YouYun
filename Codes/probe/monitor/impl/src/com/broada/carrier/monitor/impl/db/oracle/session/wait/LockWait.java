package com.broada.carrier.monitor.impl.db.oracle.session.wait;

public class LockWait {
	
	private String sid;
	private long seconds_in_wait;
	
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public long getSeconds_in_wait() {
		return seconds_in_wait;
	}
	public void setSeconds_in_wait(long seconds_in_wait) {
		this.seconds_in_wait = seconds_in_wait;
	}
	
}
