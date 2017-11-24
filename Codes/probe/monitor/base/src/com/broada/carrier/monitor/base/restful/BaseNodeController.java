package com.broada.carrier.monitor.base.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.service.BaseNodeService;

public class BaseNodeController extends BaseController {
	@Autowired
	private BaseNodeService service;
	
	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	@ResponseBody	
	public MonitorNode getNode(@PathVariable("id") String id) {
		return service.getNode(id);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody	
	public String saveNode(@RequestBody MonitorNode node) {		
		return service.saveNode(node);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/delete")
	@ResponseBody	
	public OperatorResult deleteNode(@PathVariable("id") String id) {
		return service.deleteNode(id);
	}
}
