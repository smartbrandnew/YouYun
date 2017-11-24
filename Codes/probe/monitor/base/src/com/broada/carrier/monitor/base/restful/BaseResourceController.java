package com.broada.carrier.monitor.base.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.service.BaseResourceService;

public class BaseResourceController extends BaseController {
	@Autowired
	private BaseResourceService service;
	
	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	@ResponseBody	
	public MonitorResource getResource(@PathVariable String id) {
		return service.getResource(id);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody	
	public String saveResource(@RequestBody MonitorResource resources) {		
		return service.saveResource(resources);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/delete")
	@ResponseBody	
	public OperatorResult deleteResource(@PathVariable String id) {
		return service.deleteResource(id);
	}
}
