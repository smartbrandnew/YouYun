package uyun.bat.monitor.impl.logic;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.SimpleResource;
import uyun.bat.datastore.api.mq.ResourceInfo;
import uyun.bat.monitor.core.mq.MQManager;
import uyun.bat.monitor.impl.common.DistributedUtil;
import uyun.bat.monitor.impl.common.ServiceManager;

/**
 *每分钟查询最近1分钟没有上报数据且在线的资源
 * 发送到MQ
 */
public class CheckResourceStateTask {

    private static final Logger logger = LoggerFactory.getLogger(CheckResourceStateTask.class);

    private static final int corePoolSize = 1;
    // 设置40s执行一次
    private static final long period = 40;

    /**
     * 时间间隔
     */
    private static final long interval = 1;

    public void init() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!DistributedUtil.isLeader()){
                    return;
                }
                try{
                    checkResource();
                }catch (Exception e){
                    logger.warn("Check resource offline scheduled task execution exception："+e.getMessage());
                }
            }

        }, 60, period, TimeUnit.SECONDS);
    }

    private void checkResource() {
        long lasCollectTime = System.currentTimeMillis() - interval * 60 * 1000;
		List<SimpleResource> simpleResources = ServiceManager.getInstance().getResourceService()
				.query(OnlineStatus.ONLINE, lasCollectTime);
        if (null == simpleResources || simpleResources.size() < 1) {
            return;
        }
        for (SimpleResource resource : simpleResources) {
            ResourceInfo data = new ResourceInfo(resource.getResourceId(), resource.getTenantId(),
                    resource.getResourceName(), resource.getLastCollectTime(), OnlineStatus.OFFLINE, resource.getIpaddr());
            data.setTags(resource.getTags());
            MQManager.getInstance().getStateMQService().save(data);
        }
    }

}
