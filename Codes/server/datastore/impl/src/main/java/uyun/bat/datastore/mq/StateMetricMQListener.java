package uyun.bat.datastore.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.datastore.api.mq.StateMetricData;
import uyun.bat.datastore.logic.LogicManager;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class StateMetricMQListener implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(StateMetricMQListener.class);

    @Override
    public void onMessage(Message message) {
        try {
            if (!(message instanceof ObjectMessage)) {
                return;
            }
            Object object = ((ObjectMessage) message).getObject();
            if (object instanceof StateMetricData) {
                StateMetricData stateMetricData = (StateMetricData) object;
                LogicManager.getInstance().getStateMetricLogic().insertStateMetric(stateMetricData);
            }
        } catch (JMSException e) {
            logger.warn("ActiveMQ(state metric) message exception：", e);
        } catch (Throwable e) {
            //防止代码异常导致jms监听整个退出
            logger.warn("ActiveMQ(state metric) unknow exception：", e);
        }
    }
}
