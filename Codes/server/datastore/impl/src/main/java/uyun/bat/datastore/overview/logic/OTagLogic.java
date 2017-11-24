package uyun.bat.datastore.overview.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Param;

import uyun.bat.datastore.api.overview.entity.OTag;
import uyun.bat.datastore.api.util.UUIDUtils;
import uyun.bat.datastore.overview.dao.OTagDao;
import uyun.bat.datastore.overview.entity.OResourceTag;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

public class OTagLogic {
	@Resource
	private OTagDao oTagDao;

	public int createOTag(List<OTag> oTagList) {
		if (oTagList == null || oTagList.isEmpty())
			return 0;
		for (OTag oTag : oTagList) {
			if (oTag.getId() == null)
				oTag.setId(UUIDTypeHandler.createUUID());
		}

		return create(oTagList);
	}

	/**
	 * 每批插入1000个
	 * 
	 * @param oTagList
	 * @return
	 */
	private int create(List<OTag> oTagList) {
		if (oTagList.size() > 1000) {
			int count = create(oTagList.subList(0, 1000));
			return count + create(oTagList.subList(1000, oTagList.size()));
		} else {
			return oTagDao.create(oTagList);
		}
	}

	public int deleteOTag(String tenantId, String id) {
		return oTagDao.delete(tenantId, id);
	}

	/**
	 * 总览堆图<br>
	 * 标签Key列表
	 * 
	 * @param tenantId
	 * @return
	 */
	public List<String> getTagKeyListByTenantId(String tenantId) {
		return oTagDao.getTagKeyListByTenantId(tenantId);
	}

	/**
	 * 获取 资源id 与 标签列表Map
	 * 
	 * @param tenantId
	 * @param resourceId
	 *            null则获取所有资源的
	 * @return
	 */
	public Map<String, List<OResourceTag>> getTenantResourceTag(String tenantId, String resourceId) {
		 resourceId=UUIDUtils.encodeMongodbId(resourceId);
		List<OResourceTag> tags = oTagDao.getTenantResourceTag(tenantId, resourceId);
		for(OResourceTag tag:tags){
			String resId=UUIDUtils.decodeMongodbId(tag.getResourceId());
			tag.setResourceId(resId);
		}
		Map<String, List<OResourceTag>> resourceTagMap = new HashMap<String, List<OResourceTag>>();
		if (tags.isEmpty())
			return resourceTagMap;

		for (OResourceTag resourceTag : tags) {
			List<OResourceTag> tagList = resourceTagMap.get(resourceTag.getResourceId());
			if (tagList == null) {
				tagList = new ArrayList<OResourceTag>();
				resourceTagMap.put(resourceTag.getResourceId(), tagList);
			}

			tagList.add(resourceTag);
		}
		return resourceTagMap;
	}

	public OTag queryTag(String tenantId, String key, String value) {
		if (key == null)
			key = "";
		if (value == null)
			value = "";
		return oTagDao.queryTag(tenantId, key, value);
	}
}
