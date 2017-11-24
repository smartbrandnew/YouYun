package com.broada.carrier.monitor.probe.impl.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.base.restful.BaseSystemController;
import com.broada.carrier.monitor.probe.api.service.ProbeSystemService;

@Controller
@RequestMapping("/v1/probe/system")

public class ProbeSystemController extends BaseSystemController {
	@Autowired
	private ProbeSystemService service;

	public ProbeSystemController() {
		super();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/deleteAll")
	@ResponseBody
	public void deleteAll() {
		service.deleteAll();
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/exit")
	@ResponseBody
	public void exit(@RequestBody String reason) {
		service.exit(reason);
	}
}
