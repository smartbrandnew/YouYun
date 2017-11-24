package uyun.bat.datastore.overview;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.overview.entity.OTag;
import uyun.bat.datastore.overview.logic.OverviewLogicManager;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OTagLogicTest {
	private static final String TENANT_ID = "e0a67e986a594a61b3d1e523a0a39c77";

	@Before
	public void setUp() throws Exception {
		Startup.getInstance().startup();
	}

	@Test
	public void test1CreateOTag() {
		List<OTag> tagList = new ArrayList<OTag>();
		OTag tag = new OTag(TENANT_ID, "k", "v");
		tagList.add(tag);
		tag = new OTag(TENANT_ID, "k", null);
		tagList.add(tag);
		tag = new OTag(TENANT_ID, null, "v");
		tagList.add(tag);
		tag = new OTag(TENANT_ID, null, null);
		tagList.add(tag);
		int flag = OverviewLogicManager.getInstance().getoTagLogic().createOTag(tagList);
		assertTrue(flag >= 0);
	}

	@Test
	public void test2GetByTenantId() {
		List<String> tags = OverviewLogicManager.getInstance().getoTagLogic().getTagKeyListByTenantId(TENANT_ID);
		assertTrue(tags.size() > 0);
		OTag tag = OverviewLogicManager.getInstance().getoTagLogic().queryTag(TENANT_ID, "k", "v");
		assertTrue(tags.size() > 0);
		int flag = OverviewLogicManager.getInstance().getoTagLogic().deleteOTag(TENANT_ID, tag.getId());
		assertTrue(flag > 0);
	}

}
