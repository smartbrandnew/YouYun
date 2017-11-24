package com.broada.carrier.monitor.server.impl.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.service.ServerTargetTypeService;

@Controller
@RequestMapping("/v1/monitor/targetTypes")
public class ServerTargetTypeController extends BaseController {
	@Autowired
	private ServerTargetTypeService service;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public MonitorTargetType[] getTargetTypes(
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "parentId", required = false) String parentId) {
		if (type != null) {
			if (type.equalsIgnoreCase("resource"))
				return service.getTargetTypesByResource();
			else
				return service.getTargetTypesByNode();
		} else if (parentId != null)
			return service.getTargetTypesByParentId(parentId);
		else
			throw new IllegalArgumentException();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	@ResponseBody
	public MonitorTargetType getTargetType(@PathVariable("id") String id) {
		return service.getTargetType(id);
	}
}
