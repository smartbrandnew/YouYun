package uyun.bat.dashboard.impl.service;

import com.alibaba.dubbo.config.annotation.Service;

import uyun.bat.dashboard.api.entity.TenantResTemplate;
import uyun.bat.dashboard.api.service.TenantResTemplateService;
import uyun.bat.dashboard.impl.facade.FacadeManager;

@Service(protocol = "dubbo")
public class TenantResTemplateServiceImpl implements TenantResTemplateService{
	
	@Override
	public TenantResTemplate getTemplate(String appName, String tenantId, String resourceId) {
		return FacadeManager.getInstance().getTenantResTemplateFacade().getTemplate(appName, tenantId, resourceId);
	}

	@Override
	public TenantResTemplate getGlobalTemplate(String appName, String tenantId) {
		return FacadeManager.getInstance().getTenantResTemplateFacade().getGlobalTemplate(appName, tenantId);
	}

	@Override
	public void createTemplate(TenantResTemplate temp) {
		if(temp.getResourceId()==null){
			if(FacadeManager.getInstance().getTenantResTemplateFacade().getGlobalTemplate(temp.getAppName(), temp.getTenantId())==null)
				FacadeManager.getInstance().getTenantResTemplateFacade().createTemplate(temp);
			else
				throw new IllegalArgumentException("Same Template");
		}else{
			if(FacadeManager.getInstance().getTenantResTemplateFacade().getTemplate(temp.getAppName(), temp.getTenantId(), temp.getResourceId())==null)
				FacadeManager.getInstance().getTenantResTemplateFacade().createTemplate(temp);
			else
				throw new IllegalArgumentException("Same Template");
		}
	}

	@Override
	public void update(TenantResTemplate temp) {
		FacadeManager.getInstance().getTenantResTemplateFacade().update(temp);
	}

	@Override
	public void delete(TenantResTemplate template) {
		FacadeManager.getInstance().getTenantResTemplateFacade().delete(template);
	}
	
}
