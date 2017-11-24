package com.broada.carrier.monitor.method.cli;

import java.io.Serializable;

public class CLITestResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private String error;
	private String os;
	private String version;
	private String fullPrompt;

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFullPrompt() {
		return fullPrompt;
	}

	public void setFullPrompt(String fullPrompt) {
		this.fullPrompt = fullPrompt;
	}

}
