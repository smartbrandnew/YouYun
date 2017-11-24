package com.broada.carrier.monitor.base.restful;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.common.util.Base64Util;
import com.broada.carrier.monitor.server.api.client.restful.entity.ExecuteMethodRequest;
import com.broada.carrier.monitor.server.api.entity.SystemInfo;
import com.broada.carrier.monitor.server.api.service.BaseSystemService;

public class BaseSystemController extends BaseController {
	@Autowired
	private BaseSystemService service;			

	@RequestMapping(method = RequestMethod.GET, value = "/infos")
	@ResponseBody
	public SystemInfo[] getInfos() {
		return service.getInfos();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/time")
	@ResponseBody
	public Date getTime() {
		return service.getTime();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/properties/{code}")
	@ResponseBody
	public String getProperty(@PathVariable("code") String code) {
		return service.getProperty(Base64Util.decodeString(code));
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/executeMethod")
	@ResponseBody
	public String executeMethod(@RequestBody ExecuteMethodRequest request) {
		Object result = service.executeMethod(request.getClassName(), request.getMethodName(), request.retParams());
		return Base64Util.encodeObject(result);
	}
}
