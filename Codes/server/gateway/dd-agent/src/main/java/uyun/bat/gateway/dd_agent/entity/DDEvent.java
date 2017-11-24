package uyun.bat.gateway.dd_agent.entity;

import java.util.List;

public class DDEvent {

	/**
	 * 时间戳
	 */
	private long timestamp;

	/**
	 * 事件类型：dogstream_event，apache，mysql
	 */
	private String eventType;
	/**
	 * title
	 */
	private String msgTitle;
	/**
	 * 内容
	 */
	private String msgText;
	/**
	 * a key to use for aggregating events 事件对象名称 localhost:8080,
	 */
	private String aggregationKey;
	/**
	 * (optional) string, one of ('error', 'warning', 'success', 'info');defaults
	 * to 'info'
	 */
	private String alertType;
	/**
	 * 源对象类型名apache，mysql
	 */
	private String sourceTypeName;
	/**
	 * 主机名
	 */
	private String host;
	/**
	 * tags
	 */
	private List<TagEntry> tags;
	/**
	 * (optional) string which specifies the priority of the event (Normal, Low)
	 */
	private String priority;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getMsgTitle() {
		return msgTitle;
	}

	public void setMsgTitle(String msgTitle) {
		this.msgTitle = msgTitle;
	}

	public String getMsgText() {
		return msgText;
	}

	public void setMsgText(String msgText) {
		this.msgText = msgText;
	}

	public String getAggregationKey() {
		return aggregationKey;
	}

	public void setAggregationKey(String aggregationKey) {
		this.aggregationKey = aggregationKey;
	}

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public String getSourceTypeName() {
		return sourceTypeName;
	}

	public void setSourceTypeName(String sourceTypeName) {
		this.sourceTypeName = sourceTypeName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public List<TagEntry> getTags() {
		return tags;
	}

	public void setTags(List<TagEntry> tags) {
		this.tags = tags;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}
}
