package uyun.bat.web.impl.service.rest.agent;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.jackson.map.ObjectMapper;

import uyun.bat.common.config.Config;
import uyun.bat.web.api.agent.entity.AgentDownloadInfo;

public abstract class AgentInfoManager {
	private static final String escaped = Pattern.quote("${monitor.server.url}");

	private static Map<String, AgentDownloadInfo> agentInfoMap = new LinkedHashMap<String, AgentDownloadInfo>();

	private static AgentInfoManager instance = new AgentInfoManager() {
	};

	public static AgentInfoManager getInstance() {
		return instance;
	}

	/**
	 * oneStep的安装命令
	 */
	private Map<String, String> agentCommandMap = new LinkedHashMap<String, String>();

	public void init() {
		String baseDir = Config.getInstance().get("work.dir", System.getProperty("user.dir"));
		String[] searchPaths = new String[] { "/conf/", "/../conf/", "/../../conf/", "/src/main/resources/conf/" };

		File agentFile = null;
		for (String path : searchPaths) {
			agentFile = new File(baseDir, path + "agent_files.json");
			if (agentFile.exists()) {
				break;
			}
		}
		if (agentFile == null || agentFile.isDirectory())
			throw new IllegalArgumentException("Initialize agent download information failed!The agent_files.json configuration file does not exist.");

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Map<String, Object>> agentConfigMap;
		try {
			agentConfigMap = mapper.readValue(agentFile, Map.class);
		} catch (Exception e) {
			throw new RuntimeException("Parsing the Json file fails");
		}

		for (Map.Entry<String, Map<String, Object>> configEntry : agentConfigMap.entrySet()) {
			if ("files".equals(configEntry.getKey())) {
				for (Map.Entry<String, Object> entry : configEntry.getValue().entrySet()) {
					List<Map<String, String>> agentFiles = (List<Map<String, String>>) entry.getValue();
					AgentDownloadInfo info = AgentDownloadInfo.parseInfo(agentFiles);
					if (info != null)
						agentInfoMap.put(entry.getKey(), info);
				}
			} else if ("commands".equals(configEntry.getKey())) {
				String productURL = Config.getInstance().get("monitor.server.url").toString();
				for (Map.Entry<String, Object> entry : configEntry.getValue().entrySet()) {
					agentCommandMap.put(entry.getKey(), entry.getValue().toString().replaceAll(escaped, productURL));
				}
			}
		}
	}

	public Map<String, AgentDownloadInfo> getAgentInfoMap() {
		return agentInfoMap;
	}

	public AgentDownloadInfo generateAgentDownloadInfo(String apiKey, String os) {
		AgentDownloadInfo info = agentInfoMap.get(os);
		if (info == null)
			info = new AgentDownloadInfo();

		String command = generateCommand(apiKey, os);
		info.setCommand(command);
		return info;
	}

	private String generateCommand(String apiKey, String os) {
		String command = agentCommandMap.get(os);
		if (command == null || command.indexOf("%s") == -1)
			return command;
		return String.format(command, apiKey);
	}

}
