package com.broada.carrier.monitor.common.tomcat;

import java.io.File;

import org.apache.catalina.startup.Bootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tomcat启动器
 * @author Jiangjw
 */
public class TomcatStartup {
	private static final Logger logger = LoggerFactory.getLogger(TomcatStartup.class);
	
	/**
	 * 启动tomcat
	 * @param tomcatHome tomcat安装路径
	 * @param tomcatBase tomcat工作目录
	 * @param tomcatConfPath tomcat配置文件路径
	 */
	public static void startup(String tomcatHome, String tomcatBase, String tomcatConfPath) {
		System.setProperty("catalina.home", tomcatHome);
		System.setProperty("catalina.base", tomcatBase);
		
		File file = new File(System.getProperty("java.io.tmpdir"));
		if (!file.exists())
			file.mkdirs();
		
		Bootstrap.main(new String[]{"-config", tomcatConfPath, "start"});
	}
	
	public static void main(String[] args) {
		if (args.length <= 0) {
			printUsage();
		}
		String tomcatHome = args[0];
		String tomcatBase = (args.length < 2) ? tomcatHome : args[1];
		String tomcatConfPath = (args.length < 3) ? "conf/server.xml" : args[2];

		startup(tomcatHome, tomcatBase, tomcatConfPath);
	}

	private static void printUsage() {
		logger.info("使用方法: TomcatStartup tomcat目录 工作目录 配置文件路径");
		logger.info("使用例子: TomcatStartup d:/tomcat d:/work/cos/monitor conf/server.xml");
		logger.info("\ttomcat目录\t完整的tomcat程序路径");
		logger.info("\t工作目录\t\ttomcat运行工作的路径");
		logger.info("\t配置文件路径\ttomcat运行所需要的server.xml配置文件路径");
		System.exit(1);
	}
}
