package com.broada.carrier.monitor.common.restful;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 自定义序列化类的Bean属性
 */
public class CustomBeanField {
	private String name;
	private Method method;
	
	/**
	 * 构建函数
	 * @param fieldName
	 * @param method
	 */
	public CustomBeanField(String name, Method method) {
		this.name = name;
		this.method = method;
	}

	/**
	 * 字段名称
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 获取指定对象的字段值
	 * @param entity
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public Object getValue(Object entity) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return method.invoke(entity);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}	
}
