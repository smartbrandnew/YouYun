package uyun.bat.monitor.core.logic;

import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Test;

import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.core.entity.AppMonitorParam;
import uyun.bat.monitor.core.entity.Symbol;
import uyun.bat.monitor.core.entity.TagEntry;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.common.ServiceManager;

public class AppMonitorTest {    

	ArrayList<TagEntry> tags = new ArrayList<TagEntry>();
	Monitor monitor = new Monitor(); 
	AppMonitorParam appMonitorParam = new AppMonitorParam();
	AppMonitor appMonitor = new AppMonitor(monitor, appMonitorParam);
	
	public AppMonitorTest() {
		Startup.getInstance().startup();
	}
	
	@Test
	public void testCheckIfMonitorStatusRollover(){
		ServiceManager.getInstance().setStateService(InstantiateService.stateService);
		TagEntry tagEntry = new TagEntry();
		tagEntry.setKey(ConstantDef.TEST_KEY);
		tagEntry.setValue(ConstantDef.TEST_VALUE);
		tags.add(tagEntry);
		appMonitorParam.setState(ConstantDef.TEST_STATE);
		appMonitorParam.setTags(tags);
		appMonitorParam.setPeriod("10h");
		MonitorState monitorState = appMonitor.checkIfMonitorStatusRollover();
		appMonitor.doAfterCheck();
		System.out.println(monitorState.toString());
		assertTrue(monitorState != null);
	}
	
	@Test
	public void testGenerateSymbol(){
		MonitorState monitorStatus = MonitorState.OK;
		appMonitorParam.setState(ConstantDef.TEST_STATE);
		Symbol symbol = new Symbol();
		symbol = appMonitor.generateSymbol(ConstantDef.TEST_RESID, monitorStatus);
		System.out.println(symbol.toString());
	}
	
	

}
