package uyun.bat.datastore.api.mq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uyun.bat.common.tag.entity.Tag;

public class MetricInfo implements Serializable {
	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	private String name;
	private String tenantId;
	private List<Tag> tags;

	public MetricInfo() {
		super();
	}

	public MetricInfo(String name, String tenantId) {
		super();
		this.name = name;
		this.tenantId = tenantId;
	}

	public MetricInfo(String name, String tenantId, List<Tag> tags) {
		super();
		this.name = name;
		this.tenantId = tenantId;
		this.tags = tags;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public void addTag(String key, String value) {
		if (tags == null)
			tags = new ArrayList<Tag>();
		tags.add(new Tag(key, value));
	}

	@Override
	public String toString() {
		return "MetricInfo [name=" + name + ", tenantId=" + tenantId + ", tags=" + tags + "]";
	}

}
