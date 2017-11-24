package uyun.bat.datastore.mq;

import java.util.List;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.datastore.api.entity.RelativeTime;
import uyun.bat.datastore.api.entity.ResourceModify;
import uyun.bat.datastore.logic.LogicManager;
import uyun.bat.datastore.overview.logic.EnsureAccuracy;
import uyun.bat.datastore.service.ServiceManager;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

public class TopicMessageListener implements MessageListener {
	private static Logger logger = LoggerFactory.getLogger(TopicMessageListener.class);

	@Override
	public void onMessage(Message message) {
		try {
			if (!(message instanceof ObjectMessage)) {
				return;
			}
			Object object = ((ObjectMessage) message).getObject();
			if (!(object instanceof ResourceModify))
				return;

			if (((ResourceModify) object).getType().equals(ResourceModify.TYPE_DELETE_RESOURCE)) {
				ResourceModify resModify = (ResourceModify) object;

				// 维护总览数据
				EnsureAccuracy.onResourceChange(resModify.getTenantId(), true);
				
				String tenantId = resModify.getTenantId();
				String resourceId = resModify.getResourceId();
				List<String> metricNames = ServiceManager.getInstance().getMetricService()
						.getMetricNamesByTenantId(tenantId);
				SetMultimap<String, String> tags = HashMultimap.create();
				tags.put("tenantId", tenantId);
				tags.put("resourceId", resourceId);
				for (String metricName : metricNames) {
					LogicManager
							.getInstance()
							.getMetricClean()
							.deleteMetricData(metricName, tags,
									new RelativeTime(5, uyun.bat.datastore.api.entity.TimeUnit.YEARS));
				}

				deleteState(tenantId,resourceId);

			} else {
				// 维护总览数据
				EnsureAccuracy.onResourceChange(((ResourceModify) object).getTenantId(), false);
			}
			
		} catch (Throwable e) {
			logger.warn("ActiveMQ获取执行资源对应的性能及状态指标删除任务失败:{}", e);
			logger.warn("ActiveMQ fail to gain task that delete resource's performance and state metric:{}", e);
		}
	}


	private void deleteState(String tenantId, String resourceId) {

		LogicManager.getInstance().getStateMetricLogic().deleteByResId(tenantId,resourceId);

		String[] tags=new String[]{"tenantId:"+tenantId,"resourceId:"+resourceId};
		ServiceManager.getInstance().getStateService().deleteCheckpoints(null,tags);

	}

}
