package uyun.bat.monitor.core.entity;

import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.monitor.core.mq.MQData;

import java.util.Date;

public class ResourceData implements MQData {
    private String resourceId;
    private String tenantId;
    private String hostname;
    private Date lastCollectTime;
    private short eventSourceType;
    private OnlineStatus onlineStatus;
    private String ipaddr;

    public ResourceData() {
    }

    public ResourceData(String resourceId, String tenantId, String hostname, Date lastCollectTime, short eventSourceType, OnlineStatus onlineStatus, String ipaddr) {
        this.resourceId = resourceId;
        this.tenantId = tenantId;
        this.hostname = hostname;
        this.lastCollectTime = lastCollectTime;
        this.eventSourceType = eventSourceType;
        this.onlineStatus=onlineStatus;
        this.ipaddr=ipaddr;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Date getLastCollectTime() {
        return lastCollectTime;
    }

    public void setLastCollectTime(Date lastCollectTime) {
        this.lastCollectTime = lastCollectTime;
    }

    public short getEventSourceType() {
        return eventSourceType;
    }

    public void setEventSourceType(short eventSourceType) {
        this.eventSourceType = eventSourceType;
    }

    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(OnlineStatus onlineStatus) {
        this.onlineStatus = onlineStatus;
    }
}
