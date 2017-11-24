package com.broada.carrier.monitor.server.impl.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.base.restful.BaseTaskController;
import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.server.api.client.restful.entity.SaveTaskRequest;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.service.ServerTaskService;

@Controller
@RequestMapping("/v1/monitor/tasks")
public class ServerTaskController extends BaseTaskController {
	@Autowired
	private ServerTaskService service;
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody	
	public Page<MonitorTask> getTasks(
			@RequestParam(value = "pageFirst", required = false) Integer pageFirst, 
			@RequestParam(value = "pageSize", required = false) Integer pageSize, 
			@RequestParam(value = "probeId", required = false) Integer probeId,
			@RequestParam(value = "nodeId", required = false) String nodeId,
			@RequestParam(value = "resourceId", required = false) String resourceId,
			@RequestParam(value = "policyCode", required = false) String policyCode,
			@RequestParam(value = "currentDomain", required = false) Boolean currentDomain) {
		if (currentDomain == null)
			currentDomain = false;
		if (probeId != null) {
			PageNo pageNo = PageNo.createByFirst(pageFirst == null ? 0 : pageFirst, pageSize == null ? 100 : pageSize);
			return service.getTasksByProbeId(pageNo, probeId, currentDomain);
		} else if (nodeId != null) {
			return new Page<MonitorTask>(service.getTasksByNodeId(nodeId));
		} else if (resourceId != null) {
			return new Page<MonitorTask>(service.getTasksByResourceId(resourceId));			
		} else if (policyCode != null) {
			PageNo pageNo = PageNo.createByFirst(pageFirst == null ? 0 : pageFirst, pageSize == null ? 100 : pageSize);
			return service.getTasksByPolicyCode(pageNo, policyCode);			
		} else {
			PageNo pageNo = PageNo.createByFirst(pageFirst == null ? 0 : pageFirst, pageSize == null ? 100 : pageSize);
			return service.getTasks(pageNo,currentDomain);
		}			
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "nodeIds")
	@ResponseBody	
	public MonitorTask[] getTasksByNodeIds(@RequestBody String[] nodeIds) {		
		return service.getTasksByNodeIds(nodeIds);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "resourceIds")
	@ResponseBody	
	public MonitorTask[] getTasksByResourceIds(@RequestBody String[] resourceIds) {		
		return service.getTasksByResourceIds(resourceIds);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody	
	public String saveTask(@RequestBody SaveTaskRequest request) {		
		if (request.isWithInstances())
			return service.saveTask(request.getTask(), request.getInstances());
		else
			return service.saveTask(request.getTask());
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/results")
	@ResponseBody
	public void commitResults(@PathVariable("id") int id, @RequestParam("probeCode") String probeCode, @RequestBody MonitorResult[] result) {
		service.commitResults(probeCode, result);
	}	
	
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/enable")
	@ResponseBody
	public void enableTask(@PathVariable("id") String id) {
		service.setTaskEnabled(id, true);
	}	
	
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/disable")
	@ResponseBody
	public void disableTask(@PathVariable("id") String id) {
		service.setTaskEnabled(id, false);
	}	
	
	@RequestMapping(method = RequestMethod.POST, value = "/records")
	@ResponseBody
	public MonitorRecord[] getRecords(@RequestBody String taskIds) {
		return service.getRecords(taskIds);
	}	
}
