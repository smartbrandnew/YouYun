package com.broada.carrier.monitor.server.impl.logic;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.mq.EmbedMQServer;
import com.broada.carrier.monitor.server.api.client.EventClient;
import com.broada.carrier.monitor.server.api.client.EventListener;
import com.broada.carrier.monitor.server.api.event.NodeChangedEvent;
import com.broada.carrier.monitor.server.api.event.ObjectChangedEvent;
import com.broada.carrier.monitor.server.api.event.ProbeChangedEvent;
import com.broada.carrier.monitor.server.api.event.RecordChangedEvent;
import com.broada.carrier.monitor.server.api.event.ResourceChangedEvent;
import com.broada.carrier.monitor.server.api.event.SystemEvent;
import com.broada.carrier.monitor.server.api.event.TargetStatusChangedEvent;
import com.broada.carrier.monitor.server.api.event.TaskChangedEvent;
import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.mq.client.FailoverConnection;
import com.broada.mq.client.FailoverTopicPublisher;

public class EventBus extends EventClient {
	private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
	private static EventBus instance;
	private FailoverConnection globalConnection;
	private FailoverTopicPublisher innerObjectChangedPublisher;
	private FailoverTopicPublisher globalObjectChangedPublisher;
	private FailoverTopicPublisher globalSystemEventPublisher;

	/**
	 * 获取默认实例
	 * 
	 * @return
	 */
	public static EventBus getDefault() {
		if (instance == null) {
			synchronized (EventBus.class) {
				if (instance == null) {
					try {
						instance = new EventBus();
					} catch (Throwable e) {
						throw ErrorUtil.createRuntimeException("建立事件总线失败", e);
					}
				}
			}
		}
		return instance;
	}

	public EventBus() throws JMSException {
		super(EmbedMQServer.getDefault().getVmConnection());
		try {
			innerObjectChangedPublisher = new FailoverTopicPublisher(EmbedMQServer.getDefault().getVmConnection(),
					ObjectChangedEvent.TOPIC);

			globalConnection = new FailoverConnection(Config.getDefault().getMQUrl(), "carrier.monitor", Config.getDefault()
					.getMQUser(), Config
					.getDefault().getMQPassword());
			globalObjectChangedPublisher = new FailoverTopicPublisher(globalConnection, ObjectChangedEvent.TOPIC);
			globalSystemEventPublisher = new FailoverTopicPublisher(globalConnection, SystemEvent.TOPIC);
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("事件总线建立失败", e);
		}
	}

	public FailoverConnection getGlobalConnection() {
		return globalConnection;
	}

	public void registerObjectChangedListener(EventListener eventListener) {
		logger.debug("对象变更事件监听器注册：" + eventListener);
		addListener(ObjectChangedEvent.TOPIC, eventListener.getClass().getName(), eventListener);
	}

	public void publishObjectChanged(Serializable event) {
		logger.debug("对象变更事件发布：" + event);
		try {
			Message msg = innerObjectChangedPublisher.getSession().createObjectMessage(event);
			innerObjectChangedPublisher.publish(msg);

			if (event instanceof TaskChangedEvent
					|| event instanceof RecordChangedEvent
					|| event instanceof ProbeChangedEvent
					|| event instanceof TargetStatusChangedEvent
					|| event instanceof NodeChangedEvent
					|| event instanceof ResourceChangedEvent) {
				globalObjectChangedPublisher.checkConnected();
				globalObjectChangedPublisher.publish(msg);
			}
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "对象变更事件发布失败", e);
		}
	}

	public void publishSystemEvent(Serializable event) {
		try {
			Message msg = globalSystemEventPublisher.getSession().createObjectMessage(event);
			globalSystemEventPublisher.publish(msg);
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "对象变更事件发布失败", e);
		}
	}
}
