package com.broada.carrier.monitor.impl.db.oracle.wait;

public class WaitEvent {
	
	private String event;
	private int act_session;
	private double rate;
	
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public int getAct_session() {
		return act_session;
	}
	public void setAct_session(int act_session) {
		this.act_session = act_session;
	}
	public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
	
}
