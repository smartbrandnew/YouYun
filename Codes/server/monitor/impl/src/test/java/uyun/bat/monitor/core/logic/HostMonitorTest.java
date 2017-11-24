package uyun.bat.monitor.core.logic;

import java.util.ArrayList;

import org.junit.Test;

import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.monitor.api.common.util.PeriodUtil.Period;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.core.entity.HostMonitorParam;
import uyun.bat.monitor.core.entity.ResourceData;
import uyun.bat.monitor.core.entity.Symbol;
import uyun.bat.monitor.core.entity.TagEntry;
import uyun.bat.monitor.impl.common.ServiceManager;

public class HostMonitorTest {

	Monitor monitor = new Monitor();
	HostMonitorParam hostMonitorParam = new HostMonitorParam();
	ResourceData resourceData = new ResourceData();
	HostMonitor hostMonitor = new HostMonitor(monitor, hostMonitorParam,resourceData);
	//private static ServiceManager serviceManager = Startup.getInstance().getBean(ServiceManager.class);
	@Test
	public void testCheckIfMonitorStatusRollover() {
		ServiceManager.getInstance().setResourceService(InstantiateService.resourceService);
		
		resourceData.setResourceId("testResourceId");
		hostMonitorParam.setPeriod("testPeriod");
		Period period = new Period();
		period.setStart(123456);
		OnlineStatus onlineStatus = OnlineStatus.ONLINE;
		resourceData.setOnlineStatus(onlineStatus);
		MonitorState state = hostMonitor.checkIfMonitorStatusRollover();
		System.out.println(state);
		
	}
	
	@Test
	public void testDoAfterCheck(){
		hostMonitor.doAfterCheck();
	}
	
	@Test
	public void testGenerateSymbol(){
		Symbol symbol = new Symbol();
		MonitorState monitorState = MonitorState.OK;
		symbol = hostMonitor.generateSymbol(ConstantDef.TEST_RESID, monitorState);
		System.out.println(symbol);
	}
	
	@Test
	public void testMatch(){
		
		ServiceManager.getInstance().setEventService(InstantiateService.eventService);
		monitor.setTenantId(ConstantDef.TENANT_ID);
		ArrayList<TagEntry> tags = new ArrayList<TagEntry>();
		TagEntry tagEntry = new TagEntry();
		tagEntry.setKey(ConstantDef.TEST_KEY);
		tagEntry.setValue(ConstantDef.TEST_VALUE);
		tags.add(tagEntry);
		hostMonitorParam.setTags(tags);
		boolean b = hostMonitor.match(ConstantDef.TEST_RESID, null);
		System.out.println(b);
	}

}
