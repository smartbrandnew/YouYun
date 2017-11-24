package uyun.bat.datastore.overview.logic;

import java.util.List;

import javax.annotation.Resource;

import uyun.bat.datastore.api.util.UUIDUtils;
import uyun.bat.datastore.overview.dao.OTagResourceDao;
import uyun.bat.datastore.overview.entity.OTagResource;

public class OTagResourceLogic {
	@Resource
	private OTagResourceDao oTagResourceDao;

	public int create(List<OTagResource> oTagResourceList) {
		if (oTagResourceList == null || oTagResourceList.isEmpty())
			return 0;
		for(OTagResource res:oTagResourceList){
			String id=UUIDUtils.encodeMongodbId(res.getResourceId());
			res.setResourceId(id);
		}
		if (oTagResourceList.size() > 1000) {
			int count = create(oTagResourceList.subList(0, 1000));
			return count + create(oTagResourceList.subList(1000, oTagResourceList.size()));
		} else {
			return oTagResourceDao.create(oTagResourceList);
		}
	}

	public int delete(OTagResource oTagResource) {
		if(oTagResource==null)
			return 0;
		String id=UUIDUtils.encodeMongodbId(oTagResource.getResourceId());
		oTagResource.setResourceId(id);
		return oTagResourceDao.delete(oTagResource);
	}

	public List<String> queryTenantList() {
		return oTagResourceDao.queryTenantList();
	}

	public List<String> queryTenantResourceIdList(String tenantId) {
		List<String> idList=oTagResourceDao.queryTenantResourceIdList(tenantId);
		for(int i=0;i<idList.size();i++){
			String id=UUIDUtils.decodeMongodbId(idList.get(i));
			idList.set(i, id);
		}
		return idList;
	}

	/**
	 * 清除沒有资源的标签
	 * 
	 * @param tenantId
	 */
	public void deleteNoResourceTagId(String tenantId) {
		List<String> deletedTagIdList = oTagResourceDao.queryNoResourceTagId(tenantId);

		for (String tagId : deletedTagIdList) {
			OverviewLogicManager.getInstance().getoTagLogic().deleteOTag(tenantId, tagId);
		}
	}

	/**
	 * 清除沒有标签的资源关系
	 * 
	 * @param tenantId
	 */
	public void deleteNoTagResource(String tenantId) {
		List<OTagResource> deletedList = oTagResourceDao.queryNoTagList(tenantId);

		for (OTagResource temp : deletedList) {
			oTagResourceDao.delete(temp);
		}
	}
}
