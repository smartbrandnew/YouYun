package com.broada.carrier.monitor.method.common;

import java.io.Serializable;

public class MonitorCondition implements Serializable {
	private static final long serialVersionUID = 1L;
	protected String field;
	protected int type;
	protected Object value;

	public MonitorCondition() {
	}

	public MonitorCondition(String field, Object value) {
		this.field = field;
		this.value = value;
	}

	public MonitorCondition(String field, int type, Object value) {
		this.field = field;
		this.type = type;
		this.value = value;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	// TODO 删除
	public void setCurrValue(Object value) {
		setValue(value);
	}
}
