package com.broada.carrier.monitor.probe.impl.openapi.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.error.CommTimeoutException;
import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.openapi.entity.CheckpointVO;
import com.broada.carrier.monitor.probe.impl.openapi.entity.PerfMetricVO;
import com.broada.carrier.monitor.probe.impl.sync.entity.LinkStat;
import com.broada.carrier.monitor.probe.impl.util.HTTPClientUtils;
import com.broada.carrier.monitor.probe.impl.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MetricService {
	private static final Logger logger = LoggerFactory.getLogger(MetricService.class);
	private static final String perf_url = Config.getDefault().getProperty("openapi.perfmetrics");
	private static final String state_url = Config.getDefault().getProperty("openapi.checkpoints");
	private static final String link_state_url = Config.getDefault().getProperty("openapi.linkstats");
	private static ObjectMapper mapper = new ObjectMapper();

	public boolean postPerfMetrics(List<PerfMetricVO> metrics) {
		try {
			String json = mapper.writeValueAsString(metrics);
			String sign = HTTPClientUtils.post(perf_url, json);
			if (sign == null) {
				logger.warn("性能指标上报失败,数量:{}个,内容:{}", metrics.size(), json);
				return false;
			}else
				logger.debug("性能指标上报成功,数量:{}个", metrics.size());
			String metric = Config.getDefault().getProperty("exposed.upload.metric", "");
			if(!StringUtils.isNullOrBlank(metric) && json.contains(metric))
				logger.info("已正确上报了{}指标", metric);
			return true;
		} catch (JsonProcessingException e1) {
			logger.warn("json转换异常: ", e1);
		} catch (Exception e) {
			logger.warn("异常: ", e);
			throw new CommTimeoutException("性能指标上报失败:", e);
		}
		return false;
	}

	public boolean postCheckPoints(List<CheckpointVO> metrics) {
		try {
			String json = mapper.writeValueAsString(metrics);
			String sign = HTTPClientUtils.post(state_url, json);
			if (sign == null) {
				logger.warn("状态指标上报失败,数量:{}个", metrics.size());
				return false;
			}
			return true;
		} catch (JsonProcessingException e1) {
			logger.warn("json转换异常: ", e1);
		} catch (Exception e) {
			logger.warn("异常: ", e);
			throw new CommTimeoutException("状态指标上报失败:", e);
		}
		return false;
	}
	
	/**
	 * 发送上报状态概述信息
	 * @param stats
	 * @return
	 */
	public boolean postLinkStat(List<LinkStat> stats) {
		try {
			String json = mapper.writeValueAsString(stats);
			String sign = HTTPClientUtils.post(link_state_url, json);
			if (sign == null) {
				logger.warn("链接概述信息上报失败,数量:{}个", stats.size());
				return false;
			}
			return true;
		} catch (JsonProcessingException e1) {
			logger.error("json转换异常: ", e1);
		} catch (Exception e) {
			logger.error("异常: ", e);
			throw new CommTimeoutException("链接概述信息上报失败:", e);
		}
		return false;
	}

}
