package com.broada.carrier.monitor.method.informix;

import com.broada.carrier.monitor.impl.db.informix.InformixManager;

public class InformixTester {

	public String doTest(String host, InformixMonitorMethodOption option) {
		InformixManager im = new InformixManager(host, option);
		try {
			im.initConnection();
		} catch (Throwable t) {
			return t.toString();
		} finally {
			im.close();
		}
		return "true";
	}
}
