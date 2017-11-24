package com.broada.carrier.monitor.method.fusioncompute;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class FusionComputeMethod extends MonitorMethod{
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolFusionCompute";
	private String username;
	private String password;
	private int port;

	
	public FusionComputeMethod(){
		
	}
	
	public FusionComputeMethod(MonitorMethod copy){
		super(copy);
		this.username = (String) copy.getProperties().get("username");
		this.password = (String) copy.getProperties().get("password");
		this.port = (Integer) copy.getProperties().get("port");
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "FusionComputeMethod [username=" + username + ", password=" + password + ", port=" + port + "]";
	}
	
	
	
	
}
