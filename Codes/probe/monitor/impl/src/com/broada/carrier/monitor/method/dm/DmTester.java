package com.broada.carrier.monitor.method.dm;

import com.broada.carrier.monitor.impl.db.dm.DmManager;

public class DmTester {
	public String doTest(String host, DmMonitorMethodOption option) {
		DmManager dm = new DmManager(host, option);
		try {
			dm.initConnection();
		} catch (Throwable t) {
			return t.toString();
		} finally {
			dm.close();
		}
		return "true";
	}
}
