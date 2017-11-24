package com.broada.carrier.monitor.impl.host.ipmi.disk;

public class DiskInfo {
	
	private String diskName;
	private String diskStat;
	
	public void setDiskName(String diskName) {
		this.diskName = diskName;
	}
	public String getDiskName() {
		return diskName;
	}
	public void setDiskStat(String diskStat) {
		this.diskStat = diskStat;
	}
	public String getDiskStat() {
		return diskStat;
	}
	
}
