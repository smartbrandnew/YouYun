package uyun.bat.event.api.mq;

import uyun.bat.event.api.entity.Tag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventInfo implements Serializable {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private String title;
    private String content;
    private String tenantId;
    private List<Tag> tags;
    private String resId;
    private String identity;
    private Date occurTime;
    private Short serverity;

    public EventInfo() {
        super();
    }

    public EventInfo(String title, String content, String tenantId,String resId,String identity,Date occurTime,Short serverity) {
        this.title = title;
        this.content = content;
        this.tenantId = tenantId;
        this.resId=resId;
        this.identity=identity;
        this.occurTime=occurTime;
        this.serverity=serverity;
    }

    public Short getServerity() {
        return serverity;
    }

    public void setServerity(Short serverity) {
        this.serverity = serverity;
    }

    public Date getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(Date occurTime) {
        this.occurTime = occurTime;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public void addTag(String key, String value) {
        if(tags == null)
            tags = new ArrayList<Tag>();
        tags.add(new Tag(key, value));
    }

}

