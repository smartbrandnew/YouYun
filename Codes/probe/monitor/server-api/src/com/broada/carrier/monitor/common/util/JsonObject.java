package com.broada.carrier.monitor.common.util;

import java.io.Serializable;

public class JsonObject implements Serializable {
	private static final String PREFIX_JSON = "#--json--\n";
	private static final long serialVersionUID = 1L;
	private String json;

	public void decode(String json) {
		this.json = json;
	}

	public String encode() {
		return json;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> cls) {
		if (json == null)
			return null;
		else if (json.startsWith(PREFIX_JSON))
			return SerializeUtil.decodeJson(json.substring(PREFIX_JSON.length()), cls);
		else
			return (T) json;
	}

	public void set(Object value) {
		if (value == null)
			this.json = null;
		else if (value instanceof String)
			this.json = (String) value;
		else
			this.json = PREFIX_JSON + SerializeUtil.encodeJson(value);
	}
}
