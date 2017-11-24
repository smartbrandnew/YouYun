package uyun.bat.datastore.overview.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import uyun.bat.datastore.api.overview.entity.OTag;
import uyun.bat.datastore.overview.entity.OResourceTag;

public interface OTagDao {
	int create(List<OTag> oTag);

	int delete(@Param("tenantId") String tenantId, @Param("id") String id);

	List<String> getTagKeyListByTenantId(@Param("tenantId") String tenantId);

	List<OResourceTag> getTenantResourceTag(@Param("tenantId") String tenantId, @Param("resourceId") String resourceId);
	
	OTag queryTag(@Param("tenantId") String tenantId, @Param("key") String key, @Param("value") String value);
}
