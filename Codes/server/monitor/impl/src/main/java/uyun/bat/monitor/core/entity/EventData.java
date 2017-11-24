package uyun.bat.monitor.core.entity;

import uyun.bat.monitor.core.mq.MQData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventData implements MQData {

    private String title;
    private String content;
    private String tenantId;
    private List<TagEntry> tags;
    private String resId;
    private String identity;
    private Date occurTime;
    private Short serverity;
    private boolean recover;

    public EventData() {
        super();
    }

    public EventData(String title, String content, String tenantId, String resId, String identity, Date occurTime, Short serverity) {
        this.title = title;
        this.content = content;
        this.tenantId = tenantId;
        this.resId=resId;
        this.identity=identity;
        this.occurTime=occurTime;
        this.serverity=serverity;
    }

    public boolean isRecover() {
        return recover;
    }

    public void setRecover(boolean recover) {
        this.recover = recover;
    }

    public Date getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(Date occurTime) {
        this.occurTime = occurTime;
    }

    public Short getServerity() {
        return serverity;
    }

    public void setServerity(Short serverity) {
        this.serverity = serverity;
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

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventData other = (EventData) obj;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        if (content == null) {
            if (other.content != null)
                return false;
        } else if (!content.equals(other.content))
            return false;
        if (tags == null) {
            if (other.tags != null)
                return false;
        } else if (!tags.equals(other.tags))
            return false;
        if (tenantId == null) {
            if (other.tenantId != null)
                return false;
        } else if (!tenantId.equals(other.tenantId))
            return false;
        return true;
    }
}
