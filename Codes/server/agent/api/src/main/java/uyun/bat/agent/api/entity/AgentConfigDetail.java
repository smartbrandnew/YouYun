package uyun.bat.agent.api.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lilm on 17-5-11.
 */
public class AgentConfigDetail {

    private String activeTime;
    private String status;
    private int methodCount = 0;
    private int totalCount = 0;

    // 分页相关参数
    private int current;
    private int pageSize;
    private int total;

    private List<Map<String, Object>> methods = new ArrayList<Map<String, Object>>();

    private List<AgentConfigDetailHost> hosts = new ArrayList<AgentConfigDetailHost>();

    public String getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(String activeTime) {
        this.activeTime = activeTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMethodCount() {
        return methodCount;
    }

    public void setMethodCount(int methodCount) {
        this.methodCount = methodCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Map<String, Object>> getMethods() {
        return methods;
    }

    public void setMethods(List<Map<String, Object>> methods) {
        this.methods = methods;
    }

    public List<AgentConfigDetailHost> getHosts() {
        return hosts;
    }

    public void setHosts(List<AgentConfigDetailHost> hosts) {
        this.hosts = hosts;
    }
}
