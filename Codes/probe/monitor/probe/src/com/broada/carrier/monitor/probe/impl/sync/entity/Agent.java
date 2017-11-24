package com.broada.carrier.monitor.probe.impl.sync.entity;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Agent {
	private String id;
	private String hostname;
	private String ip;
	private List<String> tags;
	private String tenantId;
	private List<String> apps;
	private AgentSource source;
	// 转换enum为string
	private String agent_source;

	private Date modified;

	public Agent() {
		super();
	}

	public Agent(String id, String hostname, String ip, List<String> tags, List<String> apps, AgentSource source,
			Date modified) {
		super();
		this.id = id;
		this.hostname = hostname;
		this.ip = ip;
		this.tags = tags;
		this.apps = apps;
		this.source = source;
		this.agent_source = source.getName();
		this.modified = modified;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	@JsonIgnore
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public List<String> getApps() {
		return apps;
	}

	public void setApps(List<String> apps) {
		this.apps = apps;
	}

	public AgentSource getSource() {
		if (source == null) {
			if (agent_source != null)
				source = AgentSource.checkAgentSourceByName(agent_source);
			else
				source = AgentSource.agent;
		}
		return source;
	}

	public void setSource(AgentSource source) {
		this.source = source;
	}

	@JsonIgnore
	public String getAgent_source() {
		if (agent_source == null) {
			if (source != null)
				agent_source = source.getName();
			else
				agent_source = AgentSource.agent.getName();
		}
		return agent_source;
	}

	public void setAgent_source(String agent_source) {
		this.agent_source = agent_source;
		this.source = AgentSource.checkAgentSourceByName(agent_source);
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	@Override
	public String toString() {
		return "Agent [id=" + id + ", hostname=" + hostname + ", ip=" + ip + ", tags=" + tags + ", tenantId="
				+ tenantId + ", apps=" + apps + ", source=" + source + ", agent_source=" + agent_source + ", modified="
				+ modified + "]";
	}
}
