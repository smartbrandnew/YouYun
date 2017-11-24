package uyun.bat.agent.impl.logic;

import org.apache.commons.collections.CollectionUtils;
import uyun.bat.agent.api.entity.Agent;
import uyun.bat.agent.api.entity.AgentQuery;
import uyun.bat.agent.api.entity.AgentTag;
import uyun.bat.agent.api.entity.PageAgent;
import uyun.bat.agent.impl.dao.AgentDao;
import uyun.bat.agent.impl.entity.AgentTagResult;

import javax.annotation.Resource;
import java.util.*;

public class AgentLogic {
	@Resource
	private AgentDao agentDao;

	//先查询一次判断是否要更新agent或者agentTag
	public boolean saveAgent(Agent agent) {
		Agent existAgent=getAgentById(agent.getId(),agent.getTenantId());
		if (null==existAgent){
			agentDao.save(agent);
			if (agent.getAgentTags().size()>0){
				agentDao.insertAgentTagBatch(agent.getAgentTags());
			}
		}else{
			if(!agent.toString().equals(existAgent.toString())){
				agentDao.update(agent);
			}
			if (!CollectionUtils.isEqualCollection(agent.getAgentTags(),existAgent.getAgentTags())){
				deleteAgentTagById(agent.getId());
				if (agent.getAgentTags().size()>0){
					agentDao.insertAgentTagBatch(agent.getAgentTags());
				}
			}
		}
		return true;
	}

	public boolean deleteAgentTagById(String id){
		agentDao.deleteAgentTagById(id);
		return true;
	}

	public boolean deleteAgent(String id) {
		agentDao.delete(id);
		return true;
	}

	public Agent getAgentById(String id, String tenantId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", id);
		map.put("tenantId", tenantId);
		return agentDao.getAgentById(map);
	}

	public List<String> queryTags(String tenantId,String source) {
		List<AgentTagResult> tagResults=agentDao.queryTags(tenantId,source);
		List<String> tags=new ArrayList<>();
		Set<String> set=new HashSet<>();
		for(AgentTagResult tagResult:tagResults){
			if (null!=tagResult){
				set.addAll(tagResult.getTags());
			}
		}
		tags.addAll(set);
		return tags;
	}

	public PageAgent queryByTags(String tenantId, String[] tags,String source, String searchValue,int pageNo, int pageSize) {
		pageNo=(pageNo-1)*pageSize;
		AgentQuery query=new AgentQuery(tenantId,pageNo,pageSize,searchValue,source);
		buildAgentTags(tags,query);
		int count=agentDao.queryCountByTags(query);
		List<Agent> agents=new ArrayList<>();
		if (count>0){
			agents=agentDao.queryByTags(query);
		}
		return new PageAgent(count,agents);
	}

	private void buildAgentTags(String[] tags,AgentQuery query){
		List<AgentTag> agentTags=new ArrayList<>();
		if (null==tags){
			return ;
		}
		boolean others=false;
		for(String tag:tags){
			String[] temp=tag.split(":");
			AgentTag agentTag=new AgentTag();
			if (temp[0].equals("others")){
				others=true;
			}
			if (temp.length==2){
				agentTag.setKey(temp[0]);
				agentTag.setValue(temp[1]);
			}else if (temp.length==1){
				agentTag.setKey(temp[0]);
			}
			agentTags.add(agentTag);
		}
		query.setOthers(others);
		query.setAgentTags(agentTags);
	}
}
