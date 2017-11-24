package uyun.bat.monitor.core.mq;

import java.util.UUID;
import java.util.Date;
import org.junit.Test;

import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.mq.ResourceInfo;
import uyun.bat.monitor.core.entity.ResourceData;
import uyun.bat.monitor.core.logic.InstantiateService;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.common.ServiceManager;
import uyun.bat.monitor.impl.logic.LogicManager;
import uyun.bat.monitor.impl.logic.MonitorLogic;

public class ResourceMQServiceTest {

	ResourceMQService resourceMQService = new ResourceMQService();
	private static LogicManager logicManager = Startup.getInstance().getBean(LogicManager.class);
	MonitorLogic monitorLogic = logicManager.getMonitorLogic();
	@Test
	public void testOnMessage() {
		resourceMQService.onMessage(MessageService.message);
		System.out.println(resourceMQService.getCount());
	}
	
	@Test
	public void testDoConsume(){
		ServiceManager serviceManager = ServiceManager.getInstance();
		serviceManager.setResourceService(InstantiateService.resourceService);
		logicManager.setMonitorLogic(monitorLogic);
//		ResourceData data = new ResourceData();
		ResourceInfo data = new ResourceInfo();
		data.setTenantId(UUID.randomUUID().toString());
		data.setResourceId(UUID.randomUUID().toString());
		data.setOnlineStatus(OnlineStatus.ONLINE);
		Date lastCollectTime = new Date();
		lastCollectTime.setTime((long)10);
		data.setLastCollectTime(lastCollectTime);
		resourceMQService.doConsume(data);
	}

}
