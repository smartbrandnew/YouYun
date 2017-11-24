package uyun.bat.monitor.core.mq;

import org.junit.Test;
import org.springframework.jms.core.JmsTemplate;

public class StateMQServiceTest {

	@Test
	public void test() {
		StateMQService stateMQService = new StateMQService();
		JmsTemplate jmsTemplate = new JmsTemplate();
		stateMQService.setJmsTemplate(jmsTemplate);
		System.out.println(stateMQService.getJmsTemplate());
		
	}

}
