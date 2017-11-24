package uyun.bat.gateway.api.service;

import org.springframework.jms.core.JmsTemplate;

import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.api.mq.StateMetricData;
import uyun.bat.gateway.api.mq.MqStateTask;

/**
 * 该类与指标工程耦合...
 */
public class CustomMetricService {
	private JmsTemplate jmsTemplate;
	private JmsTemplate stateJmsTemplate;
	
	public JmsTemplate getStateJmsTemplate() {
		return stateJmsTemplate;
	}

	public void setStateJmsTemplate(JmsTemplate stateJmsTemplate) {
		this.stateJmsTemplate = stateJmsTemplate;
	}


	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	/**
	 * 批量插入性能指标
	 * 
	 * @return
	 */
	public int insertPerf(ComplexMetricData complexMetricData) {
		if (complexMetricData == null)
			return 0;
		if (!MqStateTask.isHealthy()) {
			throw new RuntimeException("MQ State exception, forbid data report!");
		}
		jmsTemplate.convertAndSend(complexMetricData);
		return complexMetricData.getPerfMetricList() != null ? complexMetricData.getPerfMetricList().size() : 0;
	}

	/**
	 * 插入状态指标
	 * @param stateMetricData
	 * @return
	 */
	public int insertStateMetric(StateMetricData stateMetricData){
		if(null==stateMetricData||null==stateMetricData.getStateMetrics()||stateMetricData.getStateMetrics().isEmpty()){
			return 0;
		}
		if (!MqStateTask.isHealthy()) {
			throw new RuntimeException("MQ State exception, forbid data report!");
		}
		stateJmsTemplate.convertAndSend(stateMetricData);
		return stateMetricData.getStateMetrics()!=null?stateMetricData.getStateMetrics().size():0;

	}
	
}
