package uyun.bat.datastore.api.entity;

import java.io.Serializable;

public class ResourceDetail implements Serializable {
	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private String resourceId;
	private String tenantId;
	private String detail;
	private String agentDesc;

	public ResourceDetail() {

	}

	public ResourceDetail(String resourceId, String tenantId, String detail, String agentDesc) {
		this.resourceId = resourceId;
		this.tenantId = tenantId;
		this.detail = detail;
		this.agentDesc = agentDesc;
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

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getAgentDesc() {
		return agentDesc;
	}

	public void setAgentDesc(String agentDesc) {
		this.agentDesc = agentDesc;
	}

	@Override
	public String toString() {
		return "ResourceDetail [resourceId=" + resourceId + ", tenantId=" + tenantId + ", detail=" + detail
				+ ", agentDesc=" + agentDesc + "]";
	}

}
