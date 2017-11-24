package uyun.bat.syndatabase.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.syndatabase.dao.ResTemplateDao;
import uyun.bat.syndatabase.entity.ResIdTransform;
import uyun.bat.syndatabase.service.ResTemplateService;

public class ResTemplateServiceImpl implements ResTemplateService{
	
	@Autowired
	private ResTemplateDao resTemplateDao;
	
	@Override
	public List<String> getAllResId() {
		return resTemplateDao.getAllResId();
	}

	@Override
	public int updateResId(List<ResIdTransform> resId) {
		return resTemplateDao.updateResId(resId);
	}

}
