package com.broada.carrier.monitor.impl.db.oracle.asm.diskgroup;

public class ASMDiskGroup {
	
	private String name;    // Name of the disk group
	private double usage_pct;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getUsage_pct() {
		return usage_pct;
	}
	public void setUsage_pct(double usage_pct) {
		this.usage_pct = usage_pct;
	}
	
}
