package com.broada.carrier.monitor.impl.db.postgresql.session;

public class PostgreSQLSession {
	public static String[] alermItem = new String[] {"_UserCount", "_ConnCount"};
  String procpid;
  String usename;
  String client_addr;
  String client_port;
  String backend_start;
  
	public String getProcpid() {
		return procpid;
	}
	public void setProcpid(String procpid) {
		this.procpid = procpid;
	}
	public String getUsename() {
		return usename;
	}
	public void setUsename(String usename) {
		this.usename = usename;
	}
	public String getClient_addr() {
		return client_addr;
	}
	public void setClient_addr(String client_addr) {
		this.client_addr = client_addr;
	}
	public String getClient_port() {
		return client_port;
	}
	public void setClient_port(String client_port) {
		this.client_port = client_port;
	}
	public String getBackend_start() {
		return backend_start;
	}
	public void setBackend_start(String backend_start) {
		this.backend_start = backend_start;
	}
}
