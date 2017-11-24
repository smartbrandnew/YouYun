package uyun.bat.dashboard.impl.facade;

import uyun.bat.dashboard.api.entity.TenantResTemplate;
import uyun.bat.dashboard.impl.logic.LogicManager;

public class TenantResTemplateFacade {
	
	public TenantResTemplate getTemplate(String appName, String tenantId, String resourceId) {
		return LogicManager.getInstance().getTenantResTemplateLogic().getTemplate(appName, tenantId, resourceId);
	}

	
	public TenantResTemplate getGlobalTemplate(String appName, String tenantId) {
		return LogicManager.getInstance().getTenantResTemplateLogic().getGlobalTemplate(appName, tenantId);
	}

	
	public void createTemplate(TenantResTemplate temp) {
		LogicManager.getInstance().getTenantResTemplateLogic().createTemplate(temp);
	}

	
	public void update(TenantResTemplate temp) {
		LogicManager.getInstance().getTenantResTemplateLogic().update(temp);
	}

	
	public void delete(TenantResTemplate template) {
		LogicManager.getInstance().getTenantResTemplateLogic().delete(template);
	}
	
}
