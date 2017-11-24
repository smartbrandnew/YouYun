package com.broada.carrier.monitor.impl.mw.weblogic.agent.cluster;

/**
 * 
 * 集群中server的实例
 * @author Yaojj 
 * Create By Mar 23, 2010 10:22:06 AM
 */
public class ServerInstance {
	//是否被监测
	private boolean isMonitored;
	//server名称
	private String serverName;
	//server状态
	private String state;
	
	//集群名
	private String clusterName;

	public boolean isMonitored() {
		return isMonitored;
	}

	public void setMonitored(boolean isMonitored) {
		this.isMonitored = isMonitored;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	
	

}
