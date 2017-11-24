package com.broada.carrier.monitor.common.util;

import java.io.Serializable;

public class Base64Object implements Serializable {
	private static final long serialVersionUID = 1L;
	private String base64;
	
	public Base64Object() {		
	}
	
	public Base64Object(Base64Object copy) {
		this.base64 = copy.base64;
	}
	
	public Base64Object(Object data) {
		set(data);
	}

	public void decode(String base64) {
		this.base64 = base64;
	}

	public String encode() {
		return base64;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> paramClass) {
		return (T)get();		
	}

	public Object get() {
		if (base64 == null)
			return null;
		else if (base64.startsWith(Base64Util.PREFIX_BASE64))
			return Base64Util.decodeObject(base64.substring(Base64Util.PREFIX_BASE64.length()));
		else
			return base64;
	}

	public void set(Object value) {
		if (value == null) 
			this.base64 = null;
		else if (value instanceof String)
			this.base64 = (String) value;
		else
			this.base64 = Base64Util.PREFIX_BASE64 + Base64Util.encodeObject(value);
	}
}
