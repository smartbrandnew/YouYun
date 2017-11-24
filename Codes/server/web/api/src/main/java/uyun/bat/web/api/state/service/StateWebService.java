package uyun.bat.web.api.state.service;

import java.util.List;

public interface StateWebService {

    /**
     * 获取用户所有的应用
     * 应用监测器使用
     * @param tenantId
     * @return
     */
    List<String>  getStateApps(String tenantId);

    /**
     * 根据状态指标获取标签集合
     * @param tenantId
     * @param state
     * @return
     */
    List<String> getTagsByState(String tenantId, String state);

}
