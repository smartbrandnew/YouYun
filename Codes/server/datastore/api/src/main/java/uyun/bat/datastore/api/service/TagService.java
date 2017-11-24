package uyun.bat.datastore.api.service;

import java.util.List;

/**
 * 全局标签服务
 */
public interface TagService {
	/**
	 * 获取一组标签标记的一个准确对象id，如果标签与对象不存在，则建立
	 * @param tags
	 * @return
	 */
	String checkObjectId(String[] tags);

	/**
	 * 获取一组标签精确匹配的一个对象id，如果标签不存在，则返回null
	 * @param tags
	 * @return
	 */
	String getObjectId(String[] tags);

	/**
	 * 获取一组标签模糊匹配的一组对象id，如果标签不存在，则返回0长度
	 * @param tags
	 * @return
	 */
	String[] queryObjectIds(String[] tags);

	/**
	 * 删除一组标签与这些标签匹配的所有对象
	 * @param tags
	 */
	void deleteTagsAndObjects(String[] tags);

	/**
	 * 查询指标一批标签的id，如果标签不存在，则建立
	 * @param tags
	 * @return
	 */
	List<String> checkTagIds(String[] tags);

	/**
	 * 查询指定一批标签的id
	 * @param tags
	 * @return
	 */
	List<String> getTagIds(String[] tags);

	/**
	 * 获取一个指定对象的标签集合
	 * @param objectId
	 * @return
	 */
	String[] getObjectTags(String objectId);
}
