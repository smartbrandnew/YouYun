package com.broada.carrier.monitor.impl.generic;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.broada.numen.agent.script.entity.DynamicParam;
import com.broada.numen.agent.script.entity.Parameter;

/**
 * 扩展参数类
 * 
 * @author
 *
 */
public class ExtParameter implements Serializable, Cloneable {
	private static final long serialVersionUID = 1308688956835398430L;
	private Map<String, DynamicParam> params = new LinkedHashMap<String, DynamicParam>();

	/** 脚本路径 */
	private String scriptFilePath = "";
	private int timeout = Parameter.TIMEOUT_DEFAULT;

	public ExtParameter() {
		super();
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public String getScriptFilePath() {
		return scriptFilePath;
	}

	public void setScriptFilePath(String scriptFilePath) {
		this.scriptFilePath = scriptFilePath;
	}

	public DynamicParam[] getParams() {
		return params.values().toArray(new DynamicParam[0]);
	}

	public void putDynamaicParam(DynamicParam param) {
		params.put(param.getCode(), param);
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	public Parameter createParameter() {
		Parameter param = new Parameter();
		param.setTimeout(getTimeout());
		for (DynamicParam dp : params.values())
			param.set(dp.getCode(), (Serializable) dp.getValue());
		return param;
	}

	public void setParams(DynamicParam[] params) {
		this.params.clear();
		for (DynamicParam param : params)
			this.params.put(param.getCode(), param);
	}
}
