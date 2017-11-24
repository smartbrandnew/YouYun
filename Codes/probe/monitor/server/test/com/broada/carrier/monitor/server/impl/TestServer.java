package com.broada.carrier.monitor.server.impl;

import com.broada.carrier.monitor.common.tomcat.TomcatStartup;
import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.component.utils.lang.SystemProperties;

public class TestServer {
	public static void main(String[] args) {
		String moduleDir = System.getProperty("user.dir");
		String userDir = moduleDir + "/../../../build/dist/monitor";
		System.setProperty("user.dir", userDir);
		SystemProperties.setIfNotExists("monitor.db.config", userDir + "/../conf/jdbc.properties");
		SystemProperties.setIfNotExists("patch.dir", userDir + "/patch");
		SystemProperties.setIfNotExists("patch.patterns", "carrier.*;custom.*");

		WebStartupListener.checkSystemProperties();
		TomcatStartup.main(new String[] {
				moduleDir + "/../../../build/dist/platform/tomcat", Config.getWorkDir(),
				"conf/tomcat.xml"
		});
	}
}
