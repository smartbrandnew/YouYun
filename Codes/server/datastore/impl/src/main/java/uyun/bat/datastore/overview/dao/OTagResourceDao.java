package uyun.bat.datastore.overview.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import uyun.bat.datastore.overview.entity.OTagResource;

public interface OTagResourceDao {
	int create(List<OTagResource> oTagResourceList);

	int delete(OTagResource oTagResource);

	/**
	 * 暂时不分组，估计租户列表不会太多
	 * 
	 * @return
	 */
	List<String> queryTenantList();

	List<String> queryTenantResourceIdList(@Param("tenantId") String tenantId);

	List<String> queryNoResourceTagId(@Param("tenantId") String tenantId);

	List<OTagResource> queryNoTagList(@Param("tenantId") String tenantId);

}
