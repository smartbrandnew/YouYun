package uyun.bat.agent.api.service;

import uyun.bat.agent.api.entity.PageAgent;

import java.util.List;

public interface AgentService {
    /**
     * 获取租户agent tags列表
     * @param tenantId
     * @return
     */
    List<String> queryTags(String tenantId,String source);

    /**
     * 根据标签查询agent列表
     * @param tenantId
     * @param tags
     * @param searchValue
     * @param pageNo
     * @param pageSize
     * @return
     */
    PageAgent queryByTags(String tenantId, String[] tags,String source, String searchValue, int pageNo, int pageSize);

    /**
     * 根据id删除agent
     * @param id
     * @return
     */
    boolean delete(String id);
}
