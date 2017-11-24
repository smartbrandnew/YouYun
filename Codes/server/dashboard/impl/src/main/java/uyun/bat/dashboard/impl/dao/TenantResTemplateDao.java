package uyun.bat.dashboard.impl.dao;

import uyun.bat.dashboard.api.entity.TenantResTemplate;

public interface TenantResTemplateDao {

	TenantResTemplate getTemplate(String appName, String tenantId,String resourceId);
	
	TenantResTemplate getGlobalTemplate(String appName,String tenantId);
	
	void createTemplate(TenantResTemplate template);
	
	void update(TenantResTemplate template);
	
	void delete(TenantResTemplate template);
}
