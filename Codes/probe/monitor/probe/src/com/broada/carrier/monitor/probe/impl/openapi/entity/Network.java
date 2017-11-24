package com.broada.carrier.monitor.probe.impl.openapi.entity;

public class Network {
	
	private String name;
	private String macaddress;
	private String ipaddress;
	private String ipaddressv6;
	
	public Network() {
		// TODO Auto-generated constructor stub
	}
	public Network(String name, String macaddress, String ipaddress, String ipaddressv6) {
		this.name = name;
		this.macaddress = macaddress;
		this.ipaddress = ipaddress;
		this.ipaddressv6 = ipaddressv6;
	}
	
	public String getMacaddress() {
		return macaddress;
	}
	
	public void setMacaddress(String macaddress) {
		this.macaddress = macaddress;
	}
	
	public String getIpaddress() {
		return ipaddress;
	}
	
	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
	
	public String getIpaddressv6() {
		return ipaddressv6;
	}
	
	public void setIpaddressv6(String ipaddressv6) {
		this.ipaddressv6 = ipaddressv6;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
