package uyun.bat.gateway.agent.util;

import org.junit.Before;
import org.junit.Test;

import uyun.bat.common.proxy.tenant.TenantConstants;
import uyun.bat.gateway.agent.servicetest.ResourceServiceTest;
import uyun.bat.gateway.api.service.ServiceManager;

public class HostDetailTest {
	@Before
	public void setUp() throws Exception {
		ServiceManager.getInstance().setResourceService(ResourceServiceTest.create());
	}

	@Test
	public void testGetResourceDetailById() {
		String tenantId = TenantConstants.TENANT_ID;
		String resourceId = TenantConstants.TENANT_ID;
		HostDetail.getResourceDetailById(tenantId, resourceId);
	}

}
