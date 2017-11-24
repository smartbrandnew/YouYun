package com.broada.carrier.monitor.server.impl.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.base.restful.BaseResourceController;
import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;
import com.broada.carrier.monitor.server.api.service.ServerResourceService;

@Controller
@RequestMapping("/v1/monitor/resources")
public class ServerResourceController extends BaseResourceController {
	@Autowired
	private ServerResourceService service;
	
	@RequestMapping(method = RequestMethod.GET, value = "{id}/nodeId")
	@ResponseBody	
	public String getResourceNodeId(@PathVariable String id) {
		return service.getResourceNodeId(id);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "{id}/status")
	@ResponseBody	
	public MonitorTargetStatus getResourceStatus(@PathVariable String id) {
		return service.getResourceStatus(id);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "resourcesStatus")
	@ResponseBody	
	public MonitorTargetStatus[] getResourcesStatus(@RequestBody String [] resourceIds) {
		return service.getResourcesStatus(resourceIds);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody	
	public Page<MonitorResource> getResources(@RequestParam(required = false) Integer pageFirst, 
			@RequestParam(required = false) Integer pageSize, 
			@RequestParam(required = false) String groupId,
			@RequestParam(required = false) String nodeId) {
		PageNo pageNo = PageNo.createByFirst(pageFirst == null ? 0 : pageFirst, pageSize == null ? 100 : pageSize);
		if (groupId != null) 			
			return service.getResourcesByGroupId(pageNo, groupId);
		else if (nodeId != null)
			return new Page<MonitorResource>(service.getResourcesByNodeId(nodeId), pageNo);
		else
			throw new IllegalArgumentException("必须提供参数");
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "ids")
	@ResponseBody	
	public Page<MonitorResource> getResourcesByNodeIds(@RequestBody String ids) {
		return service.getResourcesByNodeIds(ids);
	}
}
