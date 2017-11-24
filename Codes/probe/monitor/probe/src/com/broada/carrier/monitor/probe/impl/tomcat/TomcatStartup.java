package com.broada.carrier.monitor.probe.impl.tomcat;

import java.io.File;

import com.broada.carrier.monitor.common.util.FileUtil;
import com.broada.carrier.monitor.probe.impl.config.Config;

public class TomcatStartup {
	public static void main(String[] args) {
		String orgTomcatFile = Config.getWorkDir() + "/conf/tomcat.xml";
		String tomcatConfig = FileUtil.readString(new File(orgTomcatFile), "utf-8");		
		tomcatConfig = tomcatConfig.replaceAll("\\$\\{port\\}", Integer.toString(Config.getDefault().getProbe().getPort()));
		String runningTomcatFile = Config.getTempDir() + "/tomcat.running.xml";
		FileUtil.writeString(new File(runningTomcatFile), tomcatConfig, "utf-8");
		com.broada.carrier.monitor.common.tomcat.TomcatStartup.main(new String[]{
				Config.getWorkDir() + "/tomcat",
				Config.getWorkDir(),
				runningTomcatFile
		});
	}
}
