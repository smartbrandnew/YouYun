package com.broada.carrier.monitor.common.entity;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.text.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 基础动态对象工具类，为了其它子类方式实现DynamicObject接口，实现了DynamicObject大部份方法
 * @author Jiangjw
 */
public abstract class BaseDynamicObject implements DynamicObject {
	private static Map<String, String> methodPropertyMap = new ConcurrentHashMap<String, String>();	
	private static final Logger logger = LoggerFactory.getLogger(BaseDynamicObject.class);
	
	@Override
	public Date get(String key, Date defaultValue) {
		Object value = get(key);
		if (value == null)
			return defaultValue;
		else if (value instanceof Date)
			return (Date) value;
		else {
			try {
				return DateUtil.parse(value.toString());
			} catch (Throwable e) {
				warn(key, value, defaultValue, e);
				return defaultValue;
			}
		}
	}

	@Override
	public Date checkDate(String key) {
		Object value = check(key);
		if (value instanceof Date)
			return (Date) value;
		else {
			try {
				return DateUtil.parse(value.toString());
			} catch (Throwable e) {
				throw new IllegalArgumentException(String.format("属性[%s:%s]无法转换为[%s]", key, value, Date.class));
			}
		}
	}

	@Override
	public Object check(String key) {
		Object value = get(key);
		if (value == null)
			throw new IllegalArgumentException("不存在的属性：" + key);
		return value;
	}

	@Override
	public String checkString(String key) {
		return check(key).toString();
	}

	@Override
	public int checkInteger(String key) {
		Object value = check(key);
		if (value instanceof Number)
			return ((Number) value).intValue();
		else {
			try {
				return Integer.parseInt(value.toString());
			} catch (Throwable e) {
				throw new IllegalArgumentException(String.format("属性[%s:%s]无法转换为[%s]", key, value, Integer.class));
			}
		}
	}
	
	@Override
	public long checkLong(String key) {
		Object value = check(key);
		if (value instanceof Number)
			return ((Number) value).longValue();
		else {
			try {
				return Long.parseLong(value.toString());
			} catch (Throwable e) {
				throw new IllegalArgumentException(String.format("属性[%s:%s]无法转换为[%s]", key, value, Long.class));
			}
		}
	}	

	@Override
	public double checkDouble(String key) {
		Object value = check(key);
		if (value instanceof Number)
			return ((Number) value).doubleValue();
		else {
			try {
				return Double.parseDouble(value.toString());
			} catch (Throwable e) {
				throw new IllegalArgumentException(String.format("属性[%s:%s]无法转换为[%s]", key, value, Double.class));
			}
		}
	}

	@Override
	public boolean checkBoolean(String key) {
		Object value = check(key);
		if (value instanceof Boolean)
			return (Boolean) value;
		else {
			try {
				return Boolean.parseBoolean(value.toString());
			} catch (Throwable e) {
				throw new IllegalArgumentException(String.format("属性[%s:%s]无法转换为[%s]", key, value, Boolean.class));
			}
		}
	}

	@Override
	public int get(String key, int defaultValue) {
		Object value = get(key);
		if (value == null)
			return defaultValue;
		else if (value instanceof Number)
			return ((Number) value).intValue();
		else {
			try {
				return Integer.parseInt(value.toString());
			} catch (Throwable e) {
				warn(key, value, defaultValue, e);
				return defaultValue;
			}
		}
	}
	
	@Override
	public long get(String key, long defaultValue) {
		Object value = get(key);
		if (value == null)
			return defaultValue;
		else if (value instanceof Number)
			return ((Number) value).longValue();
		else {
			try {
				return Long.parseLong(value.toString());
			} catch (Throwable e) {
				warn(key, value, defaultValue, e);
				return defaultValue;
			}
		}
	}

