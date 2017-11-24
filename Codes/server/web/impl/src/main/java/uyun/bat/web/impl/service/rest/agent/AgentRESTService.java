package uyun.bat.web.impl.service.rest.agent;

import com.alibaba.dubbo.config.annotation.Service;
import uyun.bat.agent.api.entity.Agent;
import uyun.bat.agent.api.entity.PageAgent;
import uyun.bat.web.api.agent.entity.AgentDownloadInfo;
import uyun.bat.web.api.agent.entity.AgentVO;
import uyun.bat.web.api.agent.entity.MineAgent;
import uyun.bat.web.api.agent.service.AgentWebService;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;
import uyun.bird.tenant.api.entity.Tenant;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service(protocol = "rest")
@Path("v2/agents")
public class AgentRESTService implements AgentWebService {

	@GET
	@Path("getInstallCmd")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public AgentDownloadInfo getInstallCmd(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("os") String os) {
		 
		// 其实CentOS也是RedHat,Fedora是RedHat的开源社区版
		Tenant t = ServiceManager.getInstance().getTenantService().view(tenantId);
		if (t == null)
			throw new IllegalArgumentException("The current tenant does not exist!");

		String apiKey = t.getApiKeys() != null && t.getApiKeys().size() > 0 ? t.getApiKeys().get(0).getKey() : "";

		return AgentInfoManager.getInstance().generateAgentDownloadInfo(apiKey, os);
	}

	@GET
	@Path("tags")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<String> queryTags(@HeaderParam(TenantConstants.COOKIE_TENANT_ID)  String tenantId
			, @QueryParam("source") String source) {
		 
		return ServiceManager.getInstance().getAgentService().queryTags(tenantId,source);
	}

	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public MineAgent queryByTags(@HeaderParam(TenantConstants.COOKIE_TENANT_ID)  String tenantId
			, @QueryParam("tags") String[] tags
			, @QueryParam("source") String source
			, @QueryParam("searchValue") String searchValue
			, @QueryParam("current") int pageNo
			, @QueryParam("pageSize") int pageSize) {
		//测试使用
		/*PageAgent pageAgent=new PageAgent();
		List<Agent> as = new ArrayList<Agent>();
		Agent a = new Agent();
		a.setId("FE3265B3A45258D38320FC3965466615");
		List<String> list = new ArrayList<String>();
		list.add("agent_metrics.yaml.default");
		list.add("apache.yaml");
		list.add("disk.yaml.default");
		list.add("mysql.yaml");
		list.add("network.yaml.default");
		list.add("ntp.yaml.default");
		list.add("updater.yaml");
		a.setApps(list);
		as.add(a);
		pageAgent.setAgents(as);*/
		PageAgent pageAgent = ServiceManager.getInstance().getAgentService().queryByTags(tenantId, tags, source, searchValue, pageNo, pageSize);
		List<AgentVO> agentVOs = new ArrayList<>();
		for (Agent agent : pageAgent.getAgents()) {
			List<String> disabledApps = ServiceManager.getInstance().getYamlFileService().getDisabledYamlNames(agent.getId());
			List<String> enabledApps = ServiceManager.getInstance().getYamlFileService().getEnabledYamlNames(agent.getId());
			Set<String> apps = new HashSet<String>();
			if (agent.getApps() != null) {
				apps.addAll(agent.getApps());
			}
			// 移除.yaml或者.yaml.example后缀
			apps = apps.stream()
					.map(app -> app.split(".yaml")[0])
					.collect(Collectors.toSet());
			if (apps.size() > 0 && disabledApps != null && disabledApps.size() > 0) {
				apps.removeAll(disabledApps);
			}
			if (enabledApps != null && enabledApps.size() > 0) {
				apps.addAll(enabledApps);
			}
			List<String> appList = new ArrayList<String>(apps);
			Collections.sort(appList);
			AgentVO agentVO = new AgentVO(agent.getId(), agent.getHostname(), agent.getIp(), agent.getTags(), appList, agent.getSource().getName());
			agentVOs.add(agentVO);
		}
		MineAgent mineAgent = new MineAgent();
		mineAgent.setCount(pageAgent.getCount());
		mineAgent.setAgents(agentVOs);
		return mineAgent;
	}

	@POST
	@Path("delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public boolean delete(@QueryParam("id") String id) {
		return ServiceManager.getInstance().getAgentService().delete(id);
	}
}
