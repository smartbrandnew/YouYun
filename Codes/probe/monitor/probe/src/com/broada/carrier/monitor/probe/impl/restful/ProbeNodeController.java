package com.broada.carrier.monitor.probe.impl.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.base.restful.BaseNodeController;
import com.broada.carrier.monitor.probe.api.service.ProbeNodeService;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;

@Controller
@RequestMapping("/v1/probe/nodes")

public class ProbeNodeController extends BaseNodeController {
	@Autowired
	private ProbeNodeService service;
		
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public MonitorNode[] getNodes() {
		return service.getNodes();
	}
}
