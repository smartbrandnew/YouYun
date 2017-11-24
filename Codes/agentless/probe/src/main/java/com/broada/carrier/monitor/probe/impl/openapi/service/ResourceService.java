package com.broada.carrier.monitor.probe.impl.openapi.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.openapi.entity.HostVO;
import com.broada.carrier.monitor.probe.impl.openapi.entity.ResourceDetail;
import com.broada.carrier.monitor.probe.impl.util.HTTPClientUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ResourceService {
	private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);
	private static final String url = Config.getDefault().getProperty("openapi.hosts");
	private static final String url_resource_detail = Config.getDefault().getProperty("openapi.resource.detail");
	private static ObjectMapper mapper = new ObjectMapper();

	public boolean postHosts(List<HostVO> hosts) {
		try {
			String json = mapper.writeValueAsString(hosts);
			String sign = HTTPClientUtils.post(url, json);
			if (sign == null) {
				logger.warn("主机资源上报失败,数量:{},内容:{}", hosts.size(), json);
				return false;
			}
			logger.warn("主机资源上报成功,数量:{},内容:{}", hosts.size(), json);
			return true;
		} catch (JsonProcessingException e) {
			logger.warn("json转换异常: ", e);
		} catch (Exception e) {
			logger.warn("异常: ", e);
		}
		return false;
	}
	
	/**
	 * 上报资源详情
	 * @param detail
	 * @return
	 */
	public boolean postResourceDetail(List<ResourceDetail> detail) {
		try {
			String json = mapper.writeValueAsString(detail);
			String sign = HTTPClientUtils.post(url_resource_detail, json);
			if (sign == null) {
				logger.warn("资源详情上报失败,数量:{}", detail.size());
				return false;
			}
			logger.warn("资源详情上报成功,数量:{}", detail.size());
			return true;
		} catch (JsonProcessingException e) {
			logger.warn("json转换异常: ", e);
		} catch (Exception e) {
			logger.warn("异常: ", e);
		}
		return false;
	}
	
}