	@Override
	public double get(String key, double defaultValue) {
		Object value = get(key);
		if (value == null)
			return defaultValue;
		else if (value instanceof Number)
			return ((Number) value).doubleValue();
		else {
			try {
				return Double.parseDouble(value.toString());
			} catch (Throwable e) {
				warn(key, value, defaultValue, e);
				return defaultValue;
			}
		}
	}

	@Override
	public String get(String key, String defaultValue) {
		Object value = get(key);
		if (value == null)
			return defaultValue;
		else
			return value.toString();
	}

	@Override
	public boolean get(String key, boolean defaultValue) {
		Object value = get(key);
		if (value == null)
			return defaultValue;
		else if (value instanceof Boolean)
			return (Boolean) value;
		else {
			try {
				return Boolean.parseBoolean(value.toString());
			} catch (Throwable e) {
				warn(key, value, defaultValue, e);
				return defaultValue;
			}
		}
	}
	
	/**
	 * 使用调用者的JavaBean方法名作为属性key，来返回字符串值，如果值不存在或转换错误，则返回defaultValue
	 * @param defaultValue
	 * @return
	 */
	public String getByMethod(String defaultValue) {
		String property = getMethodProperty();
		return get(property, defaultValue);
	}
	
	/**
	 * 使用调用者的JavaBean方法名作为属性key，来返回整型值，如果值不存在或转换错误，则返回defaultValue
	 * @param defaultValue
	 * @return
	 */
	public int getByMethod(int defaultValue) {
		String property = getMethodProperty();
		return get(property, defaultValue);
	}
	
	/**
	 * 使用调用者的JavaBean方法名作为属性key，来返回日期值，如果值不存在或转换错误，则返回defaultValue
	 * @param defaultValue
	 * @return
	 */
	public Date getByMethod(Date defaultValue) {
		String property = getMethodProperty();
		return get(property, defaultValue);
	}
	
	/**
	 * 使用调用者的JavaBean方法名作为属性key，来返回布尔值，如果值不存在或转换错误，则返回defaultValue
	 * @param defaultValue
	 * @return
	 */
	public boolean getByMethod(boolean defaultValue) {
		String property = getMethodProperty();
		return get(property, defaultValue);
	}
	
	/**
	 * 使用调用者的JavaBean方法名作为属性key，来返回浮点值，如果值不存在或转换错误，则返回defaultValue
	 * @param defaultValue
	 * @return
	 */
	public double getByMethod(double defaultValue) {
		String property = getMethodProperty();
		return get(property, defaultValue);
	}
	
	/**
	 * 使用调用者的JavaBean方法名作为属性key，来返回属性值
	 * @return
	 */
	@JsonIgnore
	public Object getByMethod() {
		String property = getMethodProperty();
		return get(property);
	}

	/**
	 * 使用调用者的JavaBean方法名作为属性key，来设置属性值
	 * @param value
	 */
	@JsonIgnore
	public void setByMethod(Object value) {
		String property = getMethodProperty();
		set(property, value);
	}
	
	private static String getMethodProperty() {
		String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
		String property = methodPropertyMap.get(methodName);
		if (property == null) {
			int start;
			if (methodName.startsWith("get")
					|| methodName.startsWith("set"))
				start = 3;
			else if (methodName.startsWith("is"))
				start = 2;
			else
				throw new IllegalArgumentException(methodName + "不是一个有效的java bean方法");
			int end = start;
			for (int i = start; i < methodName.length(); i++) {
				if (!Character.isUpperCase(methodName.charAt(i))) {
					end = i;
					break;
				}
			}
			if (end == start)
				property = methodName.substring(start);
			else
				property = methodName.substring(start, end).toLowerCase() + methodName.substring(end);
			methodPropertyMap.put(methodName, property);
		}
		return property;
	}

	private void warn(String key, Object value, Object defaultValue, Throwable e) {
		ErrorUtil.warn(logger, String.format("对象[%s]转换属性[%s]值[%s]失败，将使用默认值[%s]", this, key, value, defaultValue), e);
	}
}
