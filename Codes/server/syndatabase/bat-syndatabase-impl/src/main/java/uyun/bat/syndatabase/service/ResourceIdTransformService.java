package uyun.bat.syndatabase.service;

import java.util.List;

import uyun.bat.syndatabase.entity.ResourceIdTransform;

public interface ResourceIdTransformService {
	/**
	 * 保存资源id与统一资源库id的映射关系
	 * @param transform
	 * @return
	 */
	public int insertResourceIdTransform(ResourceIdTransform transform);
	
	/**
	 * 根据资源id和tenantId查询映射表记录
	 * @param resId
	 * @param tenantId
	 * @return
	 */
	public ResourceIdTransform getTransformIdByIds(String resId, String tenantId);
	
	/**
	 * 查询映射表中的所有资源
	 * @return
	 */
	public List<ResourceIdTransform> getAllResourceIdTransform();
	
}