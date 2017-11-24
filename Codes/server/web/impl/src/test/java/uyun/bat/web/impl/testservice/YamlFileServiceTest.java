package uyun.bat.web.impl.testservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import uyun.bat.agent.api.entity.AgentConfigDetail;
import uyun.bat.agent.api.entity.AgentConfigDetailHost;
import uyun.bat.agent.api.entity.YamlFile;
import uyun.bat.agent.api.service.YamlFileService;

public class YamlFileServiceTest implements YamlFileService {

	@Override
	public boolean upload(String tenantId, List<YamlFile> yamlFiles) {
		
		return true;
	}

	@Override
	public String getYamlContent(String tenantId, String agentId, String fileName, String source) {
		StringBuffer content = new StringBuffer();
		content.append(tenantId).append("  ").
				append(agentId).append("  ").
				append(fileName).append("  ").
				append(source).append("  ");
		return content.toString();
	}

	@Override
	public List<String> getAllYamlName(String tenantId, String source) {
		List<String>list = new ArrayList<>();
		list.add(source);
		list.add(tenantId);
		return list;
	}

	@Override
	public void updateEnabled(String tenantId, String agentId, String fileName, String source, boolean Enabled) {
	}

	@Override
	public void deleteYaml(String tenantId, String agentId, String fileName, String source) {
	}

	@Override
	public Map<String, Collection<String>> getAllPluginApp(String tenantId, String source, String id) {
		return null;
	}

	@Override
	public AgentConfigDetail pluginConfigDetail(String tenantId, String id, String pluginName, String source, int current, int pageSize, String checkStatus, String filter) {
		return null;
	}

	@Override
	public boolean updateHostConfig(String tenantId, String id, String pluginName, boolean checkNow, String source, List<AgentConfigDetailHost> newHosts, List<AgentConfigDetailHost> removeHosts, List<AgentConfigDetailHost> updateHosts) {
		return false;
	}

	@Override
	public boolean updateMethodConfig(String tenantId, String id, String pluginName, String source, List<Map<String, Object>> newMethods, List<String> removeNameList, Map<String, Object> updateMethod) {
		return false;
	}
	@Override
	public List<String> getDisabledYamlNames(String agentId) {
		return null;
	}

	@Override
	public List<String> getEnabledYamlNames(String agentId) {
		return null;
	}

	@Override
	public boolean checkPluginConfig(String tenantId, String id, String pluginName, String source, List<String> ipList) {
		return false;
	}
}
