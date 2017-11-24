package uyun.bat.web.impl.testservice;

import uyun.bat.dashboard.api.entity.TenantResTemplate;
import uyun.bat.dashboard.api.service.TenantResTemplateService;

public class TenantResTemplateServiceTest implements TenantResTemplateService{

	@Override
	public TenantResTemplate getTemplate(String appName, String tenantId, String resourceId) {
		TenantResTemplate template = new TenantResTemplate();
		if(appName.equals("app")){
			return null;
		}
		template.setAppName("Oracle");
		template.setDashId("123");
		template.setResourceId("234");
		template.setTenantId("456");
		return template;
	}

	@Override
	public void createTemplate(TenantResTemplate template) {
		System.out.println("创建模板"+template.getAppName()+template.getDashboardId()+template.getResourceId()+template.getTenantId());
	}

	@Override
	public void update(TenantResTemplate template) {
	}

	@Override
	public void delete(TenantResTemplate template) {
		System.out.println("删除template:"+template.getAppName());
	}

	@Override
	public TenantResTemplate getGlobalTemplate(String appName, String tenantId) {
		TenantResTemplate template = new TenantResTemplate();
		if(appName.equals("app")){
			return null;
		}
		template.setAppName(appName);
		template.setDashId("123");
		template.setResourceId("123");
		template.setTenantId(tenantId);
		return template;
	}

}
