package uyun.bat.dashboard.impl.service;

import static org.junit.Assert.*;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;
import uyun.bat.dashboard.api.entity.TenantResTemplate;
import uyun.bat.dashboard.api.service.TenantResTemplateService;
import uyun.bat.dashboard.impl.Startup;
import uyun.bat.dashboard.impl.facade.FacadeManager;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TenantResTemplateServiceImplTest extends TestCase{
	private static final Logger logger = LoggerFactory.getLogger(TenantResTemplateServiceImplTest.class);
	TenantResTemplate template=new TenantResTemplate();
	private static final String appName = "system";
	private static final String tenantId = "94baaadca64344d2a748dff88fe7159e";
	private static final String dashboardId = "94baaadca64344d2a748dff88fe7159e";
	//private static final String dashboardId = "12345678910111213141516171819000";
	private static final String resourceId = "94baaadca64344d2a748dff88fe7159e";
	
	@Override
	protected void setUp() throws Exception {
		Startup.getInstance().startup();
	}
	
	@Test
	public void testGetTemplate() {
		template = FacadeManager.getInstance().getTenantResTemplateFacade().getTemplate("system", "E0A67E986A594A61B3D1E523A0A39C77", "837d6ce9631411c9166855f7b387c345");
		//System.out.println(template.toString());
	}

	@Test
	public void testGetGlobalTemplate() {
		template = FacadeManager.getInstance().getTenantResTemplateFacade().getGlobalTemplate(appName, tenantId);
	}

	@Test
	public void testCreateTemplate() {
		template.setAppName(appName);
		template.setDashId(dashboardId);
		template.setResourceId(resourceId);
		template.setTenantId(tenantId);
		FacadeManager.getInstance().getTenantResTemplateFacade().createTemplate(template);
	}

	@Test
	public void testUpdate() {
		template.setAppName("testUpdate");
		template.setDashId(dashboardId);
		template.setResourceId(resourceId);
		template.setTenantId(tenantId);
		FacadeManager.getInstance().getTenantResTemplateFacade().update(template);
	}

	@Test
	public void testDelete() {
		template.setAppName(appName);
		template.setDashId(dashboardId);
		template.setResourceId(resourceId);
		template.setTenantId(tenantId);
		FacadeManager.getInstance().getTenantResTemplateFacade().delete(template);
	}

}
