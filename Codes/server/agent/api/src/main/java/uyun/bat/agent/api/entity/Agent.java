package uyun.bat.agent.api.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

public class Agent {
	private String id;
	private String hostname;
	private String ip;
	private List<String> tags = new ArrayList<>();
	private String tenantId;
	private List<String> apps;
	private AgentSource source;
	// 转换enum为string
	private String agent_source;

	private String onlineStatus;

	private Date modified;

	private List<AgentTag> agentTags = new ArrayList<>();

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

	@JsonIgnore
	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	@JsonIgnore
	public List<AgentTag> getAgentTags() {
		if (agentTags != null && agentTags.size() <= 0 && tags != null && tags.size() > 0) {
			for (String str : tags) {
				if (str != null && str.trim().length() > 0) {
					this.agentTags.add(changeToAgentTag(str));
				}
			}
		}
		return agentTags;
	}

	private AgentTag changeToAgentTag(String str) {
		int index = str.indexOf(":");
		if (index == -1) {
			return new AgentTag(id, tenantId, str, "");
		} else {
			return new AgentTag(id, tenantId, str.substring(0, index), str.substring(index + 1));
		}
	}

	@Override
	public String toString() {
		return "Agent [id=" + id + ", hostname=" + hostname + ", ip=" + ip + ", tags=" + tags + ", tenantId=" + tenantId
				+ ", apps=" + apps + ", source=" + source + ", onlineStatus=" + onlineStatus;
	}

	public static void main(String[] args) {
		Agent agent = new Agent(UUID.randomUUID().toString(), "ES_Server53.245", "10.1.53.245", new ArrayList<String>(
				Arrays.asList(new String[] { "host:myPC" })), new ArrayList<String>(Arrays.asList(new String[] { "cassandra",
				"network" })), AgentSource.agent, new Date());

		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(agent);
			System.out.println(json);
			Agent a = mapper.readValue(json, Agent.class);
			System.out.println(a.getSource());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
