package uyun.bat.gateway.agent.entity;

import java.util.List;

public class StateSnapshoot {
	private List<String> tags;
	private String value;

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public StateSnapshoot() {
		super();
	}

	public StateSnapshoot(List<String> tags, String value) {
		super();
		this.tags = tags;
		this.value = value;
	}

}
