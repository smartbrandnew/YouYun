package uyun.bat.datastore;

import uyun.bat.common.spring.SpringStartup;

import java.io.IOException;

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
		super("DataStore", "classpath:uyun/bat/datastore/spring.xml");
	}

	public static void main(String[] args) throws IOException {
		getInstance().startup();
	}
}
