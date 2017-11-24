package com.broada.carrier.monitor.server.impl.restful;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.base.restful.BaseNodeController;
import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;
import com.broada.carrier.monitor.server.api.service.ServerNodeService;

@Controller
@RequestMapping("/v1/monitor/nodes")
public class ServerNodeController extends BaseNodeController {
	@Autowired
	private ServerNodeService service;
	
	@RequestMapping(method = RequestMethod.GET, value = "{id}/probeId")
	@ResponseBody	
	public int getNodeProbeId(@PathVariable("id") String id) {
		return service.getNodeProbeId(id);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "{id}/status")
	@ResponseBody	
	public MonitorTargetStatus getNodeStatus(@PathVariable("id") String id) {
		return service.getNodeStatus(id);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "nodesStatus")
	@ResponseBody	
	public MonitorTargetStatus[] getNodesStatus(@RequestBody String [] nodeIds) {
		return service.getNodesStatus(nodeIds);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody	
	public Page<MonitorNode> getNodes(@RequestParam(value = "pageFirst", required = false) Integer pageFirst, 
			@RequestParam(value = "pageSize", required = false) Integer pageSize, 
			@RequestParam(value = "groupId", required = false) String groupId,
			@RequestParam(value = "probeId", required = false) Integer probeId,
			@RequestParam(value = "ip", required = false) String ip,
			@RequestParam(value = "currentDomain", required = false) Boolean currentDomain) {
		if (currentDomain == null)
			currentDomain = false;
		PageNo pageNo = PageNo.createByFirst(pageFirst == null ? 0 : pageFirst, pageSize == null ? 100 : pageSize);
		if (groupId != null) 			
			return service.getNodesByGroupId(pageNo, groupId);
		else if (probeId != null)
			return service.getNodesByProbeId(pageNo, probeId, currentDomain);
		else if (ip != null)
			return service.getNodesByIp(pageNo, ip);
		else
			return service.getNodes(currentDomain);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "ids")
	@ResponseBody	
	public MonitorNode[] getNodes(@RequestBody List<String> ids, @RequestParam("currentDomain") Boolean currentDomain) {
		if (currentDomain == null)
			currentDomain = false;
		return service.getNodes(ids, currentDomain);
	}
}
