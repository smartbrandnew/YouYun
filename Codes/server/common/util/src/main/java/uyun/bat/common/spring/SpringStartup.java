package uyun.bat.common.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 通过spring进行启动的基础类
 */
public class SpringStartup {
	private static final Logger logger = LoggerFactory.getLogger(SpringStartup.class);
	private String name;
	private String classPathXml;
	private ClassPathXmlApplicationContext context;

	/**
	 * 构建一个启动器
	 * @param name 应用启动名称，主要用于日志
	 * @param classPathXml spring.xml文件路径
	 */
	public SpringStartup(String name, String classPathXml) {
		this.name = name;
		this.classPathXml = classPathXml;
	}

	/**
	 * 启动
	 */
	public synchronized void startup() {
		if (isRunning())
			return;

		logger.info(name + " startup...");
		try {
			context = new ClassPathXmlApplicationContext(classPathXml);
		} catch (RuntimeException e) {
			logger.error(name + " startup error", e);
			throw e;
		}
	}

	/**
	 * 关闭
	 */
	public synchronized void shutdown() {
		if (!isRunning())
			return;

		context.destroy();
		logger.info(name + " shutdown.");
	}

	/**
	 * 获取指定类型的bean，如果bean不存在，会弹出异常
	 * @param cls 指定类型
	 * @param <T> 返回context中是此类型的bean
	 * @return bean
	 */
	public <T> T getBean(Class<T> cls) {
		init();
		return context.getBean(cls);
	}

	/**
	 * 获取指定名称的bean，如果bean不存在，会弹出异常
	 * @param name 指定名称
	 * @return bean
	 */
	public Object getBean(String name) {
		init();
		return context.getBean(name);
	}

	/**
	 * 确定启动器是否正在运行
	 * @return
	 */
	public boolean isRunning() {
		return context != null;
	}

	private void init() {
		if (!isRunning()) {
			synchronized (this) {
				if (!isRunning())
					startup();
			}
		}
	}

	/**
	 * 获取应用名称
	 * @return 应用名称
	 */
	public String getName() {
		return name;
	}
}
