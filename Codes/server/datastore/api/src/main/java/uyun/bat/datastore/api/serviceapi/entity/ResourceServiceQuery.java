package uyun.bat.datastore.api.serviceapi.entity;

import java.util.List;

public class ResourceServiceQuery {
	private String hostname;
	private String ipaddr;
	private List<String> tags;
	private String tenantId;
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getIpaddr() {
		return ipaddr;
	}
	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	@Override
	public String toString() {
		return "ResourceServiceQuery [hostname=" + hostname + ", ipaddr=" + ipaddr + ", tags=" + tags + ", tenantId="
				+ tenantId + "]";
	}
	
	
	
}
