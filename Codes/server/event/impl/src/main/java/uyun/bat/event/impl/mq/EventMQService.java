package uyun.bat.event.impl.mq;


import org.springframework.jms.core.JmsTemplate;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.mq.EventInfo;

public class EventMQService {
	// 事件通知队列（已保存的事件）
	private JmsTemplate eventJMSTemplate;

	private JmsTemplate alertTemplate;

	public JmsTemplate getEventJMSTemplate() {
		return eventJMSTemplate;
	}

	public void setEventJMSTemplate(JmsTemplate eventJMSTemplate) {
		this.eventJMSTemplate = eventJMSTemplate;
	}

	public void eventSaved(EventInfo eventInfo) {
		eventJMSTemplate.convertAndSend(eventInfo);
	}

	public void alertEventSaved(Event event) { alertTemplate.convertAndSend(event);}

	public JmsTemplate getAlertTemplate() {
		return alertTemplate;
	}

	public void setAlertTemplate(JmsTemplate alertTemplate) {
		this.alertTemplate = alertTemplate;
	}
}
