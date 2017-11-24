package uyun.bat.agent.api.entity;

public enum AgentSource {
	agent(0, "agent"), agentless(1, "agentless");
	private int id;
	private String name;

	private AgentSource(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	//默认返回agent
	public static AgentSource checkAgentSourceById(int id) {
		for (AgentSource source : AgentSource.values()) {
			if (source.getId() == id)
				return source;
		}
		return AgentSource.agent;
	}

	//默认返回agent
	public static AgentSource checkAgentSourceByName(String name) {
		for (AgentSource source : AgentSource.values()) {
			if (source.getName().equalsIgnoreCase(name))
				return source;
		}
		return AgentSource.agent;
	}
}
