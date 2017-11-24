package uyun.bat.event.api.entity;

import java.io.Serializable;

public class EventTag implements Serializable {
    /**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
		private String id;
    private String tagk;
    private String tagv;
    private String tenantId;
    private String objectId;

    public EventTag() {
    }

    public EventTag(String id,String objectId, String tenantId, String tagk, String tagv) {
        this.id = id;
        this.objectId = objectId;
        this.tenantId = tenantId;
        this.tagk = tagk;
        this.tagv = tagv;
    }

    public EventTag(String tenantId, String tagk, String tagv) {
        this.tenantId = tenantId;
        this.tagk = tagk;
        this.tagv = tagv;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTagk() {
        return tagk;
    }

    public void setTagk(String tagk) {
        this.tagk = tagk;
    }

    public String getTagv() {
        return tagv;
    }

    public void setTagv(String tagv) {
        this.tagv = tagv;
    }
}
