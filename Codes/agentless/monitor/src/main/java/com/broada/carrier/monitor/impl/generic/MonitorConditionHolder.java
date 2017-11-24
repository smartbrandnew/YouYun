package com.broada.carrier.monitor.impl.generic;

import com.broada.carrier.monitor.method.common.MonitorCondition;

public class MonitorConditionHolder extends MonitorCondition implements Cloneable {
	private static final long serialVersionUID = 8125033315112637998L;
	private String instance;
	private String columnName;
	private String unit;
	private int index;//显示所在的列索引
	private boolean hasInstance = true;

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public MonitorConditionHolder(String field, int type, String value) {
		super(field, type, value);
	}

	public MonitorConditionHolder() {
		super();
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean isHasInstance() {
		return hasInstance;
	}

	public void setHasInstance(boolean hasInstance) {
		this.hasInstance = hasInstance;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

}
