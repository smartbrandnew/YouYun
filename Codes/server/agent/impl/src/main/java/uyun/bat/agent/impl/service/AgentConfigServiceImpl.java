package uyun.bat.agent.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.agent.api.entity.FileCharSet;
import uyun.bat.agent.api.entity.FileList;
import uyun.bat.agent.api.entity.HostCheckResult;
import uyun.bat.agent.api.entity.HostCheckStatus;
import uyun.bat.agent.api.entity.YamlBean;
import uyun.bat.agent.api.entity.YamlFile;
import uyun.bat.agent.api.entity.YamlHost;
import uyun.bat.agent.api.service.AgentConfigService;
import uyun.bat.agent.impl.autosync.common.Md5Util;
import uyun.bat.agent.impl.autosync.entity.TenantConstants;
import uyun.bat.agent.impl.logic.LogicManager;
import uyun.bat.agent.impl.logic.YamlFileLogic;
import uyun.bat.agent.impl.util.YamlUtil;
import uyun.bat.common.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service(protocol = "rest-agent")
@Path("v2/agent/config")
public class AgentConfigServiceImpl implements AgentConfigService {
	private static final Logger logger = LoggerFactory.getLogger(AgentConfigServiceImpl.class);
	private YamlFileLogic yamlFileLogic = LogicManager.getInstance().getYamlFileLogic();

	@GET
	@Path("list")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public List<FileList> getFileListById(@Context HttpServletRequest request, @QueryParam("id") String agentId,
										  @QueryParam("source") String source) {
		String tenantId = (String)request.getAttribute(TenantConstants.TENANT_ID);
		List<YamlFile> yamlFiles = null;
		try {
			yamlFiles = yamlFileLogic.getYamlFileListByAgentId(tenantId, agentId, source);
		} catch (Exception e) {
			logger.warn("get agent config file failed: ", e);
		}
		if (yamlFiles == null || yamlFiles.isEmpty()) {
			return Collections.emptyList();
		}
		List<FileList> fileLists = new ArrayList<FileList>();
		if ("agent".equals(source)) {
			fileLists.addAll(yamlFiles.stream()
					.map(yamlFile -> new FileList(yamlFile.getFileName(), yamlFile.getMd5(), yamlFile.getEnabled(), false, new HashSet<>()))
					.collect(Collectors.toList()));
		} else {
			fileLists.addAll(yamlFiles.stream()
					.filter(yamlFile -> StringUtils.isNotBlank(yamlFile.getContent()))
					.map(yamlFile -> new FileList(yamlFile.getFileName(), yamlFile.getMd5(), yamlFile.getEnabled(), false, generateCheckIpList(yamlFile)))
					.collect(Collectors.toList()));
		}
		// 对于已删除配置,打上删除标志, 并只通知一次.
		for (Map.Entry<String, String> entry : YamlFileLogic.deletedCache.entrySet()) {
			if (entry.getKey().equals(tenantId + agentId + entry.getValue() + source)) {
				boolean isExist = false;
				for (FileList fileList : fileLists) {
					String fileName = fileList.getFileName();
					if (fileName != null && fileName.equals(entry.getValue())) {
						fileList.setDeleted(true);
						fileList.setEnabled(false);
						isExist = true;
						break;
					}
				}
				if (!isExist) {
					fileLists.add(new FileList(entry.getValue(), "md5", false, true, new HashSet<String>()));
				}
				YamlFileLogic.deletedCache.remove(entry.getKey());
			}
		}
		return fileLists;
	}

	/**
	 * 生成需要agent端去验证的IP列表
	 * @param yamlFile
	 * @return
	 */
	private Set<String> generateCheckIpList(YamlFile yamlFile) {
		String content = yamlFile.getContent();
		YamlBean yamlBean = YamlUtil.customYamlLoad(content);
		if (yamlBean != null) {
			List<YamlHost> hosts = yamlBean.getHosts();
			if (hosts != null && hosts.size() > 0) {
				return hosts.stream()
						.filter(host -> HostCheckStatus.checkVerifying(host.getCheckStatus()))
						.map(YamlHost::getIp)
						.collect(Collectors.toSet());
			}
		}
		return new HashSet<String>();
	}

