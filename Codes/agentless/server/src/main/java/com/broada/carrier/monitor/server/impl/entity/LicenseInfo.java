package com.broada.carrier.monitor.server.impl.entity;

/**
 * 授权许可信息
 * 
 * @author Jiangjw
 */
public class LicenseInfo {
	private boolean monitorBase;
	private int monitorPCServer;
	private int monitorMiniServer;
	private int monitorAppPlatform;
	private int monitorStorageDev;

	public LicenseInfo(boolean monitorBase, int monitorPCServer, int monitorMiniServer, int monitorAppPlatform, int monitorStorageDev) {
		this.monitorBase = monitorBase;
		this.monitorPCServer = monitorPCServer;
		this.monitorMiniServer = monitorMiniServer;
		this.monitorAppPlatform = monitorAppPlatform;
		this.monitorStorageDev = monitorStorageDev;
	}	

	public int getMonitorStorageDev() {
		return monitorStorageDev;
	}

	public boolean isMonitorBase() {
		return monitorBase;
	}

	public int getMonitorPCServer() {
		return monitorPCServer;
	}

	public int getMonitorMiniServer() {
		return monitorMiniServer;
	}

	public int getMonitorAppPlatform() {
		return monitorAppPlatform;
	}

}
