package com.broada.carrier.monitor.server.impl.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;
import com.broada.carrier.monitor.server.api.service.ServerTargetGroupService;

@Controller
@RequestMapping("/v1/monitor/targetGroups")
public class ServerTargetGroupController extends BaseController {
	@Autowired
	private ServerTargetGroupService service;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public MonitorTargetGroup[] getTargetGroups(
			@RequestParam(value = "parentId", required = false) String parentId) {
		if (parentId != null && parentId.isEmpty())
			parentId = null;
		return service.getGroupsByParentId(parentId);
	}
}
