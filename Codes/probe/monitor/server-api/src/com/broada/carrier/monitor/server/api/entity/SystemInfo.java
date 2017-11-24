package com.broada.carrier.monitor.server.api.entity;

public class SystemInfo {
	private String code;
	private String name;
	private Object value;

	public SystemInfo() {		
	}
	
	public SystemInfo(String code, String name, Object value) {
		this.code = code;
		this.name = name;
		this.value = value;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
