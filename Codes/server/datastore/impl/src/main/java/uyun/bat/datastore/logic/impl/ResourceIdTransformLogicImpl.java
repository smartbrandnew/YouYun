package uyun.bat.datastore.logic.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.datastore.api.util.UUIDUtils;
import uyun.bat.datastore.dao.ResourceIdTransformDao;
import uyun.bat.datastore.entity.ResourceIdTransform;
import uyun.bat.datastore.logic.ResourceIdTransformLogic;

public class ResourceIdTransformLogicImpl implements ResourceIdTransformLogic{
	
	@Autowired
	ResourceIdTransformDao resourceIdTransformDao;
	
	@Override
	public int insertResourceIdTransform(ResourceIdTransform transform) {
		transform.setUnitId(UUIDUtils.encodeMongodbId(transform.getUnitId()));
//		if(resourceIdTransformDao.getTransformIdByIds(transform) != null)
//			return 0;   // 已存在
//		else
		return resourceIdTransformDao.saveResourceIdTransform(transform);
	}

	@Override
	public ResourceIdTransform getTransformIdByIds(String resId, String tenantId) {
		ResourceIdTransform transform = new ResourceIdTransform(resId, tenantId);
		ResourceIdTransform result = resourceIdTransformDao.getTransformIdByIds(transform);
		if (result == null) {
			return transform;
		}
		result.setUnitId(UUIDUtils.decodeMongodbId(result.getUnitId()));
		return result;
	}

	@Override
	public List<ResourceIdTransform> getAllResourceIdTransform() {
		List<ResourceIdTransform> trans = resourceIdTransformDao.getAllResourceIdTransform();
		if(trans != null && trans.size() > 0)
			for(ResourceIdTransform tran:trans)
				tran.setUnitId(UUIDUtils.decodeMongodbId(tran.getUnitId()));
		return trans;
	}

	@Override
	public void delete(String resId, String tenantId) {
		ResourceIdTransform transform = new ResourceIdTransform(resId, tenantId);
		resourceIdTransformDao.delete(transform);
	}

}
