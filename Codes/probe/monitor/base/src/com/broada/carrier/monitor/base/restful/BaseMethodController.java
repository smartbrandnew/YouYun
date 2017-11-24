package com.broada.carrier.monitor.base.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.service.BaseMethodService;

public class BaseMethodController extends BaseController {
	@Autowired
	private BaseMethodService service;

	@RequestMapping(method = RequestMethod.GET, value = "/{code}")
	@ResponseBody
	public MonitorMethod getMethodByCode(@PathVariable("code") String code) {
		return service.getMethod(code);
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public void saveMethod(@RequestBody MonitorMethod method) {
		service.saveMethod(method);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{code}/delete")
	@ResponseBody
	public OperatorResult deleteMethod(@PathVariable("code") String code) {
		return service.deleteMethod(code);
	}
}
