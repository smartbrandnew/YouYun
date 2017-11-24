package com.broada.carrier.monitor.server.impl.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.base.restful.BaseMethodController;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.service.ServerMethodService;

@Controller
@RequestMapping("/v1/monitor/methods")
public class ServerMethodController extends BaseMethodController {
	@Autowired
	private ServerMethodService service;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public MonitorMethod[] getMethods(@RequestParam(value = "typeId", required = false) String typeId,
			@RequestParam(value = "nodeId", required = false) String nodeId) {
		if (typeId != null && nodeId != null)
			return service.getMethodsByNodeIdAndType(nodeId, typeId);
		else if (typeId != null)
			return service.getMethodsByTypeId(typeId);
		else
			return service.getMethods();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{code}/create")
	@ResponseBody
	public void createMethod(@RequestBody MonitorMethod method) {
		service.createMethod(method);
	}

}
