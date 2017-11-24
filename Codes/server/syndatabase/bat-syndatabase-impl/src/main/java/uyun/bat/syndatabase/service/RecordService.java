package uyun.bat.syndatabase.service;

import java.util.List;

import uyun.bat.syndatabase.entity.Tag;

public interface RecordService {
	
	/**
	 * 获取所有的AgentTag
	 * @param key
	 * @return
	 */
	public List<Tag> getAllTagByKey(String key);
	
	/**
	 * 批量更新AgentTag
	 * @param tags
	 * @return
	 */
	public int updateTag(List<Tag> tags);
	
}
