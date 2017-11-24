package uyun.bat.monitor.impl.common;

import org.junit.Test;

import uyun.bat.monitor.core.logic.InstantiateService;
import uyun.bird.notify.api.NotifyService;
import uyun.bird.notify.api.imsg.IMessageService;
import uyun.bird.tenant.api.UserService;

public class ServiceManagerTest {

	@Test
	public void test() {
		ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.setEventService(InstantiateService.eventService);
		serviceManager.setMetricMetaDataService(InstantiateService.metricMetaDataService);
		serviceManager.setMetricService(InstantiateService.metricService);
		serviceManager.setResourceService(InstantiateService.resourceService);
		serviceManager.setStateService(InstantiateService.stateService);	
		UserService userService = serviceManager.getUserService();
		NotifyService notifyService = serviceManager.getNotifyService();
		IMessageService iMessageService = serviceManager.getiMessageService();
		System.out.println(userService+"  "+iMessageService+"  "+notifyService);
		
	}

}
