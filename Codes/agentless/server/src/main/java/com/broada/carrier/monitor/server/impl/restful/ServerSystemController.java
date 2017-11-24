package com.broada.carrier.monitor.server.impl.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.broada.carrier.monitor.base.restful.BaseSystemController;
import com.broada.carrier.monitor.server.api.client.restful.entity.LoginRequest;
import com.broada.carrier.monitor.server.api.service.ServerSystemService;

@Controller
@RequestMapping("/v1/monitor/system")
public class ServerSystemController extends BaseSystemController {
	@Autowired
	private ServerSystemService service;

	public ServerSystemController() {
		super();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/sessions")
	@ResponseBody
	public String login(@RequestBody LoginRequest request) {
		return service.login(request.getUsername(), request.getPassword());
	}

	@RequestMapping(method = RequestMethod.POST, value = "/sessions/{token}/delete")
	@ResponseBody
	public void logout(@PathVariable("token") String token) {
		service.logout(token);
	}

	static class UsedQuota {
		int usedQuota;

		public UsedQuota(int usedQuota) {
			this.usedQuota = usedQuota;
		}

		public boolean isSuccess() {
			return true;
		}

		public String getUsedQuota() {
			return Integer.toString(usedQuota);
		}

	}

	@RequestMapping(method = RequestMethod.GET, value = "/license/user-quota")
	@ResponseBody
	public UsedQuota getLicenseUsedQuota(@RequestParam("moduleId") String moduleId) {
		return new UsedQuota(service.getLicenseUsedQuota(moduleId));
	}
}
