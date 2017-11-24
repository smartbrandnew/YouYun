package com.broada.carrier.monitor.impl.mw.jboss.webapp;

public class WebInformation {

	private int sessionMaxAliveTime;
	private int activeSessions;
	private int sessionAverageAliveTime;
	private int maxActive;
	private int expiredSessions;
	private int rejectedSessions;
	private int sessionCounter;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSessionMaxAliveTime() {
		return sessionMaxAliveTime;
	}

	public void setSessionMaxAliveTime(int sessionMaxAliveTime) {
		this.sessionMaxAliveTime = sessionMaxAliveTime;
	}

	public int getActiveSessions() {
		return activeSessions;
	}

	public void setActiveSessions(int activeSessions) {
		this.activeSessions = activeSessions;
	}

	public int getSessionAverageAliveTime() {
		return sessionAverageAliveTime;
	}

	public void setSessionAverageAliveTime(int sessionAverageAliveTime) {
		this.sessionAverageAliveTime = sessionAverageAliveTime;
	}

	public int getExpiredSessions() {
		return expiredSessions;
	}

	public void setExpiredSessions(int expiredSessions) {
		this.expiredSessions = expiredSessions;
	}

	public int getRejectedSessions() {
		return rejectedSessions;
	}

	public void setRejectedSessions(int rejectedSessions) {
		this.rejectedSessions = rejectedSessions;
	}

	public int getSessionCounter() {
		return sessionCounter;
	}

	public void setSessionCounter(int sessionCounter) {
		this.sessionCounter = sessionCounter;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	@Override
	public String toString() {
		return "WebInformation [sessionMaxAliveTime=" + sessionMaxAliveTime + ", activeSessions=" + activeSessions
				+ ", sessionAverageAliveTime=" + sessionAverageAliveTime + ", maxActive=" + maxActive + ", expiredSessions="
				+ expiredSessions + ", rejectedSessions=" + rejectedSessions + ", sessionCounter=" + sessionCounter + ", name="
				+ name + "]";
	}

}
