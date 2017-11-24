package uyun.bat.web.api.event.envity;

import java.util.Date;

public class EventVO {
    private String id;
    private Date occurTime;
    private String resId;
    private String msgTitle;
    private String msgContent;
    private Short serverity;
    private String monitorId;
    private int relateCount;
    private Date firstRelateTime;
    private String monitorName;
    private String resName;
    private String monitorType;
    private String faultId;
    private EventMetaVO metas;

    public EventVO(String id, Date occurTime, String resId, String msgTitle, String msgContent, Short serverity, String monitorId, int relateCount, Date firstRelateTime, String monitorName, String resName, String monitorType,String faultId,EventMetaVO eventMetaVO) {
        this.id = id;
        this.occurTime = occurTime;
        this.resId = resId;
        this.msgTitle = msgTitle;
        this.msgContent = msgContent;
        this.serverity = serverity;
        this.monitorId = monitorId;
        this.relateCount = relateCount;
        this.firstRelateTime = firstRelateTime;
        this.monitorName = monitorName;
        this.resName = resName;
        this.monitorType = monitorType;
        this.faultId=faultId;
        this.metas=eventMetaVO;
    }

    public EventVO(String id, Date occurTime, String resId, String msgTitle, String msgContent, Short serverity, String monitorId, String monitorName, String resName, String monitorType) {
        this.id = id;
        this.occurTime = occurTime;
        this.resId = resId;
        this.msgTitle = msgTitle;
        this.msgContent = msgContent;
        this.serverity = serverity;
        this.monitorId = monitorId;
        this.monitorName = monitorName;
        this.resName = resName;
        this.monitorType = monitorType;
    }

    public EventVO() {
    }

    public EventMetaVO getMetas() {
        return metas;
    }

    public void setMetas(EventMetaVO metas) {
        this.metas = metas;
    }

    public String getFaultId() {
        return faultId;
    }

    public void setFaultId(String faultId) {
        this.faultId = faultId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(Date occurTime) {
        this.occurTime = occurTime;
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

    public Short getServerity() {
        return serverity;
    }

    public void setServerity(Short serverity) {
        this.serverity = serverity;
    }

    public String getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(String monitorId) {
        this.monitorId = monitorId;
    }

    public int getRelateCount() {
        return relateCount==1?0:relateCount;
    }

    public void setRelateCount(int relateCount) {
        this.relateCount = relateCount;
    }

    public Date getFirstRelateTime() {
        return firstRelateTime;
    }

    public void setFirstRelateTime(Date firstRelateTime) {
        this.firstRelateTime = firstRelateTime;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getMonitorType() {
        return monitorType;
    }

    public void setMonitorType(String monitorType) {
        this.monitorType = monitorType;
    }
}
