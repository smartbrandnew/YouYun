package com.broada.carrier.monitor.impl.storage.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
/**
 * 监测结果集，存储性能，属性，状态
 * @author ly
 *
 */
public class Result {
	private String key;
	private String clazz;
	private Map<String, Object> rs = new HashMap<String, Object>();
	private Map<String, Object> attr = new HashMap<String, Object>();
	private Perf perf = new Perf();
	private Map<String, Object> state = new HashMap<String, Object>();
	
	/**
	 * 监测结果优先级
	 */
	private int priority;

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public boolean isContinue() {
		return priority > 0;
	}
	
	public Map<String, Object> getAttr() {
		return attr;
	}
	public Perf getPerf() {
		return perf;
	}
	public Map<String, Object> getState() {
		return state;
	}
	
	public String getKey() {
		return key;
	}

	public String getClazz() {
		return clazz;
	}

	public Map<String, Object> getRs() {
		return rs;
	}

	public Result(String key) {
		this.key = key;
	}
	
	public Result(String key, String clazz) {
		this.key = key;
		this.clazz = clazz;
	}
	
	/**
	 * 监测模块对性能，属性，状态模块的模式要求
	 * class='DiskArray'
	 * rs.CompentOf='node'
	 * attr.ipAddr='192.168.16.22'
	 * attr.verder='IBM'
	 * attr.model='ibm v3700'
	 * perf.cpu_use.cpu_usage=35.5
	 * state.available_status=1
	 * @return
	 */
	public Map<String, Object> getAllMonitorItem() {
		Map<String, Object> prop = new HashMap<String, Object>();
		
		if (StringUtils.isNotBlank(this.clazz)) {
			prop.put("class", clazz);
			for (Entry<String, Object> entry : getRs().entrySet()) {
				String key = "rs." + entry.getKey();
				prop.put(key, entry.getValue());
			}
		}
		
		//遍历所有属性
		for (Entry<String, Object> entry : getAttr().entrySet()) {
			String key = "attr." + entry.getKey();
			prop.put(key, entry.getValue());
		}
		
		for (Entry<String, Object> entry : getState().entrySet()) {
			String key = "state." + entry.getKey();
			prop.put(key, entry.getValue());
		}
		
		for (Entry<String, Serializable> entry : getPerf().getProperties().entrySet()) {
			String key = "perf." + entry.getKey();
			Serializable indictor = entry.getValue();
			if (indictor instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> perfMap = (Map<String, Object>) indictor;
				for (Entry<String, ?> perfEntry : perfMap.entrySet()) {
					prop.put(key + "." + perfEntry.getKey(), perfEntry.getValue());
				}
			} 
		}
		
		return prop;
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Map<String, Object> allMonitorItem = getAllMonitorItem();
		for (Map.Entry<String, Object> entry : allMonitorItem.entrySet()) {
			sb.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
		}
		return sb.toString();
	}
}

/**
 * 临时存放性能链式指标
 * @author ly
 *
 */
class Perf {
	private Map<String, Serializable> properties = new LinkedHashMap<String, Serializable>();
	public void set(String name, Serializable value) {
		this.properties.put(name.toLowerCase(), new LinkedHashMap<String, Serializable>());
	}
	
	public Serializable get(String name) {
		name = name.toLowerCase();
		if (this.properties.containsKey(name)) {
			return this.properties.get(name);
		} 
		this.properties.put(name, new LinkedHashMap<String, Serializable>());
		return this.properties.get(name); 
	}

	public Map<String, Serializable> getProperties() {
		return properties;
	}
	
	
}
