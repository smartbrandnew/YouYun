package uyun.bat.monitor.core.mq;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.mq.StateMetricInfo;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.calculate.CalculatorManager;
import uyun.bat.monitor.core.entity.StateMetricData;
import uyun.bat.monitor.core.logic.AppMonitor;
import uyun.bat.monitor.core.util.MonitorQueryUtil;

public class StateMetricMQService implements MessageListener, Consumer {

    private static final Logger logger = LoggerFactory.getLogger(StateMetricMQService.class);

    // 监测器吞吐量计数器
    private static AtomicLong atomic = new AtomicLong(0);
    /**
     * 内存最多保存数据个数
     */
    private int maxLength = 500;

    private int concurrentConsumers = 2;

    private ConsumerManager consumerManager;

    @SuppressWarnings("unused")
    private void init() {
        consumerManager = new ConsumerManager(MonitorType.APP.getCode(), this, concurrentConsumers, maxLength);
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getConcurrentConsumers() {
        return concurrentConsumers;
    }

    public void setConcurrentConsumers(int concurrentConsumers) {
        this.concurrentConsumers = concurrentConsumers;
    }

    public long getCount() {
        return atomic.getAndSet(0);
    }

	public void onMessage(Message message) {
		try {
			if (message instanceof ObjectMessage) {
				Object value = ((ObjectMessage) message).getObject();
				// 新mq Queue队列数据应该都是List;暂不考虑堆积的数据
				if (value instanceof List) {
					for (StateMetricInfo metricInfo : (List<StateMetricInfo>) value) {
						StateMetricData data = getData(metricInfo);
						consumerManager.push(data);
						atomic.incrementAndGet();
					}
				}
			}
		} catch (Throwable e) {
			if (logger.isWarnEnabled())
				logger.warn("Monitor consume state metric message exception:" + e.getMessage());
			if (logger.isDebugEnabled())
				logger.debug("Stack：", e);
		}
	}

    private StateMetricData getData(StateMetricInfo info) {
        StateMetricData data = new StateMetricData(info.getName(),info.getTenantId());
        if (info.getTags() != null && info.getTags().size() > 0) {
            for (Tag tag : info.getTags()) {
                // 去除用户为查询所内置的参数
                if ("tenantId".equals(tag.getKey()))
                    continue;
                else if ("type".equals(tag.getKey()))
                    continue;
                else if ("resourceId".equals(tag.getKey()))
                    continue;
                data.addTag(tag.getKey(), tag.getValue());
            }
        }
        return data;
    }

    @Override
    public void doConsume(String tenantId, List<MQData> mqData) {
        try {
            List<AppMonitor> appMonitors = MonitorQueryUtil.getAppMonitor(tenantId);
            if (appMonitors.size() == 0)
                return;

            List<AppMonitor> matchedMonitors = new ArrayList<>();
            for (MQData mqd : mqData) {
                StateMetricData data = (StateMetricData) mqd;
                Iterator<AppMonitor> iterator=appMonitors.iterator();
                while (iterator.hasNext()){
                    AppMonitor monitor=iterator.next();
                    if (monitor.match(data)){
                        matchedMonitors.add(monitor);
                        iterator.remove();
                    }
                }
            }
            for (AppMonitor monitor : matchedMonitors) {
                CalculatorManager.getInstance().pusthToStateMetricQueue(monitor.getMonitor().getTenantId(),
                        monitor.getMonitor().getId());
            }
        } catch (Throwable e) {
            if (logger.isWarnEnabled())
                logger.warn("Monitor match logic exception:", e);
            if (logger.isDebugEnabled())
                logger.debug("Stack：", e);
        }
    }

}
