package uyun.bat.syndatabase.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.syndatabase.dao.OverviewDao;
import uyun.bat.syndatabase.entity.Tag;
import uyun.bat.syndatabase.service.OverviewTagService;

public class OverviewTagServiceImpl implements OverviewTagService{
	
	@Autowired
	private OverviewDao overviewDao;
	
	@Override
	public List<Tag> getAllTagByKey(String key) {
		return overviewDao.getAllOverTagByKey(key);
	}

	@Override
	public int updateTag(List<Tag> tags) {
		return overviewDao.updateOverviewTag(tags);
	}

}
