package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PerfResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private String instKey;
	private String itemCode;
	private Object value;

	public PerfResult() {
	}

	public PerfResult(String itemCode) {
		this(null, itemCode, null);
	}

	public PerfResult(String itemCode, boolean value) {
		this(null, itemCode, null);
	}

	public PerfResult(String itemCode, Object value) {
		this(null, itemCode, value);
	}

	public PerfResult(String instKey, String itemCode, Object value) {
		this.instKey = instKey;
		this.itemCode = itemCode;
		this.value = value;		
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getInstKey() {
		return instKey;
	}

	public void setInstKey(String instKey) {
		this.instKey = instKey;
	}

	@Override
	public String toString() {
		return String.format("%s[instKey: %s itemCode: %s value: %s", getClass().getSimpleName(),
				getInstKey(), getItemCode(), getValue());
	}

	@JsonIgnore
	public void setInstanceKey(String field) {
		setInstKey(field);
	}

	public void setStrValue(String value) {
		this.value = value;
	}
}
