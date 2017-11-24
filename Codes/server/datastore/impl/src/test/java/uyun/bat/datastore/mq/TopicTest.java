package uyun.bat.datastore.mq;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.entity.ResourceModify;
import uyun.bat.datastore.api.entity.ResourceTag;

public class TopicTest {
	public static void main(String[] args){
		MQManager manager=Startup.getInstance().getBean(MQManager.class);
		for(int i=0;i<10;i++){
		ResourceModify resDel=new ResourceModify(UUID.randomUUID().toString(), UUID.randomUUID().toString(),null,ResourceModify.TYPE_DELETE_RESOURCE);
		manager.getMetricMQService().getResourceModifyjmsTemplate().convertAndSend("测试资源删除："+new Date());

		ResourceModify resModify=new ResourceModify(UUID.randomUUID().toString(), UUID.randomUUID().toString(),new ArrayList<ResourceTag>(),ResourceModify.TYPE_UPDATE_RESOURCE_TAG);
		manager.getMetricMQService().getResourceModifyjmsTemplate().convertAndSend("测试资源更新："+new Date());
		}
	}
}
