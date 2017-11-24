package com.broada.carrier.monitor.probe.impl.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.base.restful.BaseMethodController;
import com.broada.carrier.monitor.probe.api.service.ProbeMethodService;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

@Controller
@RequestMapping("/v1/probe/methods")

public class ProbeMethodController extends BaseMethodController {
	@Autowired
	private ProbeMethodService service;
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public MonitorMethod[] getMethods() {
		return service.getMethods();
	}
}
