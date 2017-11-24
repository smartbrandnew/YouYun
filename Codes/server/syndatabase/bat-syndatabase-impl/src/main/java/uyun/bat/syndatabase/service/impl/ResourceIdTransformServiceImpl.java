package uyun.bat.syndatabase.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.datastore.api.util.UUIDUtils;
import uyun.bat.syndatabase.dao.ResourceIdTransformDao;
import uyun.bat.syndatabase.entity.ResourceIdTransform;
import uyun.bat.syndatabase.service.ResourceIdTransformService;

public class ResourceIdTransformServiceImpl implements ResourceIdTransformService {
	
	@Autowired
	ResourceIdTransformDao resourceIdTransformDao;
	
	@Override
	public int insertResourceIdTransform(ResourceIdTransform transform) {
		transform.setUnitId(UUIDUtils.encodeMongodbId(transform.getUnitId()));
		if(resourceIdTransformDao.getTransformIdByIds(transform) != null)
			return 0;   // 已存在
		else
			return resourceIdTransformDao.insertResourceIdTransform(transform);
	}

	@Override
	public ResourceIdTransform getTransformIdByIds(String resId, String tenantId) {
		ResourceIdTransform transform = new ResourceIdTransform(resId, tenantId);
		transform = resourceIdTransformDao.getTransformIdByIds(transform);
		transform.setUnitId(UUIDUtils.decodeMongodbId(transform.getUnitId()));
		return transform;
	}

	@Override
	public List<ResourceIdTransform> getAllResourceIdTransform() {
		List<ResourceIdTransform> trans = resourceIdTransformDao.getAllResourceIdTransform();
		if(trans != null && trans.size() > 0)
			for(ResourceIdTransform tran:trans)
				tran.setUnitId(UUIDUtils.decodeMongodbId(tran.getUnitId()));
		return trans;
	}

}
