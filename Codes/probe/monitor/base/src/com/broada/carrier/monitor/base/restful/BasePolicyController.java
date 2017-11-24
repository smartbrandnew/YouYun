package com.broada.carrier.monitor.base.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.service.BasePolicyService;

@Controller
@RequestMapping("/v1/monitor/policies")
public class BasePolicyController extends BaseController {
	@Autowired
	private BasePolicyService service;
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody	
	public MonitorPolicy[] getPolicies() {
		return service.getPolicies();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{code}")
	@ResponseBody	
	public MonitorPolicy getPolicy(@PathVariable("code") String code) {
		return service.getPolicy(code);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody	
	public void savePolicy(@RequestBody MonitorPolicy policy) {		
		service.savePolicy(policy);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{code}/delete")
	@ResponseBody	
	public OperatorResult deletePolicy(@PathVariable("code") String code) {
		return service.deletePolicy(code);
	}
}
