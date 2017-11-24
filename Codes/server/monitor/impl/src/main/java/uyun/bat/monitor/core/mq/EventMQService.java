package uyun.bat.monitor.core.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.event.api.entity.Tag;
import uyun.bat.event.api.mq.EventInfo;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.calculate.CalculatorManager;
import uyun.bat.monitor.core.entity.EventData;
import uyun.bat.monitor.core.entity.TagEntry;
import uyun.bat.monitor.core.logic.EventMonitor;
import uyun.bat.monitor.core.util.MonitorQueryUtil;
import uyun.bat.monitor.impl.Startup;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class EventMQService implements MessageListener, Consumer {
	private static final Logger logger = LoggerFactory.getLogger(EventMQService.class);
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
		consumerManager = new ConsumerManager(MonitorType.EVENT.getCode(), this, concurrentConsumers, maxLength);
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
				EventData eventData = getData((EventInfo) ((ObjectMessage) message).getObject());
				consumerManager.push(eventData);
			}
			atomic.incrementAndGet();
		} catch (Throwable e) {
			if (logger.isWarnEnabled())
				logger.warn("Monitor consume Event message exception:" + e.getMessage());
			if (logger.isDebugEnabled())
				logger.debug("Stack：", e);
		}
	}

	private EventData getData(EventInfo eventInfo) throws Exception {
		EventData eventData = new EventData(eventInfo.getTitle(), eventInfo.getContent(), eventInfo.getTenantId(),
				eventInfo.getResId(), eventInfo.getIdentity(), eventInfo.getOccurTime(), eventInfo.getServerity());
		if (eventInfo.getTags() != null && eventInfo.getTags().size() > 0) {
			for (Tag tag : eventInfo.getTags()) {
				// 去除用户为查询所内置的参数
				if ("tenantId".equals(tag.getKey()))
					continue;
				else if ("type".equals(tag.getKey()))
					continue;
				else if ("resourceId".equals(tag.getKey()))
					continue;
				eventData.addTag(tag.getKey(), tag.getValue());
			}
		}
		return eventData;
	}

	@Override
	public void doConsume(String tenantId, List<MQData> mqData) {
		try {
			List<EventMonitor> eventMonitors = MonitorQueryUtil.getEventMonitor(tenantId);

			if (eventMonitors.size() == 0)
				return;

			for (MQData mqd : mqData) {
				EventData data = (EventData) mqd;
				for (int i = 0; i < eventMonitors.size(); i++) {
					EventMonitor monitor = eventMonitors.get(i);
					if (monitor.match(data)) {
						//事件监测器需要用到标题和内容
						push(monitor.getMonitor().getTenantId(),monitor.getMonitor().getId(),data.getTitle(),data.getContent(),data.isRecover(),data.getResId());
						eventMonitors.remove(i);
						i--;
					}
				}
			}
		} catch (Throwable e) {
			if (logger.isWarnEnabled())
				logger.warn("Monitor match logic exception:", e);
			if (logger.isDebugEnabled())
				logger.debug("Stack：", e);
		}
	}

	private void push(String tenantId, String monitorId, String title, String content,boolean recover,String resId) {
		CalculatorManager.getInstance().pushToEventQueue(tenantId,monitorId,title,content,recover,resId);
	}

	public static void main(String[] args) {
		Startup.getInstance().startup();
		EventData eventData = new EventData("success", "success", "94baaadca64344d2a748dff88fe7159e",
				"06f5af1b-7d75-4ff0-98e8-1aa910ce0b62", "sdfsfd", new Timestamp(System.currentTimeMillis()), (short) 1);
		List<TagEntry> tags = new ArrayList<>();
		tags.add(new TagEntry("host", "202.101.178.39"));
		eventData.setTags(tags);
		EventMQService service = (EventMQService) Startup.getInstance().getBean("eventMQService");
		service.consumerManager.push(eventData);
	}

}