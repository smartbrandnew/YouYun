package uyun.bat.web.api.agent.service;

import uyun.bat.web.api.agent.entity.AgentDownloadInfo;
import uyun.bat.web.api.agent.entity.MineAgent;

import java.util.List;

public interface AgentWebService {
	AgentDownloadInfo getInstallCmd(String tenantId, String os);

	List<String> queryTags(String tenantId,String source);

	MineAgent queryByTags(String tenantId, String[] tags, String source,String searchValue, int pageNo, int pageSize);

	boolean delete(String id);
}
