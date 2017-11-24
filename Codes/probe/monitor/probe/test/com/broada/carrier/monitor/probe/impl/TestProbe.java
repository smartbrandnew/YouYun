package com.broada.carrier.monitor.probe.impl;


import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;

import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.tomcat.TomcatStartup;
import com.broada.module.autosync.client.api.startup.Startup;

public class TestProbe {
	public static void main(String[] args) {
		/*
		String moduleDir = System.getProperty("user.dir");
		System.setProperty("user.dir", moduleDir + "/../../../build/dist-probe");
				
		WebStartupListener.checkSystemProperties();
		Startup.main(new String[] {
				TomcatStartup.class.getName(),				
		});	
		*/
		File folder=new File(Config.getYamlDir());
		String[] fileNames=folder.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if(!name.endsWith(".yaml.example"))
					return true;
				return false;
			}
		});
		System.out.println(Arrays.asList(fileNames));

	}
}
