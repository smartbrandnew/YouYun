package com.broada.carrier.monitor.impl.virtual.operationcenter.entity;

import java.util.ArrayList;
import java.util.List;

public class VMResult extends Result{
	
	private List<VMInfo> servers = new ArrayList<VMInfo>();
	
	public List<VMInfo> getServers() {
		return servers;
	}
	public void setServers(List<VMInfo> servers) {
		this.servers = servers;
	}
	
}
