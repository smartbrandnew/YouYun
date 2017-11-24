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

import uyun.bat.datastore.api.mq.ResourceInfo;
import uyun.bat.monitor.core.calculate.CalculatorManager;
import uyun.bat.monitor.core.entity.ResourceData;
import uyun.bat.monitor.core.logic.HostMonitor;
import uyun.bat.monitor.core.util.MonitorQueryUtil;
import uyun.bat.monitor.impl.facade.MonitorEventCreator;

public class ResourceMQService implements MessageListener{
    private static final Logger logger = LoggerFactory.getLogger(ResourceMQService.class);

    // 监测器吞吐量计数器
    private static AtomicLong atomic = new AtomicLong(0);

    public long getCount() {
        return atomic.getAndSet(0);
    }

    public void onMessage(Message message) {
        try {
            if (!(message instanceof ObjectMessage)) {
                return;
            }
            Object object = ((ObjectMessage) message).getObject();
            if (object instanceof ResourceInfo) {
                doConsume((ResourceInfo)object);
                atomic.incrementAndGet();
            }
        } catch (Throwable e) {
            if (logger.isWarnEnabled())
                logger.warn("Monitor consume online resource message exception:" + e.getMessage());
            if (logger.isDebugEnabled())
                logger.debug("Stack：", e);
        }
    }

    private ResourceData getData(ResourceInfo info) throws Exception {
        return new ResourceData(info.getResourceId(), info.getTenantId(), info.getHostname(),
                info.getLastCollectTime(), info.getEventSourceType(), info.getOnlineStatus(), info.getIpaddr());
    }

    public void doConsume(ResourceInfo data) {
        try {

            List<HostMonitor> hostMonitors = MonitorQueryUtil.getHostMonitor(data.getTenantId());
            if (hostMonitors.size() == 0){
                //没有监测器上下线处理
            	MonitorEventCreator.onResourceOnline(getData(data));
                return;
            }

            List<HostMonitor> matchedMonitors = new ArrayList<HostMonitor>();
            Iterator<HostMonitor> iterator=hostMonitors.iterator();
            while (iterator.hasNext()){
                HostMonitor monitor=iterator.next();
                //判断主机名以及资源标签是否匹配
                if (monitor.match(data.getHostname(), data.getTags())){
                    matchedMonitors.add(monitor);
                    iterator.remove();
                  }
            }

            if (matchedMonitors.isEmpty()){
                //监测器未监测到做上下线 处理
                MonitorEventCreator.onResourceOnline(getData(data));
                return;
            }

			for (HostMonitor monitor : matchedMonitors) {
				CalculatorManager.getInstance().pusthToResourceQueue(monitor.getMonitor().getTenantId(),
                        monitor.getMonitor().getId(),data.getResourceId(),data.getEventSourceType(),data.getOnlineStatus().getId(),data.getHostname(),data.getIpaddr());
			}

        } catch (Throwable e) {
            if (logger.isWarnEnabled())
                logger.warn("Host monitor match logic exception:", e);
            if (logger.isDebugEnabled())
                logger.debug("Stack：", e);
        }
    }
}
