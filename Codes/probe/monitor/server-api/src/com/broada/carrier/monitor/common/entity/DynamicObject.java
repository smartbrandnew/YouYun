package com.broada.carrier.monitor.common.entity;

import java.util.Date;

/**
 * 动态对象接口，通过提供各个不同数据类型的get与set方法，以实现动态属性获取
 * @author Jiangjw
 */
public interface DynamicObject {
	/**
	 * 获取一个属性，不管是何种数据类型
	 * @param key
	 * @return
	 */
	Object get(String key);
	
	/**
	 * 设置一个属性，不管是何种数据类型
	 * @param key
	 * @param value
	 */
	void set(String key, Object value);
	
	/**
	 * 获取一个整型属性
	 * @param key
	 * @param defaultValue
	 * @return 如果属性不存在，类型错误或转换失败，则返回defaultValue
	 */
	int get(String key, int defaultValue);
	
	/**
	 * 获取一个整型属性
	 * @param key
	 * @param defaultValue
	 * @return 如果属性不存在，类型错误或转换失败，则返回defaultValue
	 */
	long get(String key, long defaultValue);	
	
	/**
	 * 获取一个浮点属性
	 * @param key
	 * @param defaultValue
	 * @return 如果属性不存在，类型错误或转换失败，则返回defaultValue
	 */
	double get(String key, double defaultValue);
	
	/**
	 * 获取一个字符串属性
	 * @param key
	 * @param defaultValue
	 * @return 如果属性不存在，类型错误或转换失败，则返回defaultValue
	 */
	String get(String key, String defaultValue);
	
	/**
	 * 获取一个布尔属性
	 * @param key
	 * @param defaultValue
	 * @return 如果属性不存在，类型错误或转换失败，则返回defaultValue
	 */
	boolean get(String key, boolean defaultValue);
	
	/**
	 * 获取一个日期属性
	 * @param key
	 * @param defaultValue
	 * @return 如果属性不存在，类型错误或转换失败，则返回defaultValue
	 */
	Date get(String key, Date defaultValue);

	/**
	 * 移除一个属性
	 * @param key
	 */
	void remove(String key);	
	
	/**
	 * 获取一个属性，如果属性不存在则弹出异常
	 * @param key
	 * @return
	 * @throws IllegalArgumentException
	 */
	Object check(String key) throws IllegalArgumentException;
	
	/**
	 * 获取一个字符串属性，如果属性不存在或转换失败则弹出异常
	 * @param key
	 * @return
	 * @throws IllegalArgumentException
	 */
	String checkString(String key) throws IllegalArgumentException;

	/**
	 * 获取一个整型属性，如果属性不存在或转换失败则弹出异常
	 * @param key
	 * @return
	 * @throws IllegalArgumentException
	 */
	int checkInteger(String key) throws IllegalArgumentException;
	
	/**
	 * 获取一个长整型属性，如果属性不存在或转换失败则弹出异常
	 * @param key
	 * @return
	 * @throws IllegalArgumentException
	 */
	long checkLong(String key) throws IllegalArgumentException;
		
	/**
	 * 获取一个浮点属性，如果属性不存在或转换失败则弹出异常
	 * @param key
	 * @return
	 * @throws IllegalArgumentException
	 */
	double checkDouble(String key) throws IllegalArgumentException;
	
	/**
	 * 获取一个布尔属性，如果属性不存在或转换失败则弹出异常
	 * @param key
	 * @return
	 * @throws IllegalArgumentException
	 */
	boolean checkBoolean(String key) throws IllegalArgumentException;
	
	/**
	 * 获取一个日期属性，如果属性不存在或转换失败则弹出异常
	 * @param key
	 * @return
	 * @throws IllegalArgumentException
	 */
	Date checkDate(String key) throws IllegalArgumentException;
}
