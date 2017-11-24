package uyun.bat.syndatabase.dao;

import java.util.List;

import uyun.bat.syndatabase.entity.ResIdTransform;

public interface MetricResourceDao {
	
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
	
}
