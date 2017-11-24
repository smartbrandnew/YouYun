package com.broada.carrier.monitor.impl.host.ipmi.sdk.core.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.EntityType;

/**
 * 配置文件读取类
 * 
 * @author pippo 
 * Create By 2013-6-27 下午2:15:43
 */
public class ConfigProperties {

	/*
	 * 工程配置文件路径
	 */
	public static final String LOG_CONFIG_FILE = System.getProperty(Constants.IPMI_EXTEND_PATH, System.getProperty("user.dir") + "/conf/ipmiExtendConfig.properties");

	private static Properties pro;

	static {
		pro = null;
		initIfNessary();
	}

	/*
	 * 根据配置文件初始化。
	 */
	private static void initIfNessary() {
		if (pro != null) {
			return;
		}

		FileInputStream fis = null;
		File confFile = new File(LOG_CONFIG_FILE);

		try {
			fis = new FileInputStream(confFile);
		} catch (FileNotFoundException ex) {
			throw new RuntimeException("指定的配置文件[ " + LOG_CONFIG_FILE + " ]不存在。");
		}

		pro = new Properties();
		try {
			pro.load(fis);
		} catch (IOException e) {
			throw new RuntimeException("读取配置文件时出错，文件路径：" + LOG_CONFIG_FILE, e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static void loadEntityType() {
		int process =  getPropertyInteger(Constants.ENTITY_PROCESS_CODE, 3);
		EntityType.PROCE.setValue(process);
		int board =  getPropertyInteger(Constants.ENTITY_BOARD_CODE, 7);
		EntityType.BOARD.setValue(board);
		int memory =  getPropertyInteger(Constants.ENTITY_MEMORY_CODE, 8);
		EntityType.MEMORY.setValue(memory);
		int power =  getPropertyInteger(Constants.ENTITY_POWER_CODE, 10);
		EntityType.POWER.setValue(power);
	}

	/**
	 * 更改配置文件属性
	 * 
	 * @param key 属性
	 * @param value 值
	 */
	public static void fillProperties(String key, String value) {
		if (pro == null) {
			initIfNessary();
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(LOG_CONFIG_FILE);
			pro.setProperty(key, value);
			pro.store(fOut, null);
		} catch (FileNotFoundException e) {
			new RuntimeException(String.format("指定的配置文件【%s】不存在。", LOG_CONFIG_FILE));
		} catch (IOException e) {
			throw new RuntimeException(String.format("配置文件【%s】写入错误。", LOG_CONFIG_FILE));
		} finally {
			if (fOut != null) {
				try {
					fOut.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * 
	 * @param key 键
	 * @return 值
	 * @throws RuntimeException 当读取错误时抛出
	 */
	public static String getProperty(String key) throws RuntimeException {
		initIfNessary();

		String value = pro.getProperty(key);
		return value;
	}
	
	public static boolean getPropertyBoolean(String key) throws RuntimeException {
		initIfNessary();

		String value = pro.getProperty(key);
		if (Util.isEmpty(value)) {
			throw new RuntimeException("找不到对应的键：" + key + "，或它的对应值是空的。");
		}
		boolean bool = Boolean.valueOf(value);
		return bool;
	}

	public static int getPropertyInteger(String key) throws RuntimeException, NumberFormatException {
		initIfNessary();

		String value = pro.getProperty(key);
		if (Util.isEmpty(value)) {
			throw new RuntimeException("找不到对应的键：" + key + "，或它的对应值是空的。");
		}
		try {
			int intValue = Integer.parseInt(value);
			return intValue;
		} catch (NumberFormatException e) {
			throw e;
		}
	}

	public static long getPropertyLong(String key) throws RuntimeException, NumberFormatException {
		initIfNessary();

		String value = pro.getProperty(key);
		if (Util.isEmpty(value)) {
			throw new RuntimeException("找不到对应的键：" + key + "，或它的对应值是空的。");
		}
		try {
			long longValue = Long.parseLong(value);
			return longValue;
		} catch (NumberFormatException e) {
			throw e;
		}
	}

	/**
	 * 读取配置文件值
	 * 
	 * @param key 键
	 * @param defaultValue 默认值
	 * @return 值
	 */
	public static String getProperty(String key, String defaultValue) {
		initIfNessary();
		
		String value = pro.getProperty(key);
		if (Util.isEmpty(value)) {
			value = defaultValue;
		}
		return value;
	}

	public static int getPropertyInteger(String key, int defaultValue) {
		initIfNessary();

		String value = pro.getProperty(key);
		if (Util.isEmpty(value)) {
			return defaultValue;
		}
		try {
			int intValue = Integer.parseInt(value);
			return intValue;
		} catch (NumberFormatException e) {
			// TODO: print error
			e.printStackTrace();
			return defaultValue;
		}
	}

	public static long getPropertyLong(String key, long defaultValue) {
		initIfNessary();

		String value = pro.getProperty(key);
		if (Util.isEmpty(value)) {
			return defaultValue;
		}
		try {
			long intValue = Long.parseLong(value);
			return intValue;
		} catch (NumberFormatException e) {
			// TODO: print error
			e.printStackTrace();
			return defaultValue;
		}
	}

}
