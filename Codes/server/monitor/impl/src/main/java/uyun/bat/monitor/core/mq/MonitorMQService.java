package uyun.bat.monitor.core.mq;

import org.springframework.jms.core.JmsTemplate;
import uyun.bat.monitor.api.entity.Monitor;

public class MonitorMQService {
    private JmsTemplate monitorQueueJmsTemplate;

    public JmsTemplate getMonitorQueueJmsTemplate() {
        return monitorQueueJmsTemplate;
    }

    public void setMonitorQueueJmsTemplate(JmsTemplate monitorQueueJmsTemplate) {
        this.monitorQueueJmsTemplate = monitorQueueJmsTemplate;
    }

    public void save(Monitor monitor){
        monitorQueueJmsTemplate.convertAndSend(monitor);
    }

}
