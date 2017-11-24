package uyun.bat.monitor.core.mq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.mq.MetricInfo;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.calculate.CalculatorManager;
import uyun.bat.monitor.core.entity.MetricData;
import uyun.bat.monitor.core.entity.TagEntry;
import uyun.bat.monitor.core.logic.MetricMonitor;
import uyun.bat.monitor.core.util.MonitorQueryUtil;
import uyun.bat.monitor.impl.Startup;

public class MetricMQService implements MessageListener, Consumer {
	private static final Logger logger = LoggerFactory.getLogger(MetricMQService.class);
	// 监测器吞吐量计数器
	private static AtomicLong atomic = new AtomicLong(0);

	private ConsumerManager consumerManager;
	/**
	 * 内存最多保存数据个数
	 */
	private int maxLength = 500;

	private int concurrentConsumers = 2;

	@SuppressWarnings("unused")
	private void init() {
		consumerManager = new ConsumerManager(MonitorType.METRIC.getCode(), this, concurrentConsumers, maxLength);
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
		// 暂缺如下考虑
		// 1.dubbox服务获取失败
		// 2.指标数据消费异常
		try {
			List<MetricData> metrics = generateData(message);
			if (metrics == null || metrics.isEmpty())
				return;
			for (MetricData metric : metrics) {
				if (metric != null && metric.getTenantId() != null && metric.getTenantId().length() > 0) {
					consumerManager.push(metric);
				}
				atomic.incrementAndGet();
			}
		} catch (Throwable e) {
			if (logger.isWarnEnabled())
				logger.warn("Monitor consume metric data exception:" + e.getMessage());
			if (logger.isDebugEnabled())
				logger.debug("Stack：", e);
		}
	}

	@SuppressWarnings("unchecked")
	private List<MetricData> generateData(Message message) throws Exception {
		if (!(message instanceof ObjectMessage))
			return null;

		List<MetricData> metrics = new ArrayList<MetricData>();

		Object value = ((ObjectMessage) message).getObject();
		// 新mq Queue队列数据应该都是List;暂不考虑堆积的数据
		if (value instanceof List) {
			for (MetricInfo metricInfo : (List<MetricInfo>) value) {
				MetricData metric = new MetricData(metricInfo.getName(), metricInfo.getTenantId());
				if (metricInfo.getTags() != null && metricInfo.getTags().size() > 0) {
					for (Tag tag : metricInfo.getTags()) {
						// 去除为用户查询所内置的参数
						if ("tenantId".equals(tag.getKey()))
							continue;
						else if ("type".equals(tag.getKey()))
							continue;
						else if ("resourceId".equals(tag.getKey()))
							continue;

						metric.addTag(tag.getKey(), tag.getValue());
					}
				}
				metrics.add(metric);
			}
		}
		return metrics;
	}

	@Override
	public void doConsume(String tenantId, List<MQData> mqData) {
		try {
			List<MetricMonitor> metricMonitors = MonitorQueryUtil.getMetricMonitor(tenantId);

			if (metricMonitors.size() == 0)
				return;

			List<MetricMonitor> matchedMonitors = new ArrayList<MetricMonitor>();
			for (MQData mqd : mqData) {
				MetricData data = (MetricData) mqd;
				for (int i = 0; i < metricMonitors.size(); i++) {
					MetricMonitor monitor = metricMonitors.get(i);
					if (monitor.match(data)) {
						matchedMonitors.add(monitor);
						metricMonitors.remove(i);
						i--;
					}
				}
			}
			for (MetricMonitor monitor : matchedMonitors) {
				CalculatorManager.getInstance().pushToMetricQueue(monitor.getMonitor().getTenantId(),
						monitor.getMonitor().getId());
			}
		} catch (Throwable e) {
			if (logger.isWarnEnabled())
				logger.warn("Monitor match logic exception:", e);
			if (logger.isDebugEnabled())
				logger.debug("Stack：", e);
		}
	}

	public static void main(String[] args) {
		Startup.getInstance().startup();
		MetricMQService m = (MetricMQService) Startup.getInstance().getBean("metricMQService");

		MetricData metric = new MetricData("system.cpu.system", "94baaadca64344d2a748dff88fe7159e");
		List<TagEntry> tags = new ArrayList<TagEntry>();
		tags.add(new TagEntry("host", "10.1.11.9"));
		metric.setTags(tags);
		m.consumerManager.push(metric);
	}

}
