package com.broada.carrier.monitor.server.impl.restful;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.server.api.service.ServerTypeService;

@Controller
@RequestMapping("/v1/monitor")
public class ServerTypeController extends BaseController {
	@Autowired
	private ServerTypeService service;

	@RequestMapping(method = RequestMethod.GET, value = "/types")
	@ResponseBody
	public MonitorType[] getTypesByTargetTypeId(
			@RequestParam(value = "targetTypeId", required = false) String targetTypeId) {
		if (targetTypeId == null || targetTypeId.isEmpty())
			return service.getTypes();
		else
			return service.getTypesByTargetTypeId(targetTypeId);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/types/{id}")
	@ResponseBody
	public MonitorType getType(@PathVariable("id") String id) {
		return service.getType(id);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/items")
	@ResponseBody
	public MonitorItem[] getItems() {
		return service.getItems();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/items/{code}")
	@ResponseBody
	public MonitorItem getItem(@PathVariable("code") String code) {
		return service.getItem(code);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/methodTypes/{id}")
	@ResponseBody
	public MonitorMethodType getMethodType(@PathVariable("id") String id) {
		return service.getMethodType(id);
	}
}
