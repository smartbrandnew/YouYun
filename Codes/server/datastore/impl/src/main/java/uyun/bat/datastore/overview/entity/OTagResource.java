package uyun.bat.datastore.overview.entity;

/**
 * 标签与资源关系类
 */
public class OTagResource {
	private String tenantId;
	private String tagId;
	private String resourceId;

	public OTagResource(String tenantId, String tagId, String resourceId) {
		super();
		this.tenantId = tenantId;
		this.tagId = tagId;
		this.resourceId = resourceId;
	}

	public OTagResource() {
		super();
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

}
