package com.broada.carrier.monitor.probe.impl.openapi.entity;

import java.util.List;

public class EventVO {
	private String id;
	private String hostId;
	private String type;
	private String name;
	private long timestamp;
	private String message;
	private String state;
	private List<String> tags;
	private String source;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public EventVO() {
		super();
	}

	public EventVO(String id, String hostId, String type, String name, long timestamp, String message, String state,
			List<String> tags, String source) {
		super();
		this.id = id;
		this.hostId = hostId;
		this.type = type;
		this.name = name;
		this.timestamp = timestamp;
		this.message = message;
		this.state = state;
		this.tags = tags;
		this.source = source;
	}

	@Override
	public String toString() {
		return "EventVO [id=" + id + ", hostId=" + hostId + ", type=" + type
				+ ", name=" + name + ", timestamp=" + timestamp + ", message="
				+ message + ", state=" + state + ", tags=" + tags + ", source="
				+ source + "]";
	}
	
}
