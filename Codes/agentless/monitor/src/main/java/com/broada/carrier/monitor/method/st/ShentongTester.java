package com.broada.carrier.monitor.method.st;

import com.broada.carrier.monitor.impl.db.st.ShentongManager;

public class ShentongTester {
	public String doTest(String host, ShentongMethod option) {
		ShentongManager sm = new ShentongManager(host, option);
		try {
			sm.initConnection();
		} catch (Throwable t) {
			return t.toString();
		} finally {
			sm.close();
		}
		return "true";
	}
}
