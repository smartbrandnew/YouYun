package uyun.bat.syndatabase.dao;

import java.util.List;

import uyun.bat.syndatabase.entity.ResIdTransform;
import uyun.bat.syndatabase.entity.Tag;

public interface OverviewDao {
	
	/**
	 * 依据key查询所有总览标签
	 * @return
	 */
	public List<Tag> getAllOverTagByKey(String key);
	
	/**
	 * 更新标签
	 * @param tags
	 * @return
	 */
	public int updateOverviewTag(List<Tag> tags);
	
	/**
	 * 获取所有ResId
	 * @return
	 */
	public List<String> getAllResId();
	
	/**
	 * 更新资源Id
	 * @param map
	 * @return
	 */
	public int updateResId(List<ResIdTransform> resId);
	
}
