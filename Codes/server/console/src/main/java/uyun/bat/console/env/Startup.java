package uyun.bat.console.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.spring.SpringStartup;
import uyun.bat.console.db.DBInit;
import uyun.whale.common.util.error.ErrorUtil;

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
		super("Console", "classpath:uyun/bat/console/spring.xml");
	}

	@Override
	public synchronized void startup() {
		super.startup();

		try {
			DBInit.getInstance().initDB();
		} catch (Throwable e) {
			ErrorUtil.exit(logger, "Check database version failed", e);
		}
	}
	
	public static void main(String[] args) {
		getInstance().startup();
	}
}
