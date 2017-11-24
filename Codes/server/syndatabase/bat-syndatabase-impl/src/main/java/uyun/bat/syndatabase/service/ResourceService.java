package uyun.bat.syndatabase.service;

import java.util.List;

import uyun.bat.datastore.api.entity.Resource;

public interface ResourceService extends ColumnService{
	
	public List<Resource> queryAllRes();
	
}
