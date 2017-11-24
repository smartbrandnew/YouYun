package uyun.bat.agent.impl.autosync.entity;

public class Condition {
	private String name;
	private String value;

	public Condition() {
		super();
	}

	public Condition(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
