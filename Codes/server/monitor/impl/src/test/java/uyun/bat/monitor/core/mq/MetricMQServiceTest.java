package uyun.bat.monitor.core.mq;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Test;

import uyun.bat.monitor.core.logic.ConstantDef;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.logic.LogicManager;
import uyun.bat.monitor.impl.logic.MonitorLogic;

public class MetricMQServiceTest {

	MetricMQService metricMQService = new MetricMQService();
	static {
		Startup.getInstance().startup();
	}
	@Test
	public void testBean() {
		metricMQService.setConcurrentConsumers(10);
		metricMQService.setMaxLength(5);
		metricMQService.getConcurrentConsumers();
		metricMQService.getMaxLength();
		System.out.println(metricMQService.getCount());
	}
	
	
	
	@Test
	public void testDoConsume(){
		//MonitorLogic monitorLogic = new MonitorLogic();
//		logicManager.setMonitorLogic(monitorLogic);
		ArrayList <MQData> mqData = new ArrayList<MQData>();
		metricMQService.doConsume(ConstantDef.TENANT_ID, mqData);
	}
	
	@Test
	public void testOnMessage(){
		metricMQService.onMessage(MessageService.message);
	}
	
	

}
