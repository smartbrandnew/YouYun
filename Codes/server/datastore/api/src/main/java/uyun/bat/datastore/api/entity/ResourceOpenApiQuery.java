package uyun.bat.datastore.api.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResourceOpenApiQuery {
	private String tenantId;
	private String ipaddr;
	private String hostname;
	private String resourceType;
	private List<String> tags = new ArrayList<String>();
	private List<String> apps = new ArrayList<String>();
	private Date minUpdateTime;
	private int pageNo;
	//默认值为10
	private int pageSize;

	public ResourceOpenApiQuery() {

	}

	public ResourceOpenApiQuery(String tenantId, String ipaddr, String hostname, String resourceType, List<String> tags,
			List<String> apps, Date minUpdateTime, int pageNo, int pageSize) {
		this.tenantId = tenantId;
		this.ipaddr = ipaddr;
		this.hostname = hostname;
		this.resourceType = resourceType;
		this.tags = tags;
		this.apps = apps;
		this.minUpdateTime = minUpdateTime;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getIpaddr() {
		return ipaddr;
	}

	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Date getMinUpdateTime() {
		return minUpdateTime;
	}

	public void setMinUpdateTime(Date minUpdateTime) {
		this.minUpdateTime = minUpdateTime;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public List<String> getApps() {
		return apps;
	}

	public void setApps(List<String> apps) {
		this.apps = apps;
	}

}
