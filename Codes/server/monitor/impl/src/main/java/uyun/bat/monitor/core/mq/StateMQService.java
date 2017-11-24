package uyun.bat.monitor.core.mq;

import org.springframework.jms.core.JmsTemplate;
import uyun.bat.datastore.api.mq.ResourceInfo;

public class StateMQService {
    private JmsTemplate jmsTemplate;

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void save(ResourceInfo data){
        jmsTemplate.convertAndSend(data);
    }

}
