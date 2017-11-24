package uyun.bat.web.impl.service.rest.config;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import uyun.bat.common.config.Config;
import uyun.bat.web.api.config.ConfigWebService;
import uyun.bat.web.impl.service.rest.agent.AgentInfoManager;

import com.alibaba.dubbo.config.annotation.Service;

@Service(protocol = "rest")
@Path("v2/config")
public class ConfigRESTService implements ConfigWebService {

	@GET
	@Path("authorization")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> isOpenAutoRecover() {
		Boolean isShowAuto = Config.getInstance().get("auto.push.mode", false);
		Boolean isEE = Config.getInstance().get("enterprise.edition.mode", false);
		Map<String, Object> map = new HashMap<>();
		map.put("monitor_show_auto", isShowAuto);
		map.put("deploy_show_agentless", isEE);
		map.put("os_list", AgentInfoManager.getInstance().getAgentInfoMap().keySet());

		return map;
	}
}
