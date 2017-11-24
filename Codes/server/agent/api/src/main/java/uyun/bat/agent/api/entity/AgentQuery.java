package uyun.bat.agent.api.entity;

import java.util.List;

public class AgentQuery {

    private String tenantId;
    private int pageNo;
    private int pageSize;
    private List<AgentTag> agentTags;
    private String searchValue;
    private boolean others;
    private String source;

    public AgentQuery() {
    }

    public AgentQuery(String tenantId, int pageNo, int pageSize,String searchValue,String source) {
        this.tenantId = tenantId;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.searchValue=searchValue;
        this.source=source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isOthers() {
        return others;
    }

    public void setOthers(boolean others) {
        this.others = others;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<AgentTag> getAgentTags() {
        return agentTags;
    }

    public void setAgentTags(List<AgentTag> agentTags) {
        this.agentTags = agentTags;
    }
}
