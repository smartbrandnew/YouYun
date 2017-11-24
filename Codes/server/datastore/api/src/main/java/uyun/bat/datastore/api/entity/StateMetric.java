package uyun.bat.datastore.api.entity;

import java.io.Serializable;
import java.util.*;

public class StateMetric implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String value;
    private long timestamp;
    private Map<String, List<String>> tags;
    private static final String TENANT_ID = "tenantId";
    private static final String RESOURCE_ID = "resourceId";

    public StateMetric(String name, String value, long timestamp) {
        this.name = name;
        this.value=value;
        this.timestamp=timestamp;
        this.tags=new HashMap<>();
    }

    public StateMetric addTenantId(String tenantId) {
        if (this.tags.get(TENANT_ID) == null) {
            this.tags.put(TENANT_ID, new ArrayList<String>(Arrays.asList(tenantId)));
        } else {
            this.tags.get(TENANT_ID).clear();
            this.tags.get(TENANT_ID).add(tenantId);
        }
        return this;
    }

    public StateMetric addResourceId(String resourceId) {
        if (this.tags.get(RESOURCE_ID) == null) {
            this.tags.put(RESOURCE_ID, new ArrayList<String>(Arrays.asList(resourceId)));
        } else {
            this.tags.get(RESOURCE_ID).clear();
            this.tags.get(RESOURCE_ID).add(resourceId);
        }
        return this;
    }

    public StateMetric addTag(String key, String value) {
        if (this.tags.get(key) == null) {
            this.tags.put(key, new ArrayList<String>(Arrays.asList(value)));
        } else {
            if (value == null)
                value = "";
            this.tags.get(key).add(value);
        }
        return this;
    }

    public String getResourceId() {
        if (this.tags.get(RESOURCE_ID) != null && this.tags.get(RESOURCE_ID).size() > 0)
            return this.tags.get(RESOURCE_ID).get(0);
        return null;
    }

    public String getTenantId() {
        if (this.tags.get(TENANT_ID) != null && this.tags.get(TENANT_ID).size() > 0)
            return this.tags.get(TENANT_ID).get(0);
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, List<String>> getTags() {
        return tags;
    }

    public void setTags(Map<String, List<String>> tags) {
        this.tags = tags;
    }
}
