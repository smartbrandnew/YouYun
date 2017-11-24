package uyun.bat.web.impl.service.rest.overview;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uyun.bat.event.api.entity.PageUnrecoveredEvent;
import uyun.bat.web.api.overview.entity.Statistic;
import uyun.bat.web.api.overview.entity.TagKey;
import uyun.bat.web.api.overview.entity.TagNode;
import uyun.bat.web.impl.Startup;
import uyun.bat.web.impl.common.service.ServiceManager;
import uyun.bat.web.impl.testservice.OverviewServiceTest;

public class OverviewRESTServiceTest {
	OverviewRESTService overviewRESTService = null;

	@Before
	public void setUp() throws Exception {
		Startup.getInstance().startup();
		overviewRESTService = Startup.getInstance().getBean(OverviewRESTService.class);
		ServiceManager.getInstance().setOverviewService(new OverviewServiceTest());
	}

	@Test
	public void testGetAllStatistics() {
		List<TagNode> nodes = overviewRESTService.getAllStatistics("94baaadca64344d2a748dff88fe7159e");
		assertTrue(nodes != null);
	}

	@Test
	public void testGetStatisticByTag() {
		Statistic statistic = overviewRESTService.getStatisticByTag("94baaadca64344d2a748dff88fe7159e", null);
		assertTrue(statistic != null);
	}

	@Test
	public void testGetTagKeyList() {
		List<TagKey> keys = overviewRESTService.getTagKeyList("94baaadca64344d2a748dff88fe7159e");
		assertTrue(keys != null);
	}

	@Test
	public void testGetStatisticsByTagKey() {
		List<Statistic> statistic = overviewRESTService.getStatisticsByTagKey("94baaadca64344d2a748dff88fe7159e", null);
		assertTrue(statistic != null);
	}

	@Test
	public void testGetUnrecoveredEvents() {
		PageUnrecoveredEvent events = overviewRESTService.getUnrecoveredEvents("e0a67e986a594a61b3d1e523a0a39c77", 1, 10,
				"tt", null, null);
		assertTrue(events != null);
	}

}
