package uyun.bat.event.impl.mq;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.event.api.logic.EventLogic;
import uyun.bat.monitor.api.mq.MonitorEvent;

public class MonitorMQListener implements MessageListener {
	private static final Logger logger = LoggerFactory.getLogger(MonitorMQListener.class);

	@Autowired
	private EventLogic eventLogic;

	@Override
	@SuppressWarnings("unchecked")
	public void onMessage(Message message) {
		try {
			if (!(message instanceof ObjectMessage) || !(((ObjectMessage) message).getObject() instanceof MonitorEvent)) {
				return;
			}
			MonitorEvent monitorEvent = (MonitorEvent) ((ObjectMessage) message).getObject();

			eventLogic.updateEventTypeByMonitorId(monitorEvent.getTenantId(), monitorEvent.getMonitorId());
		} catch (Throwable e) {
			logger.warn("jms message exception：" + e.getMessage());
			if (logger.isDebugEnabled())
				logger.info("Stack：", e);
		}
	}
}
