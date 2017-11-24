package uyun.bat.monitor.core.entity;

import java.util.ArrayList;
import java.util.List;

import uyun.bat.monitor.core.mq.MQData;
import uyun.bat.monitor.core.util.TagUtil;

public class MetricData implements MQData {

	private String metric;
	private String tenantId;
	private List<TagEntry> tags;

	public MetricData() {
		super();
	}

	public MetricData(String metric, String tenantId) {
		super();
		this.metric = metric;
		this.tenantId = tenantId;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public List<TagEntry> getTags() {
		return tags;
	}

	public void setTags(List<TagEntry> tags) {
		this.tags = tags;
	}

	public void addTag(String key, String value) {
		if (tags == null)
			tags = new ArrayList<TagEntry>();
		tags.add(new TagEntry(key, value));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(metric);
		sb.append("@");
		sb.append(tenantId);
		if (tags != null && tags.size() > 0) {
			sb.append("[");
			for (TagEntry tag : tags) {
				sb.append(tag.getKey());
				if (tag.getValue() != null && tag.getValue().length() > 0) {
					sb.append(TagUtil.SEPARATOR);
					sb.append(tag.getValue());
				}
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("]");
		}
		return sb.toString();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetricData other = (MetricData) obj;
		if (metric == null) {
			if (other.metric != null)
				return false;
		} else if (!metric.equals(other.metric))
			return false;
		if (tags == null) {
			if (other.tags != null)
				return false;
		} else if (!tags.equals(other.tags))
			return false;

		if (tenantId == null) {
			if (other.tenantId != null)
				return false;
		} else if (!tenantId.equals(other.tenantId))
			return false;
		return true;
	}

}
