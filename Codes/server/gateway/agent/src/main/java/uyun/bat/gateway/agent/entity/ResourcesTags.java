package uyun.bat.gateway.agent.entity;

import java.util.List;

/**
 * Created by lilm on 17-3-23.
 */
public class ResourcesTags {

    List<String> ipaddrs;

    List<String> tags;

    String tenantId;

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getIpaddrs() {
        return ipaddrs;
    }

    public void setIpaddrs(List<String> ipaddrs) {
        this.ipaddrs = ipaddrs;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
