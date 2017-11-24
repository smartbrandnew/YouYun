package uyun.bat.datastore.api.mq;

import java.io.Serializable;
import java.util.List;

import uyun.bat.common.tag.entity.Tag;

public class StateMetricInfo implements Serializable{
    private String name;
    private String tenantId;
    private List<Tag> tags;

    public StateMetricInfo() {
    }

    public StateMetricInfo(String name, String tenantId, List<Tag> tags) {
        this.name = name;
        this.tenantId = tenantId;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}
