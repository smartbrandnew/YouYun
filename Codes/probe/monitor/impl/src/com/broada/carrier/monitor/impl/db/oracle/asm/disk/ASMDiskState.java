package com.broada.carrier.monitor.impl.db.oracle.asm.disk;

public class ASMDiskState {
	
	private String path;        // Operating system pathname portion of the name returned by discovery
	private String mode_status; // Global status about which kinds of I/O requests are allowed to the disk:ONLINE/OFFLINE
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getMode_status() {
		return mode_status;
	}
	public void setMode_status(String mode_status) {
		this.mode_status = mode_status;
	}
	
}
