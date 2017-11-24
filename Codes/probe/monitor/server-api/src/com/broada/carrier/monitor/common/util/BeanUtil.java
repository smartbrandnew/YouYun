package com.broada.carrier.monitor.common.util;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.broada.component.utils.error.ErrorUtil;

public class BeanUtil {
	private static Map<Class<?>, Map<String, Method>> classMethods = new ConcurrentHashMap<Class<?>, Map<String, Method>>();
	private static Method nullMethod = BeanUtil.class.getMethods()[0];
	
	public static Object checkPropertyValue(Object bean, String property) {		
		return getPropertyValueMulti(bean, property, true);
	}
	
	private static Object getPropertyValueSingle(Object bean, String property, boolean throwIfNoMethod) {
		Method method = getMethod(bean.getClass(), property);
		if (method == null) {
			if (throwIfNoMethod)
				throw new IllegalArgumentException(String.format("Bean[%s]不存在属性[%s]", bean, property));
			else
				return null;
		}
		try {
			return method.invoke(bean);
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException(String.format("Bean[%s]的属性[%s]获取失败", bean, property), e);
		}
	}
	
	private static Object getPropertyValueMulti(Object bean, String property, boolean throwIfNoMethod) {
		int pos = property.indexOf(".");
		if (pos > 0) {
			String[] props = property.split("\\.");
			for (String prop : props) {
				bean = getPropertyValueSingle(bean, prop, throwIfNoMethod);
				if (bean == null)
					return null;
			}
			return bean;
		} else
			return getPropertyValueSingle(bean, property, throwIfNoMethod);
	}
	
	public static Object getPropertyValue(Object bean, String property) {
		return getPropertyValueMulti(bean, property, false);
	}
	
	private static Method getMethod(Class<?> cls, String code) {
		Map<String, Method> methods = classMethods.get(cls);
		if (methods == null) {
			methods = new ConcurrentHashMap<String, Method>();
			classMethods.put(cls, methods);
		}
		
		Method method = methods.get(code);
		if (method == nullMethod)
			return null;		
		else if (method == null) {
			String match1 = "get" + code;
			String match2 = "is" + code;
			String match3 = "ret" + code;			
			method = nullMethod;
			for (Method item : cls.getMethods()) {
				if (item.getParameterTypes().length > 0)
					continue;
				if (item.getName().equalsIgnoreCase(match1)
						|| item.getName().equalsIgnoreCase(match2)
						|| item.getName().equalsIgnoreCase(match3)) {
					method = item;
					break;
				}
			}						
			methods.put(code, method);
			if (method == nullMethod)
				return null;
		}		
		return method;
	}
}
