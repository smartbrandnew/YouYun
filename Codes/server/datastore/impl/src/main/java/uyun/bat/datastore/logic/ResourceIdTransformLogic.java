package uyun.bat.datastore.logic;

import java.util.List;

import uyun.bat.datastore.entity.ResourceIdTransform;


public interface ResourceIdTransformLogic {
	/**
	 * 保存资源id与统一资源库id的映射关系
	 * @param transform
	 * @return
	 */
	int insertResourceIdTransform(ResourceIdTransform transform);
	
	/**
	 * 根据资源id和tenantId查询映射表记录
	 * @param resId
	 * @param tenantId
	 * @return
	 */
	ResourceIdTransform getTransformIdByIds(String resId, String tenantId);
	
	/**
	 * 查询映射表中的所有资源
	 * @return
	 */
	List<ResourceIdTransform> getAllResourceIdTransform();
	
	/**
	 * 删除映射关系中的一条记录
	 * @param resId
	 * @param tenantId
	 */
	void delete(String resId, String tenantId);
	
}
