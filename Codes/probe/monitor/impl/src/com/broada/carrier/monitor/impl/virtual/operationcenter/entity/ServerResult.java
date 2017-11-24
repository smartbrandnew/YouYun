package com.broada.carrier.monitor.impl.virtual.operationcenter.entity;

import java.util.ArrayList;
import java.util.List;

public class ServerResult extends Result{
	
	private List<ServerInfo> phy_servers = new ArrayList<ServerInfo>();

	public void setPhy_servers(List<ServerInfo> phy_servers) {
		this.phy_servers = phy_servers;
	}
	public List<ServerInfo> getPhy_servers() {
		return phy_servers;
	}
	
}
