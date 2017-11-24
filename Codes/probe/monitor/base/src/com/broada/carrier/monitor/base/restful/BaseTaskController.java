package com.broada.carrier.monitor.base.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.common.util.Base64Util;
import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.CollectTaskSign;
import com.broada.carrier.monitor.server.api.entity.ExecuteParams;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.TestParams;
import com.broada.carrier.monitor.server.api.service.BaseTaskService;

public class BaseTaskController extends BaseController {
	@Autowired
	private BaseTaskService service;

	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	@ResponseBody
	public MonitorTask getTask(@PathVariable("id") String id) {
		return service.getTask(id);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{id}/delete")
	@ResponseBody
	public void deleteTask(@PathVariable("id") String id) {
		service.deleteTask(id);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{id}/execute")
	@ResponseBody
	public MonitorResult executeTask(@PathVariable("id") String id, @RequestBody ExecuteParams params) {
		return service.executeTask(id, params);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/0/collect")
	@ResponseBody
	public String collectTask(@RequestBody CollectParams params) {
		Object obj = service.collectTask(params);
		return Base64Util.encodeObject(obj);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/0/test")
	@ResponseBody
	public MonitorResult testTask(@RequestBody TestParams params) {
		return service.testTask(params);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{id}/dispatch")
	@ResponseBody
	public void dispatchTask(@PathVariable("id") String id) {
		service.dispatchTask(id);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}/record")
	@ResponseBody
	public MonitorRecord getRecordByLast(@PathVariable("id") String id) {
		return service.getRecord(id);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}/instances")
	@ResponseBody
	public MonitorInstance[] getInstancesByTaskId(@PathVariable("id") String id) {
		return service.getInstancesByTaskId(id);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{nodeId}/{id}/cancelCollect")
	@ResponseBody
	public void cancelCollect(@PathVariable("nodeId") String nodeId, @PathVariable("id") String id) {
		service.cancelCollect(nodeId, id);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{nodeId}/{id}/getCollectResult")
	@ResponseBody
	public CollectResult getCollectResult(@PathVariable("nodeId") String nodeId, @PathVariable("id") String id) {
		return service.getCollectResult(nodeId, id);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/0/commit")
	@ResponseBody
	public CollectTaskSign commitTask(@RequestBody CollectParams params) {
		return service.commitTask(params);
	}

}
