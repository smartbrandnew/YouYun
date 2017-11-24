package uyun.bat.gateway.agent.servicetest;

import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.api.mq.StateMetricData;
import uyun.bat.gateway.api.service.CustomMetricService;

public class CustomMetricServiceTest extends CustomMetricService {
	/**
	 * 批量插入性能指标
	 * 
	 * @param metrics
	 * @return
	 */
	public int insertPerf(ComplexMetricData complexMetricData) {
		if (complexMetricData == null)
			return 0;
		return complexMetricData.getPerfMetricList() != null ? complexMetricData.getPerfMetricList().size() : 0;
	}

	/**
	 * 插入状态指标
	 * 
	 * @param stateMetricData
	 * @return
	 */
	public int insertStateMetric(StateMetricData stateMetricData) {
		if (null == stateMetricData || null == stateMetricData.getStateMetrics()
				|| stateMetricData.getStateMetrics().isEmpty()) {
			return 0;
		}
		return stateMetricData.getStateMetrics() != null ? stateMetricData.getStateMetrics().size() : 0;

	}
}
