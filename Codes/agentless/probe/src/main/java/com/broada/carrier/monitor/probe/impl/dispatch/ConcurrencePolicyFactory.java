package com.broada.carrier.monitor.probe.impl.dispatch;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.probe.impl.config.Config;

/**
 * 并发策略工厂
 */
public class ConcurrencePolicyFactory {
	private static final Log logger = LogFactory.getLog(ConcurrencePolicyFactory.class);
	private static ConcurrencePolicyFactory instance;
	private ConcurrencePolicy defaultPolicy;
	private Map<String, ConcurrencePolicy> policies = new HashMap<String, ConcurrencePolicy>();

	/**
	 * 获取默认实例
	 * @return
	 */
	public static ConcurrencePolicyFactory getDefault() {
		if (instance == null) {
			synchronized (ConcurrencePolicyFactory.class) {
				if (instance == null)
					instance = new ConcurrencePolicyFactory();
			}
		}
		return instance;
	}
	
	/**
	 * 默认构造函数，从probe-conf.properties中加载并发策略
	 */
	public ConcurrencePolicyFactory() {
		try {			
			String value = Config.getDefault().getMonitorPolicy();
			String[] items = value.split(";");			
			for (String item : items) {
				ConcurrencePolicy policy = ConcurrencePolicy.parse(item);
				if (policy.getId().equalsIgnoreCase("default"))
					defaultPolicy = policy;
				else
					policies.put(policy.getId(), policy);
			}
		} catch (Throwable e) {
			logger.warn(String.format("读取配置项[monitor.policy]失败，将使用默认配置。错误：%s", e));
			logger.debug("堆栈：", e);
			defaultPolicy = ConcurrencePolicy.DEFAULT_POLICY;
		}
	}

	/**
	 * 获取一个监测任务的策略，此返回不可能返回null
	 * @param typeId
	 * @return
	 */
	public ConcurrencePolicy check(String typeId) {
		ConcurrencePolicy result = policies.get(typeId);
		if (result == null)
			return defaultPolicy;
		else
			return result;
	}
}
