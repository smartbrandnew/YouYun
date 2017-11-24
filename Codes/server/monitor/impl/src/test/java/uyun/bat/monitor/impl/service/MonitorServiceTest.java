package uyun.bat.monitor.impl.service;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorCount;
import uyun.bat.monitor.api.entity.MonitorCountVO;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.entity.Options;
import uyun.bat.monitor.api.entity.PageMonitor;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.dao.mybatis.MonitorStateTinyintTypeHandler;
import uyun.bat.monitor.impl.facade.FacadeManager;
import uyun.bat.monitor.impl.logic.LogicManager;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MonitorServiceTest {
	private static final String TENANT_ID = "94baaadca64344d2a748dff88fe7159e";
	private static final String USER_ID = "94baaadca64344d2a748dff88fe7159e";

	private static String metricMonitorId;
	private static String eventMonitorId;

	public MonitorServiceTest() {
		Startup.getInstance().startup();
	}

	@Test
	public void test1CreateMonitor() {
		List<String> list = new ArrayList<String>();
		list.add(USER_ID);

		Monitor monitor = new Monitor();
		monitor.setId(UUIDTypeHandler.createUUID());
		monitor.setCreatorId(USER_ID);
		monitor.setMessage("testmetrictestmetric");
		monitor.setName("testmetric");
		monitor.setQuery("avg:system.cpu.system{host:10.1.11.9} by {resourceId}");
		monitor.setMonitorType(MonitorType.METRIC);
		monitor.setTenantId(TENANT_ID);
		monitor.setNotify(true);
		monitor.setNotifyUserIdList(list);
		monitor.setModified(new Date());
		monitor.setEnable(true);
		Options options = new Options();
		options.addThreshold(Options.ALERT, "last_5m < 3");
		options.addThreshold(Options.WARNING, "last_10h > 0");
		monitor.setOptions(options);
		monitor.setCreateTime(new Date());
		FacadeManager.getInstance().getMonitorFacade().createMonitor(monitor);
		metricMonitorId = monitor.getId();
		monitor = new Monitor();
		monitor.setId(UUIDTypeHandler.createUUID());
		monitor.setCreatorId(USER_ID);
		monitor.setName("testevent");
		monitor.setTenantId(TENANT_ID);
		monitor.setMonitorType(MonitorType.EVENT);
		monitor.setNotifyUserIdList(list);
		monitor.setMessage("testeventtestevent");
		monitor.setQuery("events('status:alert,warning \"不想开门\"').by('resourceId').rollup('count').last('5m') >= 1");
		monitor.setModified(new Date());
		monitor.setEnable(true);
		Options op = new Options();
		String[] eventRecover = { "1", "10m" };
		op.setEventRecover(eventRecover);
		monitor.setOptions(op);
		monitor.setCreateTime(new Date());
		FacadeManager.getInstance().getMonitorFacade().createMonitor(monitor);

		eventMonitorId = monitor.getId();
		assertTrue(eventMonitorId.length() > 0);
	}

	@Test
	public void test3GetMonitorById() {
		Monitor m = FacadeManager.getInstance().getMonitorFacade().getMonitorById(TENANT_ID, eventMonitorId);
		assertTrue(m != null);
	}

	@Test
	public void test4UpdateMonitor() {
		Monitor monitor = new Monitor();
		monitor.setTenantId(TENANT_ID);
		monitor.setId(eventMonitorId);
		monitor.setName("dfsdf");
		monitor.setModified(new Date());
		FacadeManager.getInstance().getMonitorFacade().updateMonitor(monitor);
		monitor = FacadeManager.getInstance().getMonitorFacade().getMonitorById(TENANT_ID, eventMonitorId);
		assertTrue("dfsdf".equals(monitor.getName()));
	}

	@Test
	public void test8DeleteMonitor() {
		Monitor monitor = new Monitor();
		monitor.setTenantId(TENANT_ID);
		monitor.setId(eventMonitorId);
		FacadeManager.getInstance().getMonitorFacade().deleteMonitor(monitor);
		monitor = FacadeManager.getInstance().getMonitorFacade().getMonitorById(TENANT_ID, eventMonitorId);

		assertTrue(monitor == null);
	}

	@Test
	public void test2GetMonitors() {
		List<Monitor> monitors = LogicManager.getInstance().getMonitorLogic().getMonitors(TENANT_ID, MonitorType.METRIC);
		assertTrue(monitors.size() > 0);
	}

	@Test
	public void test5UpdateMonitorStatus() {
		Monitor monitor = FacadeManager.getInstance().getMonitorFacade().getMonitorById(TENANT_ID, metricMonitorId);
		monitor.setMonitorState(MonitorState.WARNING);
		assertTrue(LogicManager.getInstance().getMonitorLogic().updateMonitorState(monitor));
	}

	@Test
	public void test6GetMonitorsByFilter() {
		List<MonitorState> list = new ArrayList<>();
		list.add(MonitorState.OK);
		list.add(MonitorState.ERROR);
		PageMonitor pm = FacadeManager.getInstance().getMonitorFacade()
				.getMonitorsByFilter(TENANT_ID, 1, 20, null, list, null);
		assertTrue(pm.getMonitors() != null && pm.getMonitors().size() >= 0);
	}

	@Test
	public void test7GetCount() {
		List<MonitorCount> count = FacadeManager.getInstance().getMonitorFacade().getCount(TENANT_ID);
		int silent = 0;
		int ok = 0;
		int warn = 0;
		int alert = 0;
		int all = 0;
		for (MonitorCount c : count) {
			int temp = c.getCount();
			if (c.isEnable() == false)
				silent += temp;
			else {
				if (c.getState() == 2)
					ok = temp;
				if (c.getState() == 5)
					warn = temp;
				if (c.getState() == 8)
					alert = temp;
			}
		}
		assertTrue(count.size() > 0);
	}

	@Test
	public void test8Delete() {
		Monitor monitor = new Monitor();
		monitor.setTenantId(TENANT_ID);
		monitor.setId(metricMonitorId);
		boolean delete1 = LogicManager.getInstance().getMonitorLogic().deleteMonitor(monitor);
		assertTrue(delete1);
	}

	@Test
	public void test9GetMonitorCountByDate() {
		Date endTime = new Date();
		List<MonitorCountVO> list = LogicManager.getInstance().getMonitorLogic()
				.getMonitorCountByDate(new Date(endTime.getTime() - 1000 * 60 * 60 * 24 * 30l), endTime);
		assertTrue(list != null && list.size() >= 0);
	}

	@Test
	public void test10GetMonitorCount() {
		List<MonitorCountVO> list = LogicManager.getInstance().getMonitorLogic().getMonitorCount();
		assertTrue(list != null && list.size() >= 0);
	}
}
