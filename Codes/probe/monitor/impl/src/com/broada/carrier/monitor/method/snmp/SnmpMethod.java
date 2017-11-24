package com.broada.carrier.monitor.method.snmp;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.snmputil.Snmp;
import com.broada.snmputil.SnmpTarget;
import com.broada.snmputil.SnmpTarget.AuthProtocol;
import com.broada.snmputil.SnmpTarget.PrivProtocol;

public class SnmpMethod extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	public static final String TYPE_ID = "ProtocolSnmp";
	
	public SnmpMethod() {		
	}

	public SnmpMethod(String community) {
		setVersion(SnmpVersion.V2C);
		setCommunity(community);
	}

	public SnmpMethod(MonitorMethod method) {
		super(method);
	}

	public SnmpTarget getTarget(String ip) {
		SnmpTarget target = new SnmpTarget();
		target.setIp(ip);
		target.setPort(getPort());
		target.setVersion(getVersion().getId());
		target.setTimeout(getTimeout());
		target.setRetryTime(getRetryTime());
		if (target.getVersion() == 3) {
			target.setAuthProtocol(getAuthProtocol());
			target.setAuthPassword(getAuthPassword());
			target.setSecurityLevel(getSecurityLevel());
			target.setSecurityName(getSecurityName());
			target.setPrivProtocol(getPrivProtocol());
			target.setPrivPassword(getPrivPassword());
		} else {
			target.setReadCommunity(getCommunity());
		}
		return target;
	}

	public String getSecurityLevel() {
		return getProperties().get("securityLevel", Snmp.SAFELEVEL_NOAUTHNOPRIV);
	}
	
	public void setSecurityLevel(String securityLevel) {
		getProperties().set("securityLevel", securityLevel);
	}

	public String getPrivPassword() {
		return getProperties().get("privPassword", "");
	}
	
	public void setPrivPassword(String privPassword) {
		getProperties().set("privPassword", privPassword);
	}

	public PrivProtocol getPrivProtocol() {
		return PrivProtocol.valueOf(getProperties().get("privProtocol", PrivProtocol.AES128.toString()));
	}
	
	public void setPrivProtocol(String privProtocol) {
		getProperties().set("privProtocol", privProtocol);
	}	

	public String getSecurityName() {
		return getProperties().get("securityName", "");
	}
	
	public void setSecurityName(String securityName) {
		getProperties().set("securityName", securityName);
	}	

	public String getAuthPassword() {
		return getProperties().get("authPassword", "");
	}

	public void setAuthPassword(String authPassword) {
		getProperties().set("authPassword", authPassword);
	}	
	
	public AuthProtocol getAuthProtocol() {
		return AuthProtocol.valueOf(getProperties().get("authProtocol", AuthProtocol.MD5.toString()));
	}
	
	public void setAuthProtocol(String authProtocol) {
		getProperties().set("authProtocol", authProtocol);
	}	

	public String getCommunity() {
		return getProperties().get("community", "public");
	}

	public int getRetryTime() {
		return getProperties().get("retryTime", 1);
	}

	public long getTimeout() {
		return getProperties().get("timeout", 1000);
	}
	
	public void setTimeout(long timeout) {
		getProperties().set("timeout", timeout);
	}

	public SnmpVersion getVersion() {
		return SnmpVersion.valueOf(getProperties().get("version", SnmpVersion.V2C.toString()).toUpperCase());
	}

	public void setCommunity(String community) {
		getProperties().set("community", community);
	}

	public void setVersion(SnmpVersion version) {
		getProperties().set("version", version.toString());
	}

	public int getPort() {
		return getProperties().get("port", 161);
	}
	
	public void setPort(int port) {
		getProperties().set("port", port);
	}

}
