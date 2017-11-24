package uyun.bat.datastore.mq;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.logic.LogicManager;
import uyun.bat.datastore.mq.macro.MacroManager;

public class MetricMQListener implements MessageListener {
	private static final Logger logger = LoggerFactory.getLogger(MetricMQListener.class);
	
	@Override
	public void onMessage(Message message) {
		try {
			if (!(message instanceof ObjectMessage)) {
				return;
			}
			Object object = ((ObjectMessage) message).getObject();
			if (object instanceof List) {
				// mq中的老数据
				List<PerfMetric> metrics = (List<PerfMetric>) object;

				if (metrics != null && metrics.size() > 0) {
					LogicManager.getInstance().getMetricLogic().insertPerf(metrics);
				}
			} else if (object instanceof ComplexMetricData) {
				// 新的mq数据
				ComplexMetricData complexMetricData = (ComplexMetricData) object;
				MacroManager.getInstance().getMetricMacro(complexMetricData.getType()).exec(complexMetricData);
			}
		} catch (JMSException e) {
			logger.warn("ActiveMQ(performance metric) message error：", e);
			if (logger.isDebugEnabled())
				logger.debug("stack:", e);
		} catch (Throwable e) {
			// 防止代码异常导致jms监听整个退出
			logger.warn("ActiveMQ(performance metric)unknow exception：", e);
			if (logger.isDebugEnabled())
				logger.debug("stack:", e);
		}
	}
}
