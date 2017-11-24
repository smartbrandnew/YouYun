package uyun.bat.monitor.core.util;


import org.junit.Test;
import uyun.bat.event.api.entity.EventServerityType;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.entity.Options;
import uyun.bat.monitor.core.logic.*;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.logic.LogicManager;
import uyun.bat.monitor.impl.logic.MonitorLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MonitorQueryUtilTest {
	
    private static LogicManager logicManager=Startup.getInstance().getBean(LogicManager.class);
	MonitorLogic monitorLogic = logicManager.getMonitorLogic();
	
	public Checker checker = new Checker() {
		
		@Override
		public Monitor getMonitor() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void doAfterCheck() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public MonitorState checkIfMonitorStatusRollover() {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	@Test
	public void testGetMetricMonitor() {
		logicManager.setMonitorLogic(monitorLogic);
		List<MetricMonitor> metricMonitors = MonitorQueryUtil.getMetricMonitor(UUID.randomUUID().toString());
		System.out.println(metricMonitors.toString());
	}
	
	@Test
	public void testGetCheckerById(){
		logicManager.setMonitorLogic(monitorLogic);
		String ss[] = {UUID.randomUUID().toString(),UUID.randomUUID().toString()};
		checker = MonitorQueryUtil.getCheckerById(ss);
	}
	
	@Test
	public void testGetAppMonitor(){
		logicManager.setMonitorLogic(monitorLogic);
		List<AppMonitor>appMonitors = new ArrayList<AppMonitor>();
		appMonitors = MonitorQueryUtil.getAppMonitor(UUID.randomUUID().toString());
		System.out.println(appMonitors.toString());
	}
	
	@Test
	public void testGetHostMonitor(){
		logicManager.setMonitorLogic(monitorLogic);
		List<HostMonitor>hostMonitors = new ArrayList<HostMonitor>();
		hostMonitors = MonitorQueryUtil.getHostMonitor(UUID.randomUUID().toString());
		System.out.println(hostMonitors.toString());
	}
	
	@Test
	public void testGetEventMonitor(){
		logicManager.setMonitorLogic(monitorLogic);
		List<EventMonitor>eventMonitor = new ArrayList<EventMonitor>();
		eventMonitor = MonitorQueryUtil.getEventMonitor(UUID.randomUUID().toString());
		System.out.println(eventMonitor.toString());
	}
	
	@Test
	public void testGetTriggeredEventMonitor(){
		logicManager.setMonitorLogic(monitorLogic);
		List<EventMonitor>tEventMonitors = new ArrayList<EventMonitor>();
		tEventMonitors = MonitorQueryUtil.getTriggeredEventMonitor();
		System.out.println(tEventMonitors.toString());
	}

	@Test
	public void testGetEventServerityType(){
		MonitorState monitorState = MonitorState.OK;
		EventServerityType eventServerityType = MonitorQueryUtil.getEventServerityType(monitorState);
		System.out.println(eventServerityType.toString());
	}
	
	@Test
	public void testGetMonitorStatus(){
		MonitorState monitorState = MonitorQueryUtil.getMonitorStatus((short)0);
		System.out.println(monitorState.toString());
	}
	
	@Test
	public void testGetRecoversByTime(){
		String[] eventRecover = {"0","2"};
		String stringByTime = MonitorQueryUtil.getRecoversByTime(eventRecover);
		String stringByKeyWords = MonitorQueryUtil.getRecoversByKeywords(eventRecover);
		System.out.println(stringByTime);
		System.out.println(stringByKeyWords);
	}

	@Test
	public void testGenerateQuery(){
		Monitor monitor=new Monitor();
		monitor.setMonitorType(MonitorType.METRIC);
		monitor.setQuery("avg:system.cpu.system{host:jianglf-centos6.7-64;role:1} by {resourceId}");
		Options op = new Options();
		monitor.setOptions(op);
		op.addThreshold(Options.ALERT,"last_5m > 20");
		op.addThreshold(Options.WARNING,"last_10m < 2 %");
		String query=MonitorQueryUtil.generateQuery(monitor);
		System.out.println(query);
	}
	
}
