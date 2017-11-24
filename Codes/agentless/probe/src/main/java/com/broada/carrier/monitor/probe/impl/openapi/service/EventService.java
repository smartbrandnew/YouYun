package com.broada.carrier.monitor.probe.impl.openapi.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.openapi.entity.EventVO;
import com.broada.carrier.monitor.probe.impl.util.HTTPClientUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EventService {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventService.class);
	private static final String url = Config.getDefault().getProperty("openapi.events");
	private static ObjectMapper mapper = new ObjectMapper();

	public boolean postHosts(List<EventVO> events) {
		try {
			String json = mapper.writeValueAsString(events);
			String sign = HTTPClientUtils.post(url, json);
			if (sign == null) {
				LOG.warn("上报ipmi硬件故障信息失败,数量:{}", events.size());
				return false;
			}
			LOG.warn("上报ipmi硬件故障信息成功,数量:{}", events.size());
			return true;
		} catch (JsonProcessingException e) {
			LOG.warn("json转换异常: ", e);
		} catch (Exception e) {
			LOG.warn("异常: ", e);
		}
		return false;
	}
}
