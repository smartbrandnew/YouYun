package uyun.bat.common.proxy;

import org.springframework.context.ApplicationContext;

/**
 * 给{@link ProxyFactory}生产对应的proxy
 */
public interface Factory {
	/**
	 * 给{@link ProxyFactory}生产对应的proxy
	 * @param context
	 * @param isDeveloperMode
	 */
	void produce(ApplicationContext context, boolean isDeveloperMode);
}
