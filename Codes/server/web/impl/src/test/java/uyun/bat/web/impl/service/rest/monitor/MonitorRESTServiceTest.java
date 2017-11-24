package uyun.bat.web.impl.service.rest.monitor;

import org.junit.Test;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.Options;
import uyun.bat.web.api.monitor.entity.MineMonitor;
import uyun.bat.web.api.monitor.entity.MineNotifyRecord;
import uyun.bat.web.api.monitor.entity.MonitorParam;
import uyun.bat.web.api.monitor.entity.SingleMonitor;
import uyun.bat.web.impl.testservice.StartService;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class MonitorRESTServiceTest extends StartService{
	MonitorRESTService monitorREST = new MonitorRESTService();
	private static final String USER_ID = "94baaadca64344d2a748dff88fe7159e";
	private static final String T_ID = "94baaadca64344d2a748dff88fe7159e";
	static MonitorParam mParam = new MonitorParam();
	MonitorState monitorState = MonitorState.OK;
	static List<String> list = new ArrayList<String>();
	Monitor monitor = new Monitor();

	static {

		list.add(USER_ID);
		mParam.setId(UUIDTypeHandler.createUUID());
		mParam.setCreatorId(USER_ID);
		mParam.setName("testevent");
		mParam.setTenantId(T_ID);
		mParam.setMonitorType("metric");
		mParam.setNotifyUserIdList(list);
		mParam.setMessage("testeventtestevent");
		mParam.setQuery("events('status:alert,warning \"不想开门\"').by('resourceId').rollup('count').last('5m') >= 1");
		mParam.setMonitorStatus("ok");
		mParam.setEnable(true);
		Options options = new Options();
		options.addThreshold(Options.ALERT, "last_5m < 3");
		options.addThreshold(Options.WARNING, "last_10h > 0");
		mParam.setOptions(options);
	}

	@Test
	public void testGetMonitorsByFilter() {
		String currentPage = null;
		String pageSize = null;
		String filtertValue = null;
		String monitorState = "silent;ok";
		MineMonitor mine = monitorREST.getMonitorsByFilter(T_ID, currentPage, pageSize, filtertValue, monitorState);
		assertTrue(mine != null);
	}

	@Test
	public void testGetMonitorById() {
		SingleMonitor single = monitorREST.getMonitorById(T_ID, USER_ID);
		assertTrue(single != null);
	}

	@Test
	public void testCreateMonitor() {
		monitorREST.createMonitor(T_ID, USER_ID, mParam);
	}

	@Test
	public void testUpdateMonitor() {
		monitorREST.updateMonitor(T_ID, mParam);
	}

	@Test
	public void testDeleteMonitor() {
		monitorREST.deleteMonitor(T_ID, monitor);
	}

	@Test
	public void testRunStatus() {
		monitorREST.runStatus(T_ID, monitor);
	}

	@Test
	public void testGetCount() {
		List<Integer> count = monitorREST.getCount(T_ID);
		assertTrue(count != null);
	}

	@Test
	public void testGetUser() {
		monitorREST.getUser(T_ID, null, 1, 20, null);
	}

	@Test
	public void testGetNotifyRecordList() {
		String monitorId = null;
		int currentPage = 0;
		int pageSize = 0;
		String timeRange = null;

		MineNotifyRecord mnr = monitorREST.getNotifyRecordList(T_ID, monitorId, currentPage, pageSize, timeRange);
		assertTrue(mnr != null);
	}

	@Test
	public void testIsMonitorExist() {
		assertTrue(monitorREST.isMonitorExist(T_ID));
	}

	@Test
	public void testGetMonitorHostsByFilter() {
		String monitorId = null;
		String tagName = null;
		int currentPage = 0;
		int pageSize = 0;
		String timeRange = null;
		// monitorREST.getMonitorHostsByFilter(T_ID, monitorId, tagName, timeRange,
		// currentPage, pageSize);
	}

}
