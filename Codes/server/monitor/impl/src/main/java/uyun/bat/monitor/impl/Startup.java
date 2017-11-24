package uyun.bat.monitor.impl;

import uyun.bat.common.spring.SpringStartup;

public class Startup extends SpringStartup {
	private static Startup instance;

	/**
	 * 获取默认单例
	 * @return 单例
	 */
	public static Startup getInstance() {
		if (instance == null) {
			synchronized (Startup.class) {
				if (instance == null)
					instance = new Startup();
			}
		}
		return instance;
	}

	public Startup() {
		super("Monitor", "classpath:uyun/bat/monitor/impl/spring.xml");
	}

	public static void main(String[] args) {
		getInstance().startup();
	}
}