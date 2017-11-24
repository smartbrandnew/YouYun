package uyun.bat.common.proxy;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import uyun.bat.common.config.Config;

/**
 * 初始化proxyFactory
 */
public class ProxyBeanProcessor implements ApplicationContextAware {
	private List<Factory> factorys;

	public List<Factory> getFactorys() {
		return factorys;
	}

	public void setFactorys(List<Factory> factorys) {
		this.factorys = factorys;
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (factorys != null && factorys.size() > 0) {
			boolean isDeveloperMode = Config.getInstance().isDeveloperMode();
			for (Factory f : factorys) {
				f.produce(applicationContext, isDeveloperMode);
			}
		}
	}

}
