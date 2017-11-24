package uyun.bat.syndatabase.dao;

import java.util.List;

import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.syndatabase.entity.ResIdTransform;
import uyun.bat.syndatabase.entity.Tag;

public interface ResourceDao {
	
	/**
	 * 获取所有的ResTag
	 * @param key
	 * @return
	 */
	public List<Tag> getAllResTagByKey(String key);
	
	/**
	 * 批量更新AgentTag
	 * @param tags
	 * @return
	 */
	public int updateResTag(List<Tag> tags);
	
	/**
	 * 查询所有的ResId
	 * @return
	 */
	public List<String> getAllResId();
	
	/**
	 * 更新ResId
	 * @param oldResId
	 * @param newResId
	 * @return
	 */
	public int updateResId(List<ResIdTransform> resId);
	
	/**
	 * 更新ResId（res_app）
	 * @param oldResId
	 * @param newResId
	 * @return
	 */
	public int updateResIdForApp(List<ResIdTransform> resId);
	
	/**
	 * 更新ResId（res_tag）
	 * @param oldResId
	 * @param newResId
	 * @return
	 */
	public int updateResIdForTag(List<ResIdTransform> resId);
	
	/**
	 * 更新ResId（res_detail）
	 * @param oldResId
	 * @param newResId
	 * @return
	 */
	public int updateResIdForDetail(List<ResIdTransform> resId);
	
	/**
	 * 更新ResId（resource_monitor_record）
	 * @param oldResId
	 * @param newResId
	 * @return
	 */
	public int updateResIdForMonitor(List<ResIdTransform> resId);
	
	/**
	 * 查询所有资源
	 * @return
	 */
	public List<Resource> queryAllRes();
	
}
