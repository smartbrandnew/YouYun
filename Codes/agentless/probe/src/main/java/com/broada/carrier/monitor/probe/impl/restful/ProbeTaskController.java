package com.broada.carrier.monitor.probe.impl.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.base.restful.BaseTaskController;
import com.broada.carrier.monitor.probe.api.client.restful.entity.SaveTaskRequest;
import com.broada.carrier.monitor.probe.api.service.ProbeTaskService;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

@Controller
@RequestMapping("/v1/probe/tasks")

public class ProbeTaskController extends BaseTaskController {
	@Autowired
	private ProbeTaskService service;
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public MonitorTask[] getTasks() {
		return service.getTasks();
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public void saveTask(@RequestBody SaveTaskRequest request) {		
		service.saveTask(request.getTask(), request.getInstances(), request.getRecord());
	}		
}
