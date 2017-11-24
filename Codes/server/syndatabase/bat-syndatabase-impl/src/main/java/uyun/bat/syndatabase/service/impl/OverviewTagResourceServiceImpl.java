package uyun.bat.syndatabase.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.syndatabase.dao.OverviewDao;
import uyun.bat.syndatabase.entity.ResIdTransform;
import uyun.bat.syndatabase.service.OverviewTagResourceService;

public class OverviewTagResourceServiceImpl implements OverviewTagResourceService {
	
	@Autowired
	private OverviewDao overviewDao;
	
	@Override
	public List<String> getAllResId() {
		return overviewDao.getAllResId();
	}

	@Override
	public int updateResId(List<ResIdTransform> resId) {
		return overviewDao.updateResId(resId);
	}

}
