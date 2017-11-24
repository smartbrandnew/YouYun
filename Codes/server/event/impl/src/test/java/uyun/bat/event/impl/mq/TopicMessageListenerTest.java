package uyun.bat.event.impl.mq;


import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import uyun.bat.event.impl.Startup;

public class TopicMessageListenerTest {
	private static TopicMessageListener topicMessageListener= Startup.getInstance().getBean(TopicMessageListener.class);
	@Test
	public void testOnMessage() {
		topicMessageListener.onMessage(new ActiveMQTextMessage());
	}

}
