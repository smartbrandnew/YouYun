package com.broada.carrier.monitor.client.impl;

import java.awt.Font;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.client.impl.common.GuiExceptionHandler;
import com.broada.carrier.monitor.client.impl.config.Config;
import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.lang.SystemProperties;

public class Startup {
	private static Logger logger;
	
	private static void configureUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());			
		} catch (Exception e) {
			ErrorUtil.warn(logger, "皮肤初始化失败", e);		
		}

		FontUIResource f = new FontUIResource("微软雅黑", Font.PLAIN, 12);
		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
	}
	
	public static void checkSystemProperties() {	
		SystemProperties.setIfNotExists("monitor.logs.dir", Config.getWorkDir() + "/logs");
		SystemProperties.setIfNotExists("logback.configurationFile", Config.getConfDir() + "/logback.xml");
		SystemProperties.setIfNotExists("net.sf.ehcache.skipUpdateCheck", "true");
		SystemProperties.setIfNotExists("sun.awt.exception.handler", GuiExceptionHandler.class.getName());
	}

	public static void main(String[] args) {
		try {
			checkSystemProperties();
			logger = LoggerFactory.getLogger(Startup.class);
			configureUI();			
			if (LoginFrame.showLogin())
				MainWindow.display();
			else
				System.exit(0);
		} catch (Throwable e) {
			ErrorDlg.show("监测客户端运行失败", e);	
			System.exit(1);
		}
	}
}
