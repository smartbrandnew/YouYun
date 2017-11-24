package com.broada.carrier.monitor.method.snmp;

public enum SnmpVersion {
	V1(0), V2C(1), V3(3);
	
	private int id;
	
	private SnmpVersion(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
