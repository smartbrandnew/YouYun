package uyun.bat.monitor.impl.mq;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

import uyun.bat.datastore.api.mq.MetricInfo;
import uyun.bat.monitor.impl.Startup;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

public abstract class MQTest {
	private JmsTemplate jmsTemplate;

	public MQTest() {
		super();
		jmsTemplate = new JmsTemplate(Startup.getInstance().getBean(ConnectionFactory.class));
		jmsTemplate.setDefaultDestination(Startup.getInstance().getBean(Destination.class));
	}

	@Test
	public void testMetricSaved() {
		MetricInfo metricInfo = new MetricInfo("metric.jianglf", UUIDTypeHandler.createUUID());
		jmsTemplate.convertAndSend(metricInfo);
	}

}
