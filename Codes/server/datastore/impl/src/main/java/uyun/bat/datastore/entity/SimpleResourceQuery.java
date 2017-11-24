package uyun.bat.datastore.entity;

import java.util.Date;
import java.util.List;

public class SimpleResourceQuery {
	private List<String> tags;
	private Date lastCollectTime;
	private String onlineStatus;
	private String tenantId;
	private List<String> hosts;

	public SimpleResourceQuery() {

	}

	public SimpleResourceQuery(List<String> tags, Date lastCollectTime, String onlineStatus) {
		this.tags = tags;
		this.lastCollectTime = lastCollectTime;
		this.onlineStatus = onlineStatus;
	}

	public SimpleResourceQuery(List<String> tags,String tenantId,List<String> hosts) {
		this.tags = tags;
		this.tenantId = tenantId;
		this.hosts=hosts;
	}

	public List<String> getHosts() {
		return hosts;
	}

	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Date getLastCollectTime() {
		return lastCollectTime;
	}

	public void setLastCollectTime(Date lastCollectTime) {
		this.lastCollectTime = lastCollectTime;
	}

	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

}
