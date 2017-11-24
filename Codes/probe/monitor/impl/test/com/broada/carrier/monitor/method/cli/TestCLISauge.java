package com.broada.carrier.monitor.method.cli;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.broada.carrier.monitor.common.entity.DefaultDynamicObject;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.cid.action.api.entity.Protocol;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocol;
import com.broada.cid.action.protocol.impl.ccli.CcliSession;

public class TestCLISauge {
	
	@Test
	public void cliTest() {
		MonitorMethod method = new MonitorMethod();
		method.setTypeId("ProtocolCcli");
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("sessionName", "ssh");
		props.put("remotePort", 22);
		props.put("loginTimeout", 10000);
		props.put("prompt", "#");
		props.put("username", "root");
		props.put("password", "root");
		props.put("sysname", "linux");
		props.put("waitTimeout", 15000);
//		props.put("port", 161);
//		props.put("community", "public");
//		props.put("timeout", 5000);
//		props.put("snmpVersion", "V1");
		
		method.setProperties(props);
		DefaultDynamicObject properties = method.getProperties();
		CcliProtocol protocol = new CcliProtocol(new Protocol("cli",
				properties));
		protocol.setField("ip", "10.1.10.61");
//		SnmpSession snmp = new SnmpSession(protocol);
//		SnmpValue vars = snmp.get(".1.3.6.1.2.1.25.3.3.1.2");
		CcliSession session = new CcliSession(protocol);
		session.connect();
		String stat = session.execute("ls");
		session.disconnect();
		System.out.println(stat);
	}

}