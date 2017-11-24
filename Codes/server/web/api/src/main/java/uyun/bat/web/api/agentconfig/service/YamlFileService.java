package uyun.bat.web.api.agentconfig.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import uyun.bat.agent.api.entity.AgentConfigDetail;
import uyun.bat.agent.api.entity.YamlFile;
import uyun.bat.web.api.agentconfig.entity.ConfigCheck;
import uyun.bat.web.api.agentconfig.entity.ConfigHostUpdate;
import uyun.bat.web.api.agentconfig.entity.ConfigMethodUpdate;

public interface YamlFileService {

	String upload(String tenantId, List<YamlFile> yamlFiles);

	String getYamlContent(String tenantId, String agentId, String fileName, String source);

	List<String> getAllYamlName(String tenantId, String source);

	void delete(String tenantId, String agentId, String fileName, String source);

	void disable(String tenantId, String agentId, String fileName, String source);

	void enable(String tenantId, String agentId, String fileName, String source);

	Map<String, Collection<String>> AllPluginApp(String tenantId, String source, String id);

	AgentConfigDetail pluginConfigDetail(String tenantId, String id, String pluginName, String source, Integer current, Integer pageSize, String checkStatus, String filter);

	ConfigHostUpdate updateHostConfig(String tenantId, ConfigHostUpdate hostUpdate);

	ConfigMethodUpdate updateMethodConfig(String tenantId, ConfigMethodUpdate methodUpdate);

	List<String> checkPluginConfig(String tenantId, ConfigCheck configCheck);
}
