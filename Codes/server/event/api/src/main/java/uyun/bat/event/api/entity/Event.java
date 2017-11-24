package uyun.bat.event.api.entity;

import uyun.bat.event.api.util.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Event implements Serializable{

    /**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	
    private String id;
    private Date occurTime;
    private String resId;
    private String msgTitle;
    private String msgContent;
    private Short sourceType;
    private Short serverity;
    private String tenantId;
    private String monitorId;
    private String agentId;
    private String faultId;
    private String identity;
    private Integer relateCount;
    private Date firstRelateTime;
    private String monitorType;
    private EventMeta eventMeta;

    private List<EventTag> eventTags;

    private Long now;

    /**
     * 关联事件数（关联总数减去本事件）
     * @return
     */
    public Integer getRelateCount() {
        return relateCount;
    }

    public Event(){

    }

    public Event(String id,Date occurTime, Short serverity, String resId, String msgTitle, String msgContent, Short sourceType, String tenantId, String monitorId, String identity) {
        this.id=id;
        this.occurTime = occurTime;
        this.serverity = serverity;
        this.resId = resId;
        this.msgTitle = msgTitle;
        this.msgContent = msgContent;
        this.sourceType = sourceType;
        this.tenantId = tenantId;
        this.monitorId = monitorId;
        this.identity = identity;
    }

    public Long getNow() {
        return now;
    }

    public void setNow(Long now) {
        this.now = now;
    }

    public EventMeta getEventMeta() {
        return eventMeta;
    }

    public void setEventMeta(EventMeta eventMeta) {
        this.eventMeta = eventMeta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMonitorType() {
        return monitorType;
    }

    public void setMonitorType(String monitorType) {
        this.monitorType = monitorType;
    }

    public void setRelateCount(Integer relateCount) {
        this.relateCount = relateCount;
    }

    public Date getFirstRelateTime() {
        return firstRelateTime;
    }

    public void setFirstRelateTime(Date firstRelateTime) {
        this.firstRelateTime = firstRelateTime;
    }

    public Date getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(Date occurTime) {
        this.occurTime = occurTime;
    }


    public String getFaultId() {
        return faultId;
    }

    public void setFaultId(String faultId) {
        this.faultId = faultId;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public List<EventTag> getEventTags() {
        return eventTags;
    }

    public void setEventTags(List<EventTag> eventTags) {
        this.eventTags = eventTags;
    }


    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    public String getMsgTitle() {
        return msgTitle;
    }

    public void setMsgTitle(String msgTitle) {
        this.msgTitle = msgTitle;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public Short getSourceType() {
        return sourceType;
    }

    public void setSourceType(Short sourceType) {
        this.sourceType = sourceType;
    }

    public Short getServerity() {
        return serverity;
    }

    public void setServerity(Short serverity) {
        this.serverity = serverity;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(String monitorId) {
        this.monitorId = monitorId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public void verifyEvent() {
        if (StringUtils.isEmpty(msgTitle)) {
            throw new IllegalArgumentException("Event title is not allowed null");
        }
        if (StringUtils.isEmpty(msgContent)) {
            throw new IllegalArgumentException("Event content is not allowed null");
        }
        msgTitle=StringUtils.getLimitLengthString(msgTitle,100);
        msgContent=StringUtils.getLimitLengthString(msgContent,4000);

    }

}
