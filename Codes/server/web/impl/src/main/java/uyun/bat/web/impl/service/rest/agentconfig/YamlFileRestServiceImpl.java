package uyun.bat.web.impl.service.rest.agentconfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import uyun.bat.agent.api.entity.AgentConfigDetail;
import uyun.bat.agent.api.entity.YamlFile;
import uyun.bat.web.api.agentconfig.entity.ConfigCheck;
import uyun.bat.web.api.agentconfig.entity.ConfigHostUpdate;
import uyun.bat.web.api.agentconfig.entity.ConfigMethodUpdate;
import uyun.bat.web.api.agentconfig.service.YamlFileService;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;
 

import com.alibaba.dubbo.config.annotation.Service;

// 提供给web端的配置api

@Service(protocol = "rest")
@Path("v2/agent_config/yaml")
public class YamlFileRestServiceImpl implements YamlFileService {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("upload")
	public String upload(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, List<YamlFile> yamlFiles) {

		String result = "配置文件上传成功";
		//测试使用
		//tenantId=uyun.bat.common.proxy.tenant.TenantConstants.TENANT_ID;
		 
		boolean sign = ServiceManager.getInstance().getYamlFileService().upload(tenantId, yamlFiles);
		if (!sign)
			throw new RuntimeException("upload failed");
		return result;
	}

	@GET
	@Path("download")
	public String getYamlContent(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
								 @QueryParam("agentId") String agentId, @QueryParam("fileName") String fileName,
								 @QueryParam("source") String source) {
		return ServiceManager.getInstance().getYamlFileService().getYamlContent(tenantId, agentId, fileName, source);
	}

	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<String> getAllYamlName(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
									   @QueryParam("source") String source) {
		return ServiceManager.getInstance().getYamlFileService().getAllYamlName(tenantId, source);
	}

	@GET
	@Path("delete")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public void delete(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
					   @QueryParam("id") String id, @QueryParam("pluginName") String pluginName,
					   @QueryParam("source") String source) {
		ServiceManager.getInstance().getYamlFileService().deleteYaml(tenantId, id, pluginName, source);
	}
	
	@GET
	@Path("disable")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public void disable(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
						@QueryParam("id") String id, @QueryParam("pluginName") String pluginName,
						@QueryParam("source") String source) {
		ServiceManager.getInstance().getYamlFileService().updateEnabled(tenantId, id, pluginName, source, false);
	}
	
	@GET
	@Path("enable")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public void enable(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
					   @QueryParam("id") String id, @QueryParam("pluginName") String pluginName,
					   @QueryParam("source") String source) {
		ServiceManager.getInstance().getYamlFileService().updateEnabled(tenantId, id, pluginName, source, true);
	}

/************************* 新版 agent/agentless 配置升级(暂时只有 agentless 会升级) *******************************/

	@GET
	@Path("upgrade/list")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Map<String, Collection<String>> AllPluginApp(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
														@QueryParam("source") String source,
														@QueryParam("id") String id) {
		onlySupportAgentless(source);
		return ServiceManager.getInstance().getYamlFileService().getAllPluginApp(tenantId, source, id);
	}

	@GET
	@Path("upgrade/detail")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public AgentConfigDetail pluginConfigDetail(@HeaderParam(TenantConstants.COOKIE_TENANT_ID)String tenantId,
												@QueryParam("id") String id,
												@QueryParam("pluginName") String pluginName,
												@QueryParam("source") String source,
												@QueryParam("current") @DefaultValue("1") Integer current,
												@QueryParam("pageSize") @DefaultValue("100") Integer pageSize,
												@QueryParam("checkStatus") String checkStatus,
												@QueryParam("filter") String filter) {
		onlySupportAgentless(source);
		return ServiceManager.getInstance().getYamlFileService()
				.pluginConfigDetail(tenantId, id, pluginName, source, current, pageSize, checkStatus, filter);
	}


	@POST
	@Path("update/hosts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public ConfigHostUpdate updateHostConfig(@HeaderParam(TenantConstants.COOKIE_TENANT_ID)String tenantId,
											 ConfigHostUpdate hostUpdate) {
		if (hostUpdate == null || hostUpdate.getId() == null
				|| hostUpdate.getPluginName() == null || hostUpdate.getSource() == null) {
			throw new IllegalArgumentException("Wrong host update Arguments");
		}
		onlySupportAgentless(hostUpdate.getSource());
		boolean flag = ServiceManager.getInstance().getYamlFileService()
				.updateHostConfig(tenantId, hostUpdate.getId(), hostUpdate.getPluginName(), hostUpdate.getCheckNow(), hostUpdate.getSource(),
						hostUpdate.getNewHosts(), hostUpdate.getRemoveHosts(), hostUpdate.getUpdateHosts());
		if (!flag) {
			throw new IllegalArgumentException("update failed!");
		}
		return hostUpdate;
	}

	@POST
	@Path("update/methods")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public ConfigMethodUpdate updateMethodConfig(@HeaderParam(TenantConstants.COOKIE_TENANT_ID)String tenantId,
												 ConfigMethodUpdate methodUpdate) {
		if (methodUpdate == null || methodUpdate.getId() == null
				|| methodUpdate.getPluginName() == null || methodUpdate.getSource() == null) {
			throw new IllegalArgumentException("Wrong method update Arguments");
		}
		onlySupportAgentless(methodUpdate.getSource());
		boolean flag = ServiceManager.getInstance().getYamlFileService()
				.updateMethodConfig(tenantId, methodUpdate.getId(), methodUpdate.getPluginName(), methodUpdate.getSource(),
						methodUpdate.getNewMethods(), methodUpdate.getRemoveNameList(), methodUpdate.getUpdateMethod());
		if (!flag) {
			throw new IllegalArgumentException("update failed!");
		}
		return methodUpdate;
	}

	@POST
	@Path("check")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<String> checkPluginConfig(@HeaderParam(TenantConstants.COOKIE_TENANT_ID)String tenantId,
										  ConfigCheck configCheck) {
		if (configCheck == null || configCheck.getId() == null
				|| configCheck.getPluginName() == null || configCheck.getSource() == null) {
			return new ArrayList<String>();
		}
		List<String> ipList = configCheck.getIpList();
		if (ipList == null || ipList.size() <= 0) {
			return ipList;
		}
		boolean flag = ServiceManager.getInstance().getYamlFileService()
				.checkPluginConfig(tenantId, configCheck.getId(), configCheck.getPluginName(), configCheck.getSource(), ipList);
		if (!flag) {
			throw new IllegalArgumentException("yaml load failed, please check monitor/logs/bat-agent*.log");
		}
		return ipList;
	}

	// TODO 目前只支持agentless采集配置，拦截其他类型的请求
	private void onlySupportAgentless(String source) {
		if (source == null) {
			throw new IllegalArgumentException("argument source is null");
		}
		if (!"agentless".equals(source)) {
			throw new IllegalArgumentException("only support agentless for now");
		}
	}
}
