package com.broada.carrier.monitor.server.impl.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.common.util.Base64Util;
import com.broada.carrier.monitor.server.api.client.restful.entity.ExecuteMethodRequest;
import com.broada.carrier.monitor.server.api.client.restful.entity.UploadFileRequest;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorProbeStatus;
import com.broada.carrier.monitor.server.api.entity.SyncStatus;
import com.broada.carrier.monitor.server.api.service.ServerProbeService;

@Controller
@RequestMapping("/v1/monitor/probes")
public class ServerProbeController extends BaseController {
	@Autowired
	private ServerProbeService service;

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public MonitorProbe[] getProbes(
			@RequestParam(value = "code", required = false) String code,
			@RequestParam(value = "host", required = false) String host, 
			@RequestParam(value = "port", required = false) Integer port) {
		if (code != null)
			return convert(service.getProbeByCode(code));
		else if (host != null)
			return convert(service.getProbeByHostPort(host, port));
		else
			return service.getProbes();
	}
	
	@RequestMapping(method = RequestMethod.GET, value="/{id}")
	@ResponseBody
	public MonitorProbe getProbe(@PathVariable("id") int id) {
		return service.getProbe(id);
	}

	private MonitorProbe[] convert(MonitorProbe probe) {
		if (probe == null)
			return new MonitorProbe[0];
		return new MonitorProbe[] { probe };
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public int saveProbe(@RequestBody MonitorProbe probe) {		
		return service.saveProbe(probe);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{id}/delete")
	@ResponseBody
	public void deleteProbe(@PathVariable("id") int id) {
		service.deleteProbe(id);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/sync")
	@ResponseBody
	public void syncProbe(@PathVariable("id") int id) {
		service.syncProbe(id);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}/sync")
	@ResponseBody
	public SyncStatus getProbeSyncStatus(@PathVariable("id") int id) {
		return service.getProbeSyncStatus(id);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/executeMethod")
	@ResponseBody
	public String executeMethod(@PathVariable("id") int id, @RequestBody ExecuteMethodRequest request) {
		Object result = service.executeMethod(id, request.getClassName(), request.getMethodName(), request.retParams());
		return Base64Util.encodeObject(result);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/exit")
	@ResponseBody
	public void exitProbe(@PathVariable("id") int id, @RequestBody String reason) {
		service.exitProbe(id, reason);		
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{id}/status")
	@ResponseBody
	public Object getProbeStatus(@PathVariable("id") int id) {
		if (id == 0)
			return service.getProbeStatuses();
		else
			return service.getProbeStatus(id);		
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{id}/infos")
	@ResponseBody
	public Object getProbeInfos(@PathVariable("id") int id) {		
		return service.getProbeInfos(id);	
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/status/test")
	@ResponseBody
	public MonitorProbeStatus testProbeState(@PathVariable("id") int id) {
		return service.testProbeStatus(id);		
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{id}/uploadFile")
	@ResponseBody
	public void uploadFile(@PathVariable("id") int id, @RequestBody UploadFileRequest request) {
		service.uploadFile(id, request.getServerFilePath(), request.getProbeFilePath());		
	}
}
