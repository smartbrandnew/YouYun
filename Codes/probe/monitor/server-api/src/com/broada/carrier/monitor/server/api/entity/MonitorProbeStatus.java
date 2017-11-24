package com.broada.carrier.monitor.server.api.entity;

import java.util.Date;

public class MonitorProbeStatus {
	private int probeId;
	private OnlineState onlineState;
	private Date time;

	public MonitorProbeStatus() {
		onlineState = OnlineState.UNKNOWN;
	}

	public MonitorProbeStatus(int probeId) {
		this.probeId = probeId;
		this.onlineState = OnlineState.UNKNOWN;
	}
	
	public MonitorProbeStatus(int probeId, OnlineState onlineState, Date time) {
		this.probeId = probeId;
		this.onlineState = onlineState;
		this.time = time;
	}

	public OnlineState getOnlineState() {
		return onlineState;
	}

	public void setOnlineState(OnlineState onlineState) {
		this.onlineState = onlineState;
	}

	public int getProbeId() {
		return probeId;
	}

	public void setProbeId(int probeId) {
		this.probeId = probeId;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
}
