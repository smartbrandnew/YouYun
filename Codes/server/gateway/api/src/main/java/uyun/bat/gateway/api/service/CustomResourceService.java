package uyun.bat.gateway.api.service;

import org.springframework.jms.core.JmsTemplate;

import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.mq.ResourceInfo;
import uyun.bat.event.api.entity.EventSourceType;

public class CustomResourceService {
	
	private JmsTemplate resourceQueueJmsTemplate;

	public JmsTemplate getResourceQueueJmsTemplate() {
		return resourceQueueJmsTemplate;
	}

	public void setResourceQueueJmsTemplate(JmsTemplate resourceQueueJmsTemplate) {
		this.resourceQueueJmsTemplate = resourceQueueJmsTemplate;
	}
	
	// 若资源从下线转为上线，则发送事件
	public void resourceOnline(Resource resource) {
		ResourceInfo info = new ResourceInfo(resource.getId(), resource.getTenantId(), resource.getHostname(),
				resource.getLastCollectTime(), EventSourceType.OPEN_API.getKey(), OnlineStatus.ONLINE,
				resource.getIpaddr(), resource.getResTagsAll());
		resourceQueueJmsTemplate.convertAndSend(info);
	}
	
	/**
	 * 插入资源离线信息
	 * @param resourceInfo
	 */
	public void readOffline(ResourceInfo resourceInfo){
		if(null == resourceInfo) return;
		resourceQueueJmsTemplate.convertAndSend(resourceInfo);
	}

}
