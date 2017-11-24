package com.broada.carrier.monitor.server.api.client.restful.entity;

public class UploadFileRequest {
	private String serverFilePath;
	private String probeFilePath;

	public UploadFileRequest(String serverFilePath, String probeFilePath) {
		this.serverFilePath = serverFilePath;
		this.probeFilePath = probeFilePath;
	}

	public UploadFileRequest() {
	}

	public String getServerFilePath() {
		return serverFilePath;
	}

	public void setServerFilePath(String serverFilePath) {
		this.serverFilePath = serverFilePath;
	}

	public String getProbeFilePath() {
		return probeFilePath;
	}

	public void setProbeFilePath(String probeFilePath) {
		this.probeFilePath = probeFilePath;
	}

}
