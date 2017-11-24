package uyun.bat.datastore.overview;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.overview.entity.TagResourceData;
import uyun.bat.datastore.api.overview.service.OverviewService;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OverviewServiceTest {
	private static final String TENANT_ID = "9D95D46CAC304F3A8149766C2A52F94F";
	OverviewService overviewService = null;

	@Before
	public void setUp() throws Exception {
		overviewService = Startup.getInstance().getBean(OverviewService.class);
	}

	@Test
	public void testGetOverviewTagList() {
		List<String> data = overviewService.getOverviewTagKeyList(TENANT_ID);
		assertTrue(data != null);
	}

	@Test
	public void testGetOverviewData() {
		List<TagResourceData> data = overviewService.getOverviewData(TENANT_ID);
		assertTrue(data != null);
	}

	@Test
	public void testGetTagResourceDataList() {
		List<TagResourceData> data = overviewService.getTagResourceDataList(TENANT_ID, null);
		assertTrue(data != null);
	}

	@Test
	public void testGetTagResourceData() {
		TagResourceData data = overviewService.getTagResourceData(TENANT_ID, null, null);
		assertTrue(data != null);
	}

}
