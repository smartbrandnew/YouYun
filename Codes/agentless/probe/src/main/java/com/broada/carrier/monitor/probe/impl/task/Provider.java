package com.broada.carrier.monitor.probe.impl.task;

import java.util.List;

import com.broada.carrier.monitor.probe.impl.openapi.entity.ResourceDetail;

public interface Provider {
	
	/**
	 * 获取资源详情
	 * @return
	 */
	public List<ResourceDetail> getResourceDetail() throws Exception;
	
}
