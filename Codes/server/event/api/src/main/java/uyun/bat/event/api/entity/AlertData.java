package uyun.bat.event.api.entity;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * 用于Alert数据对接
 */
public class AlertData {
    /**
     * 告警名称: 英文指标-中文指标。 如: system.cpu.idle-CPU可用率<br>
     */
    private String name;

    /**
     * 告警状态
     */
    private int severity;

    /**
     * 告警描述
     */
    private String description;

    /**
     * 告警发生时间
     */
    @JsonProperty("occur_time")
    private Date occurTime;

    /**
     * 资源名称: Resource Host
     */
    @JsonProperty("entity_name")
    private String entityName;

    /**
     * 资源地址: Resource Ip
     */
    @JsonProperty("entity_addr")
    private String entityAddr;

    /**
     * source, entity_name/entity_addr, alert_name
     * entity为空时使用entity_addr
     */
    @JsonProperty("merge_key")
    private String mergeKey;

    /**
     * 扩展属性
     */
    private List<String> properties;

    public AlertData() {
    }

    public AlertData(String name, int severity, String description, Date occurTime, String entityName, String entityAddr, List<String> properties) {
        this.name = name;
        this.severity = severity;
        this.description = description;
        this.occurTime = occurTime;
        this.entityName = entityName;
        this.entityAddr = entityAddr;
        this.mergeKey = generateMergeKey();
        this.properties = properties;
    }

    private String generateMergeKey() {
        return String.join(",",  "app_key", "entity_name", "name", "entity_addr");
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(Date occurTime) {
        this.occurTime = occurTime;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityAddr() {
        return entityAddr;
    }

    public void setEntityAddr(String entityAddr) {
        this.entityAddr = entityAddr;
    }

    public String getMergeKey() {
        return mergeKey;
    }

    public void setMergeKey(String mergeKey) {
        this.mergeKey = mergeKey;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlertData alertData = (AlertData) o;

        if (severity != alertData.severity) return false;
        if (name != null ? !name.equals(alertData.name) : alertData.name != null) return false;
        if (description != null ? !description.equals(alertData.description) : alertData.description != null)
            return false;
        if (occurTime != null ? !occurTime.equals(alertData.occurTime) : alertData.occurTime != null) return false;
        if (entityName != null ? !entityName.equals(alertData.entityName) : alertData.entityName != null) return false;
        if (entityAddr != null ? !entityAddr.equals(alertData.entityAddr) : alertData.entityAddr != null) return false;
        if (mergeKey != null ? !mergeKey.equals(alertData.mergeKey) : alertData.mergeKey != null) return false;
        return properties != null ? properties.equals(alertData.properties) : alertData.properties == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + severity;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (occurTime != null ? occurTime.hashCode() : 0);
        result = 31 * result + (entityName != null ? entityName.hashCode() : 0);
        result = 31 * result + (entityAddr != null ? entityAddr.hashCode() : 0);
        result = 31 * result + (mergeKey != null ? mergeKey.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        return result;
    }
}


