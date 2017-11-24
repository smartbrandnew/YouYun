package uyun.bat.monitor.impl.service;

import static org.junit.Assert.*;
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
import uyun.bat.monitor.api.entity.PageMonitor;
import uyun.bat.monitor.impl.Startup;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MonitorServiceImplTest {

	private static final String TENANT_ID = "94baaadca64344d2a748dff88fe7159e";
	private static final String USER_ID = "94baaadca64344d2a748dff88fe7159e";
	static{
		Startup.getInstance().startup();
	}
	private static String eventMonitorId;
	
	MonitorServiceImpl monitorServiceImpl = new MonitorServiceImpl();
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
		eventMonitorId = monitor.getId();
		monitorServiceImpl.createMonitor(monitor);
	}
	
	@Test
	public void test2GetMonitorById(){
		monitorServiceImpl.getMonitorById(TENANT_ID, eventMonitorId);
	}

	@Test
	public void test3UpdateMonitor(){
		Monitor monitor = new Monitor();
		monitor.setTenantId(TENANT_ID);
		monitor.setId(eventMonitorId);
		monitor.setName("dfsdf");
		monitor.setModified(new Date());
		monitorServiceImpl.updateMonitor(monitor);
	}
	
	@Test
	public void test4DeleteMonitor(){
		Monitor monitor = new Monitor();
		monitor.setTenantId(TENANT_ID);
		monitor.setId(eventMonitorId);
		monitorServiceImpl.deleteMonitor(monitor);
	}
	
	@Test
	public void test5GetMonitorsByFilter(){
		List<MonitorState> list = new ArrayList<>();
		list.add(MonitorState.OK);
		list.add(MonitorState.ERROR);
		PageMonitor pm = monitorServiceImpl.getMonitorsByFilter(TENANT_ID, 1, 20, null, list, null);
		System.out.println(pm.toString());
	}
	
	
	@Test
	public void test6GetMonitorCountByDate(){
		Date endTime = new Date();
		List<MonitorCountVO> list = monitorServiceImpl.getMonitorCountByDate(new Date(endTime.getTime() - 1000 * 60 * 60 * 24 * 30l), endTime);
		assertTrue(list != null && list.size() >= 0);
	}
	
	@Test
	public void test7GetMonitorCount(){
		List<MonitorCountVO> list = monitorServiceImpl.getMonitorCount();
		assertTrue(list != null && list.size() >= 0);
	}
}
