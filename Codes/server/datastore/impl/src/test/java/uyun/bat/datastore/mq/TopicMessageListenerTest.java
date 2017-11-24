package uyun.bat.datastore.mq;



import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.easymock.EasyMock;
import org.junit.Test;

import uyun.bat.datastore.Startup;

public class TopicMessageListenerTest {
	private static TopicMessageListener topicMessageListener=Startup.getInstance().getBean(TopicMessageListener.class);
	@Test
	public void testOnMessage() {
		Message message = EasyMock.createMock(ObjectMessage.class);
		topicMessageListener.onMessage(message);
	}

}
