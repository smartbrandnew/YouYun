package uyun.bat.monitor.core.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import uyun.bat.event.api.entity.Event;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.entity.CheckContext;
import uyun.bat.monitor.core.entity.MonitorParam;
import uyun.bat.monitor.core.entity.Symbol;
import uyun.bat.monitor.core.entity.TagEntry;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.common.ServiceManager;
import uyun.bird.tenant.api.UserService;

public class CheckControllerTest {

	private static ServiceManager serviceManager = Startup.getInstance().getBean(ServiceManager.class);
	ArrayList<TagEntry> tags = new ArrayList<TagEntry>();
	TagEntry tagEntry = new TagEntry();
	CheckController checkController = CheckController.getInstance();
	/*static{
		Startup.getInstance().getBean(UserService.class);
	}*/
	
	@Test
	public void testCheck() {
		tagEntry.setKey(ConstantDef.TEST_KEY);
		tagEntry.setValue(ConstantDef.TEST_VALUE);
		tags.add(tagEntry);
		
		Checker checker = new Checker() {
			
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
		checkController.check(checker);
	}
	
	@Test
	public void testTrigger(){
		
		ServiceManager.getInstance().setStateService(InstantiateService.stateService);	
		ServiceManager.getInstance().setEventService(InstantiateService.eventService);
		ServiceManager.getInstance().setMetricMetaDataService(InstantiateService.metricMetaDataService);
		Monitor monitor = new Monitor();
		CheckContext context = new CheckContext();
		MonitorState monitorState = MonitorState.OK;
		MonitorType monitorType = MonitorType.METRIC;
		MonitorParam monitorParam = new MonitorParam() {
			
			@Override
			public Map<String, String> getParamMap() {
				Map<String, String> map = new HashMap<String,String>();
				map.put("${metric}","111");
				map.put("${duration_warn}", "指标监测器持续时间");
				map.put("${duration_info}", "指标监测器持续时间");
				map.put("${aggregator}", "汇聚方法");
				map.put("${comparison_warn}", "指标监测器运算符");
				map.put("${threshold_warn}", "指标监测器阈值");
				map.put("${threshold_warn_unit}", "指标监测器阈值单位");
				map.put("${comparison_info}", "指标监测器运算符");
				map.put("${threshold_info}", "指标监测器阈值");
				map.put("${threshold_info_unit}", "指标监测器阈值单位");
				return map;
			}
		};		
		Symbol symbol = new Symbol();
		Event event = new Event();
		monitor.setId(ConstantDef.TEST_ID);
		symbol.setResourceId(ConstantDef.TEST_ID);
		symbol.setMonitorId(ConstantDef.TEST_ID);
		symbol.setTenantId(ConstantDef.TENANT_ID);
		symbol.setQuery(ConstantDef.QUERY);
		symbol.setMonitorState(monitorState);
		symbol.setMonitorType(monitorType);
		event.setMonitorType(ConstantDef.TEST_MONITOR_TYPE);
		event.setServerity((short)1);
		event.setMsgContent(ConstantDef.MSG_CONTENT);
		context.setEvent(event);
		context.setHostName(ConstantDef.HOST_NAME);
		context.setValue(ConstantDef.TEST_VALUE);
		context.setMonitorParam(monitorParam);
		boolean b = checkController.trigger(monitor,context,symbol);
		System.out.println(b);
		
		//测试notify()方法
		/*List<String> users = new ArrayList<String>();
		users.add("111");
		users.add("222");*/
		
		checkController.notify(context, monitor.getNotifyUserIdList());
	}

}
