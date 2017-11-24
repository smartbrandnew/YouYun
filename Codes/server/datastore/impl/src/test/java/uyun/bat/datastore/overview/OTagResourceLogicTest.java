package uyun.bat.datastore.overview;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import uyun.bat.datastore.Startup;
import uyun.bat.datastore.overview.entity.OTagResource;
import uyun.bat.datastore.overview.logic.OverviewLogicManager;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OTagResourceLogicTest {
	private static final String TENANT_ID = "e0a67e986a594a61b3d1e523a0a39e77";

	@Before
	public void setUp() throws Exception {
		Startup.getInstance().startup();
	}

	@Test
	public void test1CreateOTagResource() {
		List<OTagResource> oTagResourceList = new ArrayList<OTagResource>();
		OTagResource oTagResource = new OTagResource(TENANT_ID, TENANT_ID, TENANT_ID);
		oTagResourceList.add(oTagResource);
		int flag = OverviewLogicManager.getInstance().getoTagResourceLogic().create(oTagResourceList);
		assertTrue(flag > 0);
	}

	@Test
	public void test2DeleteOTagResource() {
		OTagResource oTagResource = new OTagResource(TENANT_ID, TENANT_ID, TENANT_ID);
		int flag = OverviewLogicManager.getInstance().getoTagResourceLogic().delete(oTagResource);
		assertTrue(flag > 0);
	}

	@Test
	public void test2GetTenantIdList() {
		List<String> tenantList = OverviewLogicManager.getInstance().getoTagResourceLogic().queryTenantList();
		assertTrue(tenantList != null);
	}

}
