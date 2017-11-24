package com.broada.carrier.monitor.server.api.client.restful.entity;

import com.broada.carrier.monitor.common.util.Base64Util;

public class ExecuteMethodRequest {
	private String className;
	private String methodName;
	private Object[] params = new Object[0];
	
	public ExecuteMethodRequest() {		
	}

	public ExecuteMethodRequest(String className, String methodName, Object[] params) {
		super();
		this.className = className;
		this.methodName = methodName;
		this.params = params;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public String getParams() {
		if (params == null || params.length == 0)
			return null;
		return Base64Util.encodeObject(params);
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public void setParams(String params) {
		if (params == null || params.isEmpty()) {
			this.params = new Object[0];
			return;
		}
		this.params = (Object[]) Base64Util.decodeObject(params);
	}

	public Object[] retParams() {
		return params;
	}
}
