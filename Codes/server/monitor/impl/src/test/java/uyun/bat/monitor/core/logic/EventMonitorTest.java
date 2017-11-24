package uyun.bat.monitor.core.logic;

import java.util.ArrayList;
import org.junit.Test;

import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.event.api.entity.Event;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.entity.Options;
import uyun.bat.monitor.core.entity.Comparison;
import uyun.bat.monitor.core.entity.EventData;
import uyun.bat.monitor.core.entity.EventMonitorParam;
import uyun.bat.monitor.core.entity.Symbol;
import uyun.bat.monitor.core.entity.TagEntry;
import uyun.bat.monitor.impl.common.ServiceManager;

public class EventMonitorTest {

	Monitor monitor = new Monitor();
	EventMonitorParam eventMonitorParam = new EventMonitorParam();
	EventData eventData = new EventData();
	EventMonitor eventMonitor = new EventMonitor(monitor, eventMonitorParam,eventData);
	ArrayList<TagEntry> tags = new ArrayList<TagEntry>();
	EventData data = new EventData();
	
	@Test
	public void testEventMonitor() {
		
		//testMatch()
		eventMonitorParam.setKeyWords(ConstantDef.KEY_WORDS);
		Options options = new Options();
		monitor.setOptions(options);
		options.setEventRecover(new String[] { "1", "10m" });
		data.setContent(ConstantDef.TEST_CONTENT);
		data.setTitle(ConstantDef.TEST_TITLE);
		boolean b = eventMonitor.match(data);
		System.out.println(b);

		//testDoAfterCheck()
		
		data.setResId("123");
		ServiceManager.getInstance().setResourceService(InstantiateService.resourceService);
		eventMonitor.doAfterCheck();
	}
	

	@Test
	public void testCheckIfMonitorStatusRollover() {

		// checkIfMonitorStatusRollover
		Comparison comparison = Comparison.GE;

		TagEntry tagEntry = new TagEntry();
		tagEntry.setKey(ConstantDef.TEST_KEY);
		tagEntry.setValue(ConstantDef.TEST_VALUE);
		tags.add(tagEntry);

		monitor.setTenantId(ConstantDef.TEST_ID);
		String[] status = { "success", "info" };
		String[] sources = { "monitor", "open-api" };
		eventMonitorParam.setSources(sources);
		eventMonitorParam.setStatus(status);
		eventMonitorParam.setPeriod("1111m");
		eventMonitorParam.setKeyWords(ConstantDef.KEY_WORDS);
		eventMonitorParam.setTags(tags);
		eventMonitorParam.setComparison(comparison);
		eventMonitorParam.setThreshold(1);
		ServiceManager.getInstance().setEventService(InstantiateService.eventService);
		MonitorState monitorState = eventMonitor.checkIfMonitorStatusRollover();
		System.out.println(monitorState.toString());

		// generateEvent()
		Event event = new Event();
		MonitorType monitorType = MonitorType.METRIC;
		monitor.setMonitorType(monitorType);
		Resource resource = new Resource();
		resource.setId(ConstantDef.TEST_ID);
		event = eventMonitor.generateEvent(MonitorState.OK,resource);
		System.out.println(event);

		// generateSymbol()
		Symbol symbol = new Symbol();
		MonitorState monitorStatus = MonitorState.OK;
		symbol.setQuery("");
		symbol = eventMonitor.generateSymbol(ConstantDef.TEST_RESID, monitorStatus);
		System.out.println(symbol);

		// getResIdByTags
		String[] strings = { "resourceId:123" };
		String getKey = eventMonitor.getResIdByTags(strings);
		System.out.println(getKey);
	}

}
