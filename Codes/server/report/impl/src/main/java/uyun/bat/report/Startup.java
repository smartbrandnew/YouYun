package uyun.bat.report;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.common.spring.SpringStartup;

public class Startup extends SpringStartup {
	private static Logger logger = LoggerFactory.getLogger(Startup.class);
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
		super("Report", "classpath:uyun/bat/report/impl/spring.xml");
	}

	public static void main(String[] args) {
		getInstance().startup();
	}
}
