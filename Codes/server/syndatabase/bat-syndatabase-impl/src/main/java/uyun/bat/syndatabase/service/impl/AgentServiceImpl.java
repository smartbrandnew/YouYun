package uyun.bat.syndatabase.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.syndatabase.dao.AgentDao;
import uyun.bat.syndatabase.entity.Tag;
import uyun.bat.syndatabase.service.AgentService;

public class AgentServiceImpl implements AgentService{
	
	@Autowired
	private AgentDao agentDao;
	
	@Override
	public List<Tag> getAllTagByKey(String key) {
		return agentDao.getAllAgentTagByKey(key);
	}

	@Override
	public int updateTag(List<Tag> tags) {
		return agentDao.updateAgentTag(tags);
	}

}
