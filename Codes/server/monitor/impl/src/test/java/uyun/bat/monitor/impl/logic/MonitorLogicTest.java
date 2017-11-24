package uyun.bat.monitor.impl.logic;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorCountVO;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.entity.Options;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.facade.FacadeManager;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

/**
 * 监测器触发所调用的特殊方法测试
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MonitorLogicTest {
	static {
		Startup.getInstance().startup();
	}
	private static final String TENANT_ID = "e0a67e986a594a61b3d1e523a0a39c77";
	private static final String USER_ID = "94baaadca64344d2a748dff88fe7159e";

	private static String metricMonitorId;
	private static String eventMonitorId;
	private static String hostMonitroId;
	private static String appMonitorId;

	@Test
	public void test1CreateMonitor() {
		List<String> list = new ArrayList<String>();
		list.add(USER_ID);

		Monitor monitor = new Monitor();
		monitor.setId(UUIDTypeHandler.createUUID());
		monitor.setCreatorId(USER_ID);
		monitor.setMessage("testmetrictestmetric");
		monitor.setName("testmetric");
		monitor.setQuery("avg:system.cpu.system{host} by {resourceId}");
		monitor.setMonitorType(MonitorType.METRIC);
		monitor.setTenantId(TENANT_ID);
		monitor.setModified(new Date());
		monitor.setCreateTime(new Date());
		Options op = new Options();
		monitor.setOptions(op);
		op.addThreshold(Options.ALERT, "last_10h <= 3");
		op.addThreshold(Options.WARNING, null);
		monitor.setNotifyUserIdList(list);
		monitor.setModified(new Date());
		monitor.setEnable(true);
		monitor = FacadeManager.getInstance().getMonitorFacade().createMonitor(monitor);
		metricMonitorId = monitor.getId();
		assertTrue(metricMonitorId.length() > 0);
	}

	@Test
	public void test2EventMonitor() {
		List<String> list = new ArrayList<String>();
		list.add(USER_ID);

		Monitor monitor = new Monitor();
		monitor.setId(UUIDTypeHandler.createUUID());
		monitor.setCreatorId(USER_ID);
		monitor.setMessage("testevent");
		monitor.setName("testevent");
		monitor
				.setQuery("events('status:alert,warning tags:{host:jianglf-centos6.7-64;role:1} \"error\"').by('resourceId').rollup('count').last('5m') >= 2");
		monitor.setMonitorType(MonitorType.EVENT);
		monitor.setTenantId(TENANT_ID);
		monitor.setModified(new Date());
		monitor.setCreateTime(new Date());
		Options op = new Options();
		String[] arr = { "1", "5m" };
		op.setEventRecover(arr);
		monitor.setOptions(op);
		monitor.setNotifyUserIdList(list);
		monitor.setMonitorState(MonitorState.ERROR);
		monitor.setModified(new Date());
		monitor.setEnable(true);
		monitor = FacadeManager.getInstance().getMonitorFacade().createMonitor(monitor);
		eventMonitorId = monitor.getId();
		assertTrue(eventMonitorId != null);
	}

	@Test
	public void test21HostMonitor() {
		List<String> list = new ArrayList<String>();
		list.add(TENANT_ID);

		Monitor monitor = new Monitor();
		monitor.setId(UUIDTypeHandler.createUUID());
		monitor.setCreatorId(TENANT_ID);
		monitor.setMessage("testhost");
		monitor.setName("testhost");
		monitor.setQuery("('host:fengzi_pc').by('resourceId').last('1m')");
		monitor.setMonitorType(MonitorType.HOST);
		monitor.setTenantId(TENANT_ID);
		monitor.setModified(new Date());
		monitor.setCreateTime(new Date());
		Options op = new Options();
		monitor.setOptions(op);
		op.addThreshold(Options.ALERT, null);
		monitor.setNotifyUserIdList(list);
		monitor.setModified(new Date());
		monitor.setEnable(true);
		monitor = FacadeManager.getInstance().getMonitorFacade().createMonitor(monitor);
		hostMonitroId = monitor.getId();
		assertTrue(hostMonitroId != null);
	}

	@Test
	public void test22AppMonitor() {
		List<String> list = new ArrayList<String>();
		list.add(TENANT_ID);

		Monitor monitor = new Monitor();
		monitor.setId(UUIDTypeHandler.createUUID());
		monitor.setCreatorId(TENANT_ID);
		monitor.setMessage("testapp");
		monitor.setName("testapp");
		monitor.setQuery("('host:fengzi_pc').state('redis.can_connect').by('resourceId').last('3m')");
		monitor.setMonitorType(MonitorType.APP);
		monitor.setTenantId("e0a67e986a594a61b3d1e523a0a39c77");
		monitor.setModified(new Date());
		monitor.setCreateTime(new Date());
		Options op = new Options();
		monitor.setOptions(op);
		op.addThreshold(Options.ALERT, null);
		monitor.setNotifyUserIdList(list);
		monitor.setModified(new Date());
		monitor.setEnable(true);
		monitor = FacadeManager.getInstance().getMonitorFacade().createMonitor(monitor);
		appMonitorId = monitor.getId();
		assertTrue(appMonitorId != null);
	}

	@Test
	public void test23UpdateMonitor() {
		Monitor monitor = FacadeManager.getInstance().getMonitorFacade().getMonitorById(TENANT_ID, appMonitorId);
		monitor.setQuery("('host:fengzi_pc').state('redis.can_connect').by('resourceId').last('10m')");
		FacadeManager.getInstance().getMonitorFacade().updateMonitor(monitor);

		monitor = new Monitor();
		monitor.setId(appMonitorId);
		monitor.setTenantId(TENANT_ID);
		monitor.setEnable(true);
		FacadeManager.getInstance().getMonitorFacade().updateMonitor(monitor);

	}

	@Test
	public void test3GetMonitors() {
		List<Monitor> monitors = LogicManager.getInstance().getMonitorLogic().getMonitors(TENANT_ID, MonitorType.METRIC);
		assertTrue(monitors.size() > 0);
	}

	@Test
	public void test4UpdateMonitorStatus() {
		Monitor monitor = FacadeManager.getInstance().getMonitorFacade().getMonitorById(TENANT_ID, metricMonitorId);
		monitor.setMonitorState(MonitorState.ERROR);
		assertTrue(LogicManager.getInstance().getMonitorLogic().updateMonitorState(monitor));
	}

	@Test
	public void test5GetCheckEventMonitors() {
		String option = "eventRecover\":[\"1\",";
		List<Monitor> monitors = LogicManager.getInstance().getMonitorLogic()
				.getCheckEventMonitors(MonitorType.EVENT, MonitorState.ERROR, option);
		assertTrue(monitors != null && monitors.size() > 0);
	}

	@Test
	public void test6DeleteMonitors() {
		Monitor monitor = new Monitor();
		monitor.setTenantId(TENANT_ID);
		monitor.setId(metricMonitorId);
		FacadeManager.getInstance().getMonitorFacade().deleteMonitor(monitor);
		monitor.setTenantId(TENANT_ID);
		monitor.setId(eventMonitorId);
		FacadeManager.getInstance().getMonitorFacade().deleteMonitor(monitor);
		monitor.setId(hostMonitroId);
		LogicManager.getInstance().getMonitorLogic().deleteMonitor(monitor);
		monitor.setId(appMonitorId);
		LogicManager.getInstance().getMonitorLogic().deleteMonitor(monitor);
		assertTrue(true);

	}

	@Test
	public void test7GetMonitorCountByDate() {
		Date endTime = new Date();
		List<MonitorCountVO> list = LogicManager.getInstance().getMonitorLogic()
				.getMonitorCountByDate(new Date(endTime.getTime() - 1000 * 60 * 60 * 24 * 30l), endTime);
		assertTrue(list != null && list.size() >= 0);
	}

	@Test
	public void test8GetMonitorCount() {
		List<MonitorCountVO> list = LogicManager.getInstance().getMonitorLogic().getMonitorCount();
		assertTrue(list != null && list.size() >= 0);
	}

}
