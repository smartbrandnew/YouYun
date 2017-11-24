package uyun.bat.agent.impl.logic;

import org.springframework.beans.factory.annotation.Autowired;

public class LogicManager {
	private static LogicManager instance = new LogicManager();
	@Autowired
	private AgentLogic agentLogic;
	@Autowired
	private YamlFileLogic yamlFileLogic;
	public static LogicManager getInstance() {
		return instance;
	}

	public AgentLogic getAgentLogic() {
		return agentLogic;
	}

	public YamlFileLogic getYamlFileLogic() {
		return yamlFileLogic;
	}

	
}
