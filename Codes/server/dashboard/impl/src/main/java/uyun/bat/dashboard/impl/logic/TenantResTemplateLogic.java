package uyun.bat.dashboard.impl.logic;

import javax.annotation.Resource;

import uyun.bat.dashboard.api.entity.TenantResTemplate;
import uyun.bat.dashboard.impl.dao.TenantResTemplateDao;

public class TenantResTemplateLogic {
	@Resource
	private TenantResTemplateDao tenantResTemplateDao;
	public TenantResTemplate getTemplate(String appName, String tenantId, String resourceId) {
		return tenantResTemplateDao.getTemplate(appName,tenantId,resourceId);
	}

	
	public TenantResTemplate getGlobalTemplate(String appName, String tenantId) {
		return tenantResTemplateDao.getGlobalTemplate(appName, tenantId);
	}

	
	public void createTemplate(TenantResTemplate temp) {
		tenantResTemplateDao.createTemplate(temp);
	}

	
	public void update(TenantResTemplate temp) {
		tenantResTemplateDao.update(temp);
	}

	
	public void delete(TenantResTemplate template) {
		tenantResTemplateDao.delete(template);
	}
	
}
