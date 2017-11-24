package com.broada.carrier.monitor.client.impl;


public class TestClient {
	public static void main(String[] args) {
		String userDir = System.getProperty("user.dir") + "/../../../build/dist-client";
		System.setProperty("user.dir", userDir);
		System.setProperty("logback.configurationFile", userDir + "/conf/logback.xml");
				
		com.broada.module.autosync.client.api.startup.Startup.main(new String[] {
				com.broada.carrier.monitor.client.impl.Startup.class.getName(),				
		});		
	}
}
