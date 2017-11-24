package com.broada.carrier.monitor.impl.mw.websphere.entity.ui;

/**
 * @author lixy Sep 17, 2008 10:14:02 AM
 */
public class PerfItem {
  private String itemCode;
  private String name;
  private boolean showPerf;
  private boolean showCondition;
  private String conditionName;
  private int type;
  private int defaultCondValue;

  public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}

	public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isShowPerf() {
    return showPerf;
  }

  public void setShowPerf(boolean showPerf) {
    this.showPerf = showPerf;
  }

  public boolean isShowCondition() {
    return showCondition;
  }

  public void setShowCondition(boolean showCondition) {
    this.showCondition = showCondition;
  }

  public String getConditionName() {
    return conditionName;
  }

  public void setConditionName(String conditionName) {
    this.conditionName = conditionName;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getDefaultCondValue() {
    return defaultCondValue;
  }

  public void setDefaultCondValue(int defaultCondValue) {
    this.defaultCondValue = defaultCondValue;
  }
}
