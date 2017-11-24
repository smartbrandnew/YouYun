package com.broada.carrier.monitor.probe.impl.yaml;

import java.util.HashMap;
import java.util.Map;

public class YamlDynamicMap {
	private String scriptPath;
	private Map<String, Object> properties = new HashMap<String, Object>();

	public String getScriptPath() {
		return scriptPath;
	}

	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "DynamicMap [scriptPath=" + scriptPath + ", properties=" + properties + "]";
	}
}
