package uyun.bat.monitor.core.mq;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Test;

import com.mysql.fabric.xmlrpc.base.Array;

import uyun.bat.monitor.core.logic.ConstantDef;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.logic.LogicManager;
import uyun.bat.monitor.impl.logic.MonitorLogic;

public class StateMetricMQServiceTest {

	StateMetricMQService stateMetricMQService = new StateMetricMQService();
	private static LogicManager logicManager = Startup.getInstance().getBean(LogicManager.class);
	MonitorLogic monitorLogic = logicManager.getMonitorLogic();
	@Test
	public void testOnMessage() {
		
		stateMetricMQService.onMessage(MessageService.message);
		stateMetricMQService.setConcurrentConsumers(10);
		stateMetricMQService.setMaxLength(5);
		System.out.println(stateMetricMQService.getCount());
	}
	
	@Test
	public void testDoConsume(){
		logicManager.setMonitorLogic(monitorLogic);
		List<MQData> mqData = new ArrayList<MQData>();
		stateMetricMQService.doConsume(ConstantDef.TENANT_ID, mqData);
	}

}
