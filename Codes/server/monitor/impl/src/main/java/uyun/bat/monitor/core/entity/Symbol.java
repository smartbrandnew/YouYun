package uyun.bat.monitor.core.entity;

import java.util.ArrayList;
import java.util.List;

import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.monitor.api.common.util.StateUtil;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.util.TagUtil;

/**
 * 监测器触发的事件标识
 */
public class Symbol {

	private MonitorType monitorType;
	/**
	 * 租户Id
	 */
	private String tenantId;
	/**
	 * 监测器id
	 */
	private String monitorId;
	/**
	 * 查询语句
	 */
	private String query;
	/**
	 * 监测器触发状态
	 */
	private MonitorState monitorState;
	/**
	 * tag列表
	 */
	private List<TagEntry> tags;

	public MonitorType getMonitorType() {
		return monitorType;
	}

	public void setMonitorType(MonitorType monitorType) {
		this.monitorType = monitorType;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public MonitorState getMonitorState() {
		return monitorState;
	}

	public void setMonitorState(MonitorState monitorState) {
		this.monitorState = monitorState;
	}

	public List<TagEntry> getTags() {
		return tags;
	}

	public void setTags(List<TagEntry> tags) {
		this.tags = tags;
	}

	private String resourceId;

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		sb.append("uyun.monitor");
		sb.append("],[");
		sb.append(query);
		sb.append("],");
		if (tags != null && tags.size() > 0) {
			for (TagEntry tagEntry : tags) {
				sb.append("[");
				sb.append(tagEntry.getKey());
				if (tagEntry.getValue() != null && tagEntry.getValue().length() > 0) {
					sb.append(TagUtil.SEPARATOR);
					sb.append(tagEntry.getValue());
				}
				sb.append("],");
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * {@link Checkpoint} state
	 */
	public String generateState() {
		return StateUtil.generateState(monitorType);
	}

	/**
	 * {@link Checkpoint} tags
	 * 
	 * @return
	 */
	public String[] generateTags() {
		List<TagEntry> tagList = new ArrayList<TagEntry>();
		if (tags != null && tags.size() > 0) {
			tagList.addAll(tags);
		}
		TagUtil.generateTags(tagList);
		// 保存检查点时，增加租户id，监测器id 等tags
		tagList.add(0, new TagEntry(StateUtil.MONITOR_ID, monitorId));
		tagList.add(0, new TagEntry(StateUtil.TENANT_ID, tenantId));

		String[] temps = new String[tagList.size()];
		for (int i = 0, length = tagList.size(); i < length; i++) {
			TagEntry te = tagList.get(i);
			temps[i] = te.getKey() + TagUtil.SEPARATOR + te.getValue();
		}
		return temps;
	}
}
