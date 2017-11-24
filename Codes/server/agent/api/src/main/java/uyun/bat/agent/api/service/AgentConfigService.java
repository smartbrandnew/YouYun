package uyun.bat.agent.api.service;

import uyun.bat.agent.api.entity.FileList;
import uyun.bat.agent.api.entity.HostCheckResult;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

public interface AgentConfigService {
    List<FileList> getFileListById(HttpServletRequest request, String agentId, String source);
    String getFileContentByNameAndId(HttpServletRequest request, String agentId, String fileName, String source);

    void updateConfigCheckStatus(HttpServletRequest request, List<HostCheckResult> hostCheckList);
}