	@GET
	@Path("file")
	@Produces(MediaType.TEXT_PLAIN)
	@Override
	public String getFileContentByNameAndId(@Context HttpServletRequest request, @QueryParam("id") String agentId,
											@QueryParam("name") String name, @QueryParam("source") String source) {
		String yamlContent = null;
		try {
			String tenantId = (String) request.getAttribute(TenantConstants.TENANT_ID);
			yamlContent = yamlFileLogic.getYamlContent(tenantId, agentId, name, source);
		} catch (Exception e) {
			logger.warn("getting config file " + name + "failed: ", e);
		}
		if (yamlContent == null) {
			logger.warn("cannot find the agent config file, wrong argument!");
		}
		return yamlContent;
	}

	@POST
	@Path("update/status")
	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public void updateConfigCheckStatus(@Context HttpServletRequest request, List<HostCheckResult> hostCheckList) {
		if (hostCheckList == null || hostCheckList.size() <= 0) {
			return;
		}
		for (HostCheckResult result : hostCheckList) {
			if (result == null || (result.getCheckedList() == null && result.getFailedList() == null)) {
				continue;
			}
			String tenantId = (String) request.getAttribute(TenantConstants.TENANT_ID);
			YamlFile yamlFile = yamlFileLogic.getYamlFileByNameAndAgentId(tenantId, result.getId(), result.getName(), result.getSource());
			if (yamlFile == null || yamlFile.getContent() == null) {
				continue;
			}
			String yamlContent = yamlFile.getContent();
			YamlBean yamlBean = YamlUtil.customYamlLoad(yamlContent);
			if (yamlBean != null) {
				List<YamlHost> hosts = yamlBean.getHosts();
				if (hosts == null || hosts.size() <= 0) {
					continue;
				}
				updateYamlHostStatus(hosts, result.getCheckedList(), true);
				updateYamlHostStatus(hosts, result.getFailedList(), false);
				try {
					yamlContent = YamlUtil.customYamlDump(yamlBean);
				} catch (Exception e) {
					logger.warn("yaml dump failed: {}", yamlFile.getContent(), e);
				}
				if (yamlContent != null) {
					yamlFile.setContent(yamlContent);
					saveYamlFile(yamlFile, tenantId);
				}
			}
		}
	}

	// 更新主机验证状态
	private void updateYamlHostStatus(List<YamlHost> hosts, List<String> ipList, boolean isSuccess) {
		if (ipList != null && ipList.size() > 0) {
			for (String ip : ipList) {
				for (YamlHost host : hosts) {
					if (ip != null && ip.equals(host.getIp())) {
						if (isSuccess) {
							host.setCheckStatus(HostCheckStatus.CHECKED.getCode());
						} else {
							host.setCheckStatus(HostCheckStatus.FAILED.getCode());
						}
					}
				}
			}
		}
	}

	// 保存配置文件
	private void saveYamlFile(YamlFile yamlFile, String tenantId) {
		yamlFile.setModified(new Date());
		yamlFile.setTenantId(tenantId);
		String md5 = Md5Util.digest(yamlFile);
		yamlFile.setMd5(md5);
		int size = 0;
		String content = yamlFile.getContent();
		if (StringUtils.isNotNullAndTrimBlank(content)) {
			//保存数据时去除最后的空行
			while(content.endsWith("\n")){
				content=content.substring(0, content.lastIndexOf("\n"));
			}
			yamlFile.setContent(content);
			size = content.getBytes(FileCharSet.DEFAULT_CHARSET).length;
		}
		yamlFile.setSize(size);
		yamlFileLogic.save(yamlFile);
	}
}
