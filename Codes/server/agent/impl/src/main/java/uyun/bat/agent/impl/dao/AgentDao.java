package uyun.bat.agent.impl.dao;

import uyun.bat.agent.api.entity.Agent;
import uyun.bat.agent.api.entity.AgentQuery;
import uyun.bat.agent.api.entity.AgentTag;
import uyun.bat.agent.impl.entity.AgentTagResult;

import java.util.List;
import java.util.Map;

public interface AgentDao {

	int save(Agent agent);

	int update(Agent agent);

	int delete(String id);

	Agent getAgentById(Map<String, Object> map);

	List<AgentTagResult> queryTags(String tenantId,String source);

	int insertAgentTagBatch(List<AgentTag> agentTags);

	boolean deleteAgentTagBatch(List<String> ids) ;

	int deleteAgentTagById(String id);

	int queryCountByTags(AgentQuery query);

	List<Agent> queryByTags(AgentQuery query);

}
