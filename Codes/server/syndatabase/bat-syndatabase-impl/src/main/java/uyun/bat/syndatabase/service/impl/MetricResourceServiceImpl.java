package uyun.bat.syndatabase.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.syndatabase.dao.MetricResourceDao;
import uyun.bat.syndatabase.entity.ResIdTransform;
import uyun.bat.syndatabase.service.MetricResourceService;

public class MetricResourceServiceImpl implements MetricResourceService{
	
	@Autowired
	private MetricResourceDao metricResourceDao;
	
	@Override
	public List<String> getAllResId() {
		return metricResourceDao.getAllResId();
	}

	@Override
	public int updateResId(List<ResIdTransform> resId) {
		return metricResourceDao.updateResId(resId);
	}

}
