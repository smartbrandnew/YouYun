package com.broada.carrier.monitor.server.api.client.restful.entity;

import com.broada.carrier.monitor.common.remoteio.api.RemoteIOMode;

public class OpenFileRequest {
	private String file;
	private RemoteIOMode mode;

	public OpenFileRequest() {
	}

	public OpenFileRequest(String file, RemoteIOMode mode) {
		this.file = file;
		this.mode = mode;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public RemoteIOMode getMode() {
		return mode;
	}

	public void setMode(RemoteIOMode mode) {
		this.mode = mode;
	}

}
