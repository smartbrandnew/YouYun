package uyun.bat.agent.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import uyun.bat.agent.api.entity.Agent;
import uyun.bat.agent.api.entity.AgentStatus;
import uyun.bat.agent.api.service.AgentInfoService;
import uyun.bat.agent.impl.logic.AgentLogic;
import uyun.bat.agent.impl.logic.LogicManager;
import uyun.bat.common.constants.RestConstants;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Date;

@Service(protocol = "rest-agent")
@Path("v2/agent/info")
public class AgentInfoServiceImpl implements AgentInfoService {
	private AgentLogic agentLogic = LogicManager.getInstance().getAgentLogic();

	private static final String IP_UNKNOWN = "unknown";

	@POST
	@Path("intake")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public boolean saveAgent(Agent agent, @Context HttpServletRequest request) {
		checkAgentInfo(agent);
		String tenantId = (String) request.getAttribute(RestConstants.TENANT_ID);
		agent.setTenantId(tenantId);
		agent.setOnlineStatus(AgentStatus.online.getName());
		agent.setModified(new Date());
		return agentLogic.saveAgent(agent);
	}

	private void checkAgentInfo(Agent agent) {
		if (agent != null) {
			String hostname = agent.getHostname();
			if (hostname != null && hostname.length() > 64) {
				throw new IllegalArgumentException("hostname length cannot larger than 64!");
			}
			// BAT-1396 agent采集不到IP, 设置为ip为unknown
			// agent_list表有ip非空验证, 避免sql错误
			if (agent.getIp() == null) {
				agent.setIp(IP_UNKNOWN);
			}
		}
	}

}
