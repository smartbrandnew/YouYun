package uyun.bat.datastore.api.entity;

import java.io.Serializable;

public class ResourceTag implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String key;
	private String value;
	private String tenantId;

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public ResourceTag() {

	}

	public ResourceTag(String id, String key, String value, String tenantId) {
		this.id = id;
		this.key = key;
		this.value = value;
		this.tenantId = tenantId;
	}

	public ResourceTag(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String changeToString() {
		return key + ":" + value;
	}

	@Override
	public String toString() {
		return "ResourceTag [id=" + id + ", key=" + key + ", value=" + value + ", tenantId=" + tenantId + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ResourceTag resourceTag = (ResourceTag) o;

		return id.equals(resourceTag.getId()) && key.equals(resourceTag.key) && value.equals(resourceTag.value)
				&& tenantId.equals(resourceTag.getTenantId());

	}

	@Override
	public int hashCode() {
		int result = id.hashCode();
		result = 31 * result + (key != null ? key.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		result = 31 * result + (tenantId.hashCode());
		return result;
	}

}
