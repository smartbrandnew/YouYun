package uyun.bat.event.impl.mq;

import java.util.concurrent.atomic.AtomicLong;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.datastore.api.entity.ResourceModify;
import uyun.bat.event.api.logic.EventLogic;
import uyun.bat.event.impl.logic.redis.CustomType;
import uyun.bat.event.impl.logic.redis.RedisCustomQueue;

/**
 * mq --> queue --> redis --> logic
 * 
 */
public class TopicMessageListener implements MessageListener {
	private static Logger logger = LoggerFactory.getLogger(TopicMessageListener.class);

	private static AtomicLong count = new AtomicLong(0);

	private RedisCustomQueue redisUpdate;
	private RedisCustomQueue redisDel;

	@Autowired
	EventLogic eventLogic;

	public void onMessage(Message message) {
		count.incrementAndGet();
		if (!(message instanceof ObjectMessage)) {
			return;
		}
		Object object = null;
		try {
			object = ((ObjectMessage) message).getObject();
		} catch (JMSException e) {
			logger.warn("Message format error when ActiveMQ handle resource event message :{}, handled message count{}", e, count.get());
			if (logger.isDebugEnabled()) {
				logger.debug("Stack:", e);
			}
		}
		if (object != null && object instanceof ResourceModify) {
			ResourceModify resModify = (ResourceModify) object;
			if (resModify.getType().equals(ResourceModify.TYPE_DELETE_RESOURCE)) {
				redisDel.add(resModify);
			} else if (resModify.getType().equals(ResourceModify.TYPE_UPDATE_RESOURCE_TAG)) {
				redisUpdate.add(resModify);
			}
		}
	}

	public void init() {
		redisUpdate = new RedisCustomQueue(CustomType.UPDATE.getType(), eventLogic);
		redisDel = new RedisCustomQueue(CustomType.DEL.getType(), eventLogic);
	}

}
