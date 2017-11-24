package com.broada.carrier.monitor.common.config;

import java.util.Map.Entry;


/**
 * 配置读取类
 * @author Jiangjw
 */
public class BaseConfig {
	private static String workDir;
	private SimpleProperties props;
	
	/**
	 * 构建一个配置文件读取实体，默认使用${user.dir}/conf/config.properties文件。
	 */
	public BaseConfig() {
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			props = new SimpleProperties(System.getProperty("monitor.config", getConfDir() + "/config.properties"));
			props.set(entry.getKey().toString(), entry.getValue().toString());
		}
	}

	/**
	 * 获取工作路径
	 * @return
	 */
	public static String getWorkDir() {
		if (workDir == null)
			workDir = System.getProperty("user.dir"); 
		return workDir;
	}
	
	public static void setWorkDir(String workDir) {
		BaseConfig.workDir = workDir;
	}

	/**
	 * 获取配置文件路径
	 * @return
	 */
	public static String getConfDir() {
		return getWorkDir() + "/conf";
	}
	
	/**
	 * 获取临时文件路径
	 * @return
	 */
	public static String getTempDir() {
		return getWorkDir() + "/temp";
	}

	/**
	 * 配置属性集合
	 * @return
	 */
	public SimpleProperties getProps() {
		return props;
	}
}
