package uyun.bat.datastore.api.entity;

import java.util.Date;
import java.util.List;

public class SimpleResource {
	private String resourceId;
	private String resourceName;
	private String tenantId;
	private String ipaddr;
	private Date lastCollectTime;
	private Date createTime;
	private List<String> tags;
	private String onlineStatus;
	
	public SimpleResource() {

	}
	

	public SimpleResource(String resourceId, String resourceName, String tenantId, String ipaddr, Date lastCollectTime,
			Date createTime, List<String> tags, String onlineStatus) {
		super();
		this.resourceId = resourceId;
		this.resourceName = resourceName;
		this.tenantId = tenantId;
		this.ipaddr = ipaddr;
		this.lastCollectTime = lastCollectTime;
		this.createTime = createTime;
		this.tags = tags;
		this.onlineStatus = onlineStatus;
	}



	public String getIpaddr() {
		return ipaddr;
	}

	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}

	public Date getLastCollectTime() {
		return lastCollectTime;
	}

	public void setLastCollectTime(Date lastCollectTime) {
		this.lastCollectTime = lastCollectTime;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	@Override
	public String toString() {
		return "SimpleResource [resourceId=" + resourceId + ", resourceName=" + resourceName + ", tenantId=" + tenantId
				+",ipaddr="+ipaddr+",lastCollectTime="+lastCollectTime+ "]";
	}


}
