package uyun.bat.agent.api.entity;

public class AgentTag {

    private String id;
    private String tenantId;
    private String key;
    private String value;

    public AgentTag() {
    }

    public AgentTag(String id, String tenantId, String key, String value) {
        this.id = id;
        this.tenantId = tenantId;
        this.key = key;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String tagToString() {
        if (null!=value&&!"".equals(value)){
            return key+":"+value;
        }
        return key;
    }

    @Override
    public String toString() {
        return "AgentTag{" +
                "id='" + id + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
