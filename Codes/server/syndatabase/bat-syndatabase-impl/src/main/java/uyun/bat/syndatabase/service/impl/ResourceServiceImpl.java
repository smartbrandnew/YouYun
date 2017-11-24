package uyun.bat.syndatabase.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.syndatabase.dao.ResourceDao;
import uyun.bat.syndatabase.entity.ResIdTransform;
import uyun.bat.syndatabase.entity.Tag;
import uyun.bat.syndatabase.service.RecordService;
import uyun.bat.syndatabase.service.ResourceService;

public class ResourceServiceImpl implements ResourceService, RecordService{
	
	@Autowired
	private ResourceDao resourceDao;
	
	@Override
	public List<String> getAllResId() {
		return resourceDao.getAllResId();
	}

	@Override
	public int updateResId(List<ResIdTransform> resId) {
		return resourceDao.updateResId(resId) + resourceDao.updateResIdForApp(resId)
			   + resourceDao.updateResIdForDetail(resId) + resourceDao.updateResIdForMonitor(resId)
			   + resourceDao.updateResIdForTag(resId);
	}

	@Override
	public List<Tag> getAllTagByKey(String key) {
		return resourceDao.getAllResTagByKey(key);
	}

	@Override
	public int updateTag(List<Tag> tags) {
		return resourceDao.updateResTag(tags);
	}

	@Override
	public List<Resource> queryAllRes() {
		return resourceDao.queryAllRes();
	}

}
