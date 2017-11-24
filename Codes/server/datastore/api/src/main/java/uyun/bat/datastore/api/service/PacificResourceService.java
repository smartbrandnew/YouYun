package uyun.bat.datastore.api.service;

import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceClassCode;

import java.util.List;
import java.util.Map;

public interface PacificResourceService {
	
	/**
	 * 保存资源节点，返回值为统一资源库资源Id
	 * @param resource
	 * @return
	 */
	String save(Resource resource);

	/**
	 * 查询统一资源库租户下资源
	 * @param tenantId
	 * @param isContainNetwork
     * @return
     */
	List<Resource> queryAllRes(String tenantId, boolean isContainNetwork);

	/**
	 * 根据租户ID和IP列表批量查询
	 * @param tenantId
	 * @param ipList
	 * @return
	 */
	List<Resource> queryResByIpList(String tenantId, List<String> ipList);

	/**
	 * 根据租户ID和resId查询store的内置和自定义标签
	 * @param tenantId
	 * @param resId
	 * @return
	 */
	Map<String, List<String>> queryStoreTags(String tenantId, String resId);

	/**
	 * 根据租户ID和resId设置store的自定义标签
	 * @param tenantId
	 * @param resId
	 * @param tags
	 * @return
	 */
	boolean setTags(String tenantId, String resId, List<String> tags);

}
