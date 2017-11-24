package com.broada.carrier.monitor.common.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 通过LinkedHashMap实现的动态属性对象
 * @author Jiangjw
 */
public class DefaultDynamicObject extends BaseDynamicObject implements Map<String, Object>, Serializable {
	private static final long serialVersionUID = 1L;
	private Map<String, Object> props;

	public DefaultDynamicObject() {
		props = new LinkedHashMap<String, Object>();
	}
	
	/**
	 * 使用指定的map，构建动态对象，并直接将properties作为其内部存储
	 * @param properties
	 */
	public DefaultDynamicObject(Map<String, Object> properties) {		
		set(properties);
	}

	/**
	 * 使用指定的map，构建动态对象，根据clone属性决定是否将properties作为其内部存储
	 * @param properties
	 * @param clone 如果为true，则只是复制properties里的值对，否则将直接使用properties作为其内部存储
	 */
	public DefaultDynamicObject(Map<String, Object> properties, boolean clone) {		
		set(properties, clone);
	}
	
	/**
	 * 设置属性集合，将直接将properties作为其内部存储
	 * @param properties
	 */
	public void set(Map<String, Object> properties) {
		set(properties, false);
	}

	/**
	 * 设置属性集合
	 * @param properties
	 * @param clone 如果为true，则只是复制properties里的值对，否则将直接使用properties作为其内部存储
	 */
	public void set(Map<String, Object> properties, boolean clone) {
		if (properties == null)
			properties = new LinkedHashMap<String, Object>();
		if (clone) {
			if (props == null)
				props = new LinkedHashMap<String, Object>();
			clear();
			putAll(properties);
		} else {
			if (this == properties)
				throw new IllegalArgumentException("设置对象不可以是自身");
			props = properties;
		}
	}

	@Override
	public Object get(String key) {
		return props.get(key);
	}

	@Override
	public void set(String key, Object value) {
		props.put(key, value);
	}

	@Override
	public void remove(String key) {
		props.remove(key);
	}

	@Override
	public int size() {
		return props.size();
	}

	@Override
	public boolean isEmpty() {
		return props.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return props.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return props.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return props.get(key);
	}

	@Override
	public Object put(String key, Object value) {
		if (value == this)
			throw new IllegalArgumentException("设置对象不可以是自身");
		return props.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return props.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		props.putAll(m);
	}

	@Override
	public void clear() {
		props.clear();
	}

	@Override
	public Set<String> keySet() {
		return props.keySet();
	}

	@Override
	public Collection<Object> values() {
		return props.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return props.entrySet();
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), props);
	}

	@Override
	public boolean equals(Object obj) {
		DefaultDynamicObject other = (DefaultDynamicObject)obj;
		return props.equals(other);
	}
}
