package com.broada.carrier.monitor.impl.storage.netapp.cpuinterrup;

public class CPUInterrup {
  private Boolean isWacthed = Boolean.FALSE;

  private String monitorInst = null;//监测实例

  private String monitorName = null;//监测名称

  private Double currentValue = null;//当前值

  private Double vavel = null; //阈值

  private String expression = null;//表达式

  public Double getCurrentValue() {
    return currentValue;
  }

  public void setCurrentValue(Double currentValue) {
    this.currentValue = currentValue;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public String getMonitorInst() {
    return monitorInst;
  }

  public void setMonitorIns(String monitorInst) {
    this.monitorInst = monitorInst;
  }

  public String getMonitorName() {
    return monitorName;
  }

  public void setMonitorName(String monitorName) {
    this.monitorName = monitorName;
  }

  public Double getVavel() {
    return vavel;
  }

  public void setVavel(Double vavel) {
    this.vavel = vavel;
  }
}
