package uyun.bat.datastore.api.mq;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import uyun.bat.datastore.api.entity.OnlineStatus;

public class ResourceInfo implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String resourceId;
    private String tenantId;
    private String hostname;
    private Date lastCollectTime;
	/**
	 * 资源来源，暂时没用起来
	 */
	private short eventSourceType;
    private OnlineStatus onlineStatus;	
    private String ipaddr;
    private List<String> tags;
    
    public ResourceInfo() {
    }

    public ResourceInfo(String resourceId, String tenantId, String hostname, Date lastCollectTime, OnlineStatus onlineStatus, String ipaddr) {
        this.resourceId = resourceId;
        this.tenantId = tenantId;
        this.hostname = hostname;
        this.lastCollectTime = lastCollectTime;
        this.onlineStatus = onlineStatus;
        this.ipaddr=ipaddr;
    }

	public ResourceInfo(String resourceId, String tenantId, String hostname, Date lastCollectTime,
			short eventSourceType, OnlineStatus onlineStatus, String ipaddr, List<String> tags) {
		this.resourceId = resourceId;
		this.tenantId = tenantId;
		this.hostname = hostname;
		this.lastCollectTime = lastCollectTime;
		this.eventSourceType = eventSourceType;
		this.onlineStatus = onlineStatus;
		this.ipaddr = ipaddr;
		this.tags = tags;
	}
    
    public ResourceInfo(String resourceId, String tenantId, String hostname, Date lastCollectTime, short eventSourceType, OnlineStatus onlineStatus, String ipaddr) {
        this.resourceId = resourceId;
        this.tenantId = tenantId;
        this.hostname = hostname;
        this.lastCollectTime = lastCollectTime;
        this.eventSourceType = eventSourceType;
        this.onlineStatus=onlineStatus;
        this.ipaddr=ipaddr;
    }

    public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public OnlineStatus getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(OnlineStatus onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public short getEventSourceType() {
        return eventSourceType;
    }

    public void setEventSourceType(short eventSourceType) {
        this.eventSourceType = eventSourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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
}
