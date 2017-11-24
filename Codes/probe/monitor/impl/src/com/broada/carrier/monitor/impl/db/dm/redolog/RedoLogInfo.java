package com.broada.carrier.monitor.impl.db.dm.redolog;

import com.broada.utils.Condition;

/**
 * DM redo日志
 * 
 * @author Zhouqa Create By 2016年4月8日 上午9:25:52
 */
public class RedoLogInfo {
	private Boolean isWacthed = Boolean.FALSE;

	private String name;

	private int itemIdx;

	private double currValue;

	private double thresholdValue = 100d;

	private int type = Condition.LESSTHAN;

	private String unit;
	// 该项是否在面板中显示
	private Boolean isShowInColumn = Boolean.TRUE;

	public Boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(Boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getItemIdx() {
		return itemIdx;
	}

	public void setItemIdx(int itemIdx) {
		this.itemIdx = itemIdx;
	}

	public double getCurrValue() {
		return currValue;
	}

	public void setCurrValue(double currValue) {
		this.currValue = currValue;
	}

	public double getThresholdValue() {
		return thresholdValue;
	}

	public void setThresholdValue(double thresholdValue) {
		this.thresholdValue = thresholdValue;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Boolean getIsShowInColumn() {
		return isShowInColumn;
	}

	public void setIsShowInColumn(Boolean isShowInColumn) {
		this.isShowInColumn = isShowInColumn;
	}

}
