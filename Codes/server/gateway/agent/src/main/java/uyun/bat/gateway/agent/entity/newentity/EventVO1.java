package uyun.bat.gateway.agent.entity.newentity;

import java.util.List;

public class EventVO1 {
	private String host_id;
	private String type;
	private String name;
	private long timestamp;
	private String message;
	private String state;
	private List<String> tags;
	private String source;

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

	public String getHost_id() {
		return host_id;
	}

	public void setHost_id(String host_id) {
		this.host_id = host_id;
	}

	public EventVO1() {
		super();
	}

	public EventVO1(String host_id, String type, String name, long timestamp, String message, String state,
					List<String> tags, String source) {
		super();
		this.host_id = host_id;
		this.type = type;
		this.name = name;
		this.timestamp = timestamp;
		this.message = message;
		this.state = state;
		this.tags = tags;
		this.source = source;
	}

}
