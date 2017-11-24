package com.broada.carrier.monitor.base.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.common.restful.BaseController;
import com.broada.carrier.monitor.common.util.Base64Util;
import com.broada.carrier.monitor.server.api.client.restful.BaseFileClient;
import com.broada.carrier.monitor.server.api.client.restful.entity.OpenFileRequest;
import com.broada.carrier.monitor.server.api.client.restful.entity.SetLastModifiedRequest;
import com.broada.carrier.monitor.server.api.client.restful.entity.WriteFileRequest;
import com.broada.carrier.monitor.server.api.service.BaseFileService;

public class BaseFileController extends BaseController {
	@Autowired
	private BaseFileService service;

	@RequestMapping(method = RequestMethod.POST, value = "/dir")
	@ResponseBody
	public Object getList(@RequestBody String dir) {
		if (dir != null)
			return service.list(BaseFileClient.decodePath(dir));
		else
			throw new IllegalArgumentException("必须提供dir参数");
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/file")
	@ResponseBody
	public Object getFiles(@RequestBody String file) {
		if (file != null)
			return service.get(BaseFileClient.decodePath(file));
		else
			throw new IllegalArgumentException("必须提供file参数");
	}

	@RequestMapping(method = RequestMethod.POST, value = "/0/open")
	@ResponseBody
	public int open(@RequestBody OpenFileRequest request) {
		return service.open(request.getFile(), request.getMode());
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{id}/close")
	@ResponseBody
	public void open(@PathVariable("id") int id) {
		service.close(id);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{id}")
	@ResponseBody
	public void write(@PathVariable("id") int id, @RequestBody WriteFileRequest request) {
		service.write(id, request.retData(), request.getOffset(), request.getLength());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	@ResponseBody
	public String read(@PathVariable("id") int id, @RequestParam("len") int len) {
		byte[] data = service.read(id, len);
		return Base64Util.encode(data);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/0/delete")
	@ResponseBody
	public boolean read(@RequestBody String file) {
		return service.delete(file);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/0/setLastModified")
	@ResponseBody
	public boolean read(@RequestBody SetLastModifiedRequest request) {
		return service.setLastModified(request.getFile(), request.getLastModified());
	}
}
