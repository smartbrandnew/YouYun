package com.broada.carrier.monitor.probe.impl.openapi.entity;

import java.util.List;

public class Device {
	private String desc;
	private boolean onlineState;
	private List<String> tags;
	private String agentDescr;

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public boolean isOnlineState() {
		return onlineState;
	}

	public void setOnlineState(boolean onlineState) {
		this.onlineState = onlineState;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getAgentDescr() {
		return agentDescr;
	}

	public void setAgentDescr(String agentDescr) {
		this.agentDescr = agentDescr;
	}

	public Device() {
		super();
	}

	public Device(String desc, boolean onlineState, List<String> tags, String agentDescr) {
		super();
		this.desc = desc;
		this.onlineState = onlineState;
		this.tags = tags;
		this.agentDescr = agentDescr;
	}

	public Device(boolean onlineState, List<String> tags) {
		super();
		this.onlineState = onlineState;
		this.tags = tags;
	}

}
