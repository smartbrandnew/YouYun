package com.broada.carrier.monitor.probe.impl.openapi.entity;

public class ResourceDetail {
	
	private String host_id;     // host_id
	private String host_name;   // host_name
	private String details;    // 详情字段

	public void setHost_id(String host_id) {
		this.host_id = host_id;
	}
	
	public String getHost_id() {
		return host_id;
	}
	
	public void setHost_name(String host_name) {
		this.host_name = host_name;
	}
	
	public String getHost_name() {
		return host_name;
	}
	
	public String getDetails() {
		return details;
	}
	
	public void setDetails(String details) {
		this.details = details;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ResourceDetail)){
			return false;
		} else{
			ResourceDetail rd = (ResourceDetail) obj;
			if(this.host_id.equals(rd.getHost_id()) && this.host_name.equals(rd.getHost_name()))
				return true;
			else 
				return false;
		}
	}
	
	@Override
	public int hashCode() {
		return this.host_id.concat(".").concat(this.host_name).hashCode();
	}
	
	
}
