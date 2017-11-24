package uyun.bat.monitor.core.entity;

import uyun.bat.monitor.core.mq.MQData;

import java.util.ArrayList;
import java.util.List;

public class StateMetricData implements MQData {
    private String name;
    private String tenantId;
    private List<TagEntry> tags;

    public StateMetricData() {
    }

    public StateMetricData(String name, String tenantId) {
        this.name = name;
        this.tenantId = tenantId;
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

    public List<TagEntry> getTags() {
        return tags;
    }

    public void setTags(List<TagEntry> tags) {
        this.tags = tags;
    }


    public void addTag(String key, String value) {
        if (tags == null)
            tags = new ArrayList<TagEntry>();
        tags.add(new TagEntry(key, value));
    }
}
