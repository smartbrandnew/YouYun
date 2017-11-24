package com.broada.carrier.monitor.probe.impl.openapi.entity;

public class FileSystem {
	
	private String mounted_on;
	private String kb_size;
	private String name;
	
	public FileSystem() {
		// TODO Auto-generated constructor stub
	}
	public FileSystem(String mounted_on, String kb_size, String name) {
		this.mounted_on = mounted_on;
		this.kb_size = kb_size;
		this.name = name;
	}
	
	public String getMounted_on() {
		return mounted_on;
	}
	public void setMounted_on(String mounted_on) {
		this.mounted_on = mounted_on;
	}
	public String getKb_size() {
		return kb_size;
	}
	public void setKb_size(String kb_size) {
		this.kb_size = kb_size;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
