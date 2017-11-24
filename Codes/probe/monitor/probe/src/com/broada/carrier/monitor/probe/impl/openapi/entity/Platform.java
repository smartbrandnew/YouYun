package com.broada.carrier.monitor.probe.impl.openapi.entity;

public class Platform {
	
	private String hostname;
	private String kernel_version;
	private String machine_type;
	private String kernel;
	private String os;
	private String build_version;
	private String hardware;
	
	public Platform() {
		// TODO Auto-generated constructor stub
	}
	public Platform(String hostname, String kernel_version, String machine_type, String kernel,
			String os, String build_version, String hardware) {
		this.hardware = hardware;
		this.build_version= build_version;
		this.os = os;
		this.kernel = kernel;
		this.machine_type = machine_type;
		this.kernel_version = kernel_version;
		this.hostname = hostname;
	}
	
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getKernel_version() {
		return kernel_version;
	}
	public void setKernel_version(String kernel_version) {
		this.kernel_version = kernel_version;
	}
	public String getMachine_type() {
		return machine_type;
	}
	public void setMachine_type(String machine_type) {
		this.machine_type = machine_type;
	}
	public String getKernel() {
		return kernel;
	}
	public void setKernel(String kernel) {
		this.kernel = kernel;
	}
	public String getOs() {
		return os;
	}
	public void setOs(String os) {
		this.os = os;
	}
	public String getBuild_version() {
		return build_version;
	}
	public void setBuild_version(String build_version) {
		this.build_version = build_version;
	}
	public String getHardware() {
		return hardware;
	}
	public void setHardware(String hardware) {
		this.hardware = hardware;
	}
	
}
