package uyun.bat.syndatabase.dao;

import java.util.List;

import uyun.bat.syndatabase.entity.Tag;

public interface TagDao {
	
	/**
	 * 更新tag
	 * @param tagId
	 * @return
	 */
	public int updateTag(List<Tag> tags);
	
	/**
	 * 根据key获取所有相关的Tag
	 * @param key
	 * @return
	 */
	public List<Tag> getAllTagByKey(String key);
	
}
