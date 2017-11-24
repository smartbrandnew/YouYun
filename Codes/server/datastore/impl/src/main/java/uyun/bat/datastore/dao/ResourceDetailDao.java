package uyun.bat.datastore.dao;

import java.util.List;

import uyun.bat.datastore.api.entity.ResourceDetail;

public interface ResourceDetailDao {

	boolean insert(ResourceDetail resourceDetail);

	boolean update(ResourceDetail resourceDetail);

	boolean delete(String resourceId);

	ResourceDetail queryByResId(String resourceId);

	long deleteResDetailBatch(List<String> ids);

}
