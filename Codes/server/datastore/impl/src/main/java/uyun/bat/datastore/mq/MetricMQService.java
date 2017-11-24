package uyun.bat.datastore.mq;

import org.springframework.jms.core.JmsTemplate;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.mq.MetricInfo;
import uyun.bat.datastore.api.mq.ResourceInfo;
import uyun.bat.datastore.api.mq.StateMetricInfo;

import java.util.List;

public class MetricMQService {
	private JmsTemplate jmsTemplate;
	private JmsTemplate stateMetricJmsTemplate;
	private JmsTemplate resourceJmsTemplate;
	//资源更新发布订阅模板
	private JmsTemplate resourceModifyjmsTemplate;
	
	public MetricMQService() {
	}

	
	public JmsTemplate getResourceModifyjmsTemplate() {
		return resourceModifyjmsTemplate;
	}


	public void setResourceModifyjmsTemplate(JmsTemplate resourceModifyjmsTemplate) {
		this.resourceModifyjmsTemplate = resourceModifyjmsTemplate;
	}


	public JmsTemplate getResourceJmsTemplate() {
		return resourceJmsTemplate;
	}

	public void setResourceJmsTemplate(JmsTemplate resourceJmsTemplate) {
		this.resourceJmsTemplate = resourceJmsTemplate;
	}

	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public JmsTemplate getStateMetricJmsTemplate() {
		return stateMetricJmsTemplate;
	}

	public void setStateMetricJmsTemplate(JmsTemplate stateMetricJmsTemplate) {
		this.stateMetricJmsTemplate = stateMetricJmsTemplate;
	}

	public void metricSaved(List<MetricInfo> metricInfoList) {
		if (metricInfoList != null && metricInfoList.size() > 0)
			jmsTemplate.convertAndSend(metricInfoList);
	}

	public Object metricReceived() {
		Object obj = jmsTemplate.receiveAndConvert();
		return obj;
	}

	public void metricSaved(String destinationName, List<PerfMetric> metrics) {
		jmsTemplate.convertAndSend(destinationName, metrics);
	}

	public void stateMetricSaved(List<StateMetricInfo> stateMetricInfoList) {
		if (stateMetricInfoList != null && stateMetricInfoList.size() > 0)
			stateMetricJmsTemplate.convertAndSend(stateMetricInfoList);
	}

	public void resourceSaved(ResourceInfo resourceInfo){
		resourceJmsTemplate.convertAndSend(resourceInfo);
	}

	
}
