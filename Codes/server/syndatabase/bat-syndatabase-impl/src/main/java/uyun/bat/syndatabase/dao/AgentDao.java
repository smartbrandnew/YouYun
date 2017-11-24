package uyun.bat.syndatabase.dao;

import java.util.List;

import uyun.bat.syndatabase.entity.Tag;

public interface AgentDao {
	
	/**
	 * 获取所有的AgentTag
	 * @param key
	 * @return
	 */
	public List<Tag> getAllAgentTagByKey(String key);
	
	/**
	 * 批量更新AgentTag
	 * @param tags
	 * @return
	 */
	public int updateAgentTag(List<Tag> tags);
}
