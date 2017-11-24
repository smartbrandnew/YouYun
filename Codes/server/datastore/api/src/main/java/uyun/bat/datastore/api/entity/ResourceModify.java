package uyun.bat.datastore.api.entity;

import java.io.Serializable;
import java.util.List;

public class ResourceModify implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public static final String TYPE_UPDATE_RESOURCE_TAG = "update_resource_tag";
	public static final String TYPE_DELETE_RESOURCE = "delete_resource";
	public static final String TYPE_CREATE_RESOURCE = "create_resource";
	
	private String tenantId;
	private String resourceId;
	private List<ResourceTag> tags;
	private String type;

	public ResourceModify(String tenantId, String resourceId, List<ResourceTag> tags, String type) {
		this.tenantId = tenantId;
		this.resourceId = resourceId;
		this.tags = tags;
		this.type = type;
	}

	public ResourceModify() {

	}

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

	public List<ResourceTag> getTags() {
		return tags;
	}

	public void setTags(List<ResourceTag> tags) {
		this.tags = tags;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "ResourceModify [tenantId=" + tenantId + ", resourceId=" + resourceId + ", tags=" + tags + ", type=" + type
				+ "]";
	}
	

}
