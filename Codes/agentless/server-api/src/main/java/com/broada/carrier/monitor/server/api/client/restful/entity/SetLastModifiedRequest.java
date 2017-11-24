package com.broada.carrier.monitor.server.api.client.restful.entity;

public class SetLastModifiedRequest {
	private String file;
	private long lastModified;

	public SetLastModifiedRequest() {		
	}

	public SetLastModifiedRequest(String file, long lastModified) {
		this.file = file;
		this.lastModified = lastModified;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

}
