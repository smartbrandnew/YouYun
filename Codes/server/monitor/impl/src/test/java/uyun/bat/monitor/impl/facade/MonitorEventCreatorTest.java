package uyun.bat.monitor.impl.facade;

import org.easymock.EasyMock;
import org.junit.Test;

import com.google.common.util.concurrent.ServiceManager;

import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.service.ResourceService;
import uyun.bat.datastore.api.service.StateService;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.service.EventService;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.entity.ResourceData;


public class MonitorEventCreatorTest {

	
	@Test
	public void testOnMonitorCreate() {
		EventService mockEventService = EasyMock.createMock(EventService.class);
		
		mockEventService.create(new Event());
		EasyMock.expectLastCall().andReturn(new Event()).times(1);
		EasyMock.replay(mockEventService);
		
		Monitor monitor=new Monitor();
		monitor.setTenantId("123456789101112131415162223");
		monitor.setName("测试实例");
		monitor.setMonitorType(MonitorType.HOST);
		MonitorEventCreator.onMonitorCreate(monitor);
	}

	@Test
	public void testOnTriggerUpdate() {
		Monitor old = new Monitor();
		old.setName("测试实例");
		old.setMonitorType(MonitorType.APP);
		old.setTenantId("123456789101112131415162223");
		
		Monitor newData = new Monitor();
		newData.setMonitorType(MonitorType.APP);
		
		EventService mockEventService = EasyMock.createMock(EventService.class);
		
		mockEventService.create(new Event());
		EasyMock.expectLastCall().andReturn(new Event()).times(1);
		EasyMock.replay(mockEventService);
		MonitorEventCreator.onTriggerUpdate(old, newData);
	}

	@Test
	public void testOnMonitorSilence() {
		Monitor old = new Monitor();
		old.setName("测试实例");
		old.setMonitorType(MonitorType.APP);
		old.setTenantId("123456789101112131415162223");
		
		Monitor newData = new Monitor();
		newData.setMonitorType(MonitorType.APP);
		
		EventService mockEventService = EasyMock.createMock(EventService.class);
		
		mockEventService.create(new Event());
		EasyMock.expectLastCall().andReturn(new Event()).times(1);
		EasyMock.replay(mockEventService);
		MonitorEventCreator.onMonitorSilence(old, newData);
	}

	@Test
	public void testOnMonitorDelete() {
		Monitor old = new Monitor();
		old.setName("测试实例");
		old.setMonitorType(MonitorType.APP);
		old.setTenantId("123456789101112131415162223");
		
		EventService mockEventService = EasyMock.createMock(EventService.class);
		
		mockEventService.create(new Event());
		EasyMock.expectLastCall().andReturn(new Event()).times(1);
		EasyMock.replay(mockEventService);
		MonitorEventCreator.onMonitorDelete(old);
	}

	@Test
	public void testOnResourceOnline() {
		ResourceData resource=new ResourceData();
		resource.setTenantId("2");
		resource.setResourceId("1");
		Resource r = new Resource();
		ResourceService mockResourceService = EasyMock.createMock(ResourceService.class);
		mockResourceService.queryResById("1","2");
		EasyMock.expectLastCall().andReturn(null).times(1);
		EasyMock.expect(mockResourceService.updateAsync(r)).andReturn(true);
		EasyMock.replay(mockResourceService);
		uyun.bat.monitor.impl.common.ServiceManager.getInstance().setResourceService(mockResourceService);
		
		EventService mockEventService = EasyMock.createMock(EventService.class);
		
		mockEventService.create(new Event());
		EasyMock.expectLastCall().andReturn(new Event()).times(1);
		EasyMock.replay(mockEventService);
		StateService mockStateService = EasyMock.createMock(StateService.class);
		
		mockStateService.saveCheckpoint(new Checkpoint());
		EasyMock.expectLastCall().times(1);
		EasyMock.replay(mockStateService);
		MonitorEventCreator.onResourceOnline(resource);
	}



}
