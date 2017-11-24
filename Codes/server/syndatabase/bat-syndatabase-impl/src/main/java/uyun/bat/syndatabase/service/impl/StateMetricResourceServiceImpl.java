package uyun.bat.syndatabase.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.syndatabase.dao.StateMetricResourceDao;
import uyun.bat.syndatabase.entity.ResIdTransform;
import uyun.bat.syndatabase.service.StateMetricResourceService;

public class StateMetricResourceServiceImpl implements StateMetricResourceService{
	
	@Autowired
	private StateMetricResourceDao stateMetricResourceDao;
	
	@Override
	public List<String> getAllResId() {
		return stateMetricResourceDao.getAllResId() ;
	}

	@Override
	public int updateResId(List<ResIdTransform> resId) {
		return stateMetricResourceDao.updateResId(resId);
	}

}
