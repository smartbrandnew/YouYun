package com.broada.carrier.monitor.impl.storage.netapp.cpuidletime;

public class CPUIDletime {
  private Boolean isWacthed = Boolean.FALSE;

  private String monitorInst = null;//���ʵ��

  private String monitorName = null;//������

  private Double currentValue = null;//��ǰֵ

  private Double vavel = null; //��ֵ

  private String expression = null;//���ʽ

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
