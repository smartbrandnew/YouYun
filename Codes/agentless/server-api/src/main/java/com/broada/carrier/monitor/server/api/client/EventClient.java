package com.broada.carrier.monitor.server.api.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.component.utils.error.ErrorUtil;
import com.broada.mq.client.FailoverConnection;
import com.broada.mq.client.FailoverTopicSubscriber;

public class EventClient {
	private static final Logger logger = LoggerFactory.getLogger(EventClient.class);
	private FailoverConnection connection;
	private Map<String, FailoverTopicSubscriber> subscribers = new ConcurrentHashMap<String, FailoverTopicSubscriber>();
	
	public EventClient(FailoverConnection connection) {
		this.connection = connection;
	}
	
	public EventClient(String mqUrl, String mqUser, String mqPassword, String name) {
		connection = new FailoverConnection(mqUrl, name, mqUser, mqPassword);
	}
	
	public void addListener(String topic, String name, EventListener listener) {
		try {
			FailoverTopicSubscriber subscriber = new FailoverTopicSubscriber(connection, topic, name, false);
			subscriber.setMessageListener(new EventMessageListener(listener));
			subscribers.put(topic + "-" + name, subscriber);
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("事件订阅失败", e);
		}
	}
	
	public FailoverConnection getConnection() {
		return connection;
	}
	
	public void removeListener(String topic, String name) {
		subscribers.remove(topic + "-" + name);
	}
	
	private class EventMessageListener implements MessageListener {
		private EventListener target;
		
		public EventMessageListener(EventListener target) {
			this.target = target;
		}

		@Override
		public void onMessage(Message msg) {
			if (msg instanceof ObjectMessage) {				
				try {					
					Object event = ((ObjectMessage) msg).getObject();
					logger.debug("事件消息处理开始：[listener: {} event: {}]", target, event);
					target.receive(event);
				} catch (Throwable e) {
					ErrorUtil.warn(logger, String.format("事件消息处理失败[listener: %s message: %s]", target, msg), e);
				}				
			}
		}
	}
}	
