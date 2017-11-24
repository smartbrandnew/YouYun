package uyun.bat.agent.impl.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import uyun.bat.agent.api.entity.YamlFile;

public interface YamlFileDao {
	
	int save(YamlFile yamlFile);

	YamlFile getYamlFileByNameAndAgentId(Map<String, Object> map);
	
	List<YamlFile> getYamlFileListByAgentId(String tenantId,String agentId,String source);

	boolean delete(String id);

	int updateEnabled(String tenantId, String agentId, String fileName, String source, boolean enabled, Date modified);
	
	int deleteYaml(String tenantId, String agentId, String fileName, String source);

	List<String> getYamlNamesByEnabled(String agentId, boolean enabled);
}
