package com.broada.carrier.monitor.impl.mw.jboss.basic;

public class ServerInformation {

	private String javaVendor;

	private String javaVersion;

	private String oSName;

	private String version;

	private String state;

	public String getJavaVendor() {
		return javaVendor;
	}

	public void setJavaVendor(String javaVendor) {
		this.javaVendor = javaVendor;
	}

	public String getJavaVersion() {
		return javaVersion;
	}

	public void setJavaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
	}

	public String getoSName() {
		return oSName;
	}

	public void setoSName(String oSName) {
		this.oSName = oSName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "ServerInformation [javaVendor=" + javaVendor + ", javaVersion=" + javaVersion + ", oSName=" + oSName
				+ ", version=" + version + ", state=" + state + "]";
	}

}
