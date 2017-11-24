package uyun.bat.gateway.agent.service.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import uyun.bat.gateway.agent.entity.PerfMetricVO;

public interface AgentMetricService {
	/**
	 * 插入性能指标
	 * 
	 * @param metrics
	 * @param request
	 */
	void intakePerfMetric(List<PerfMetricVO> metrics, HttpServletRequest request);

}
