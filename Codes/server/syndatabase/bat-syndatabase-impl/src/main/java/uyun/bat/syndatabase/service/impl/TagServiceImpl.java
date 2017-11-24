package uyun.bat.syndatabase.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.syndatabase.dao.TagDao;
import uyun.bat.syndatabase.entity.Tag;
import uyun.bat.syndatabase.service.TagService;

public class TagServiceImpl implements TagService{
	
	@Autowired
	private TagDao tagDao;
	
	@Override
	public int updateTag(List<Tag> tags) {
		return tagDao.updateTag(tags);
	}

	@Override
	public List<Tag> getAllTagByKey(String key) {
		return tagDao.getAllTagByKey(key);
	}

}
