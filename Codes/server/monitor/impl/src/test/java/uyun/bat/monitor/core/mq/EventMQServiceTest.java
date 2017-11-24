package uyun.bat.monitor.core.mq;


import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.activemq.command.ActiveMQMessage;
import org.junit.Test;

import uyun.bat.monitor.core.logic.ConstantDef;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.logic.LogicManager;
import uyun.bat.monitor.impl.logic.MonitorLogic;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
public class EventMQServiceTest {

	EventMQService eventMQService = new EventMQService();
	private static LogicManager logicManager = Startup.getInstance().getBean(LogicManager.class);
	MonitorLogic monitorLogic = logicManager.getMonitorLogic();
	
	@Test
	public void testDoConsume(){
		ArrayList<MQData> dataList = new ArrayList<MQData>();
		logicManager.setMonitorLogic(monitorLogic);
		eventMQService.doConsume(ConstantDef.TENANT_ID, dataList);
		System.out.println(eventMQService);
		
	}
	
	@Test
	public void testOnMessage() {
		
		eventMQService.setConcurrentConsumers(10);
		eventMQService.setMaxLength(10);
		eventMQService.onMessage(MessageService.message);
		System.out.println(eventMQService);
	}

}
