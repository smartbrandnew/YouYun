package com.broada.carrier.monitor.impl.host.cli.io;

import com.broada.carrier.monitor.method.common.MonitorCondition;

public class CLIIOMonitorCondition extends MonitorCondition {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -3468797979078050045L;

  private Boolean select = Boolean.FALSE;

  private boolean validate = true;

  private float busy;
  
  private float avque;
  
  private float rwPerSecond;
  
  private float blksPerSecond;
  
  private float avwait;
  
  private float avserv;
  
  private float maxAvque = 20;
  
  private float maxAvserv = 20;

  public CLIIOMonitorCondition() {
  }

  public Boolean getSelect() {
    return select;
  }

  public void setSelect(Boolean select) {
    this.select = select;
  }

  public void setSelect(String select) {
    this.select = select.equalsIgnoreCase("true") ? Boolean.TRUE : Boolean.FALSE;
  }

  public boolean isValidate() {
    return validate;
  }

  public void setValidate(boolean validate) {
    this.validate = validate;
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof CLIIOMonitorCondition)) {
      return false;
    }
    return getField().equals(((CLIIOMonitorCondition) obj).getField());
  }

  public String getUnit() {
    return "%";
  }

  public String getComDescription() {
    return "";
  }

  public void setField(String field) {
    super.setField(field);
  }

  public String getFieldCondition() {
    return "的使用率";
  }

  public float getBusy() {
    return busy;
  }

  public void setBusy(float busy) {
    this.busy = busy;
  }

  public float getAvque() {
    return avque;
  }

  public void setAvque(float avque) {
    this.avque = avque;
  }

  public float getRwPerSecond() {
    return rwPerSecond;
  }

  public void setRwPerSecond(float rwPerSecond) {
    this.rwPerSecond = rwPerSecond;
  }

  public float getBlksPerSecond() {
    return blksPerSecond;
  }

  public void setBlksPerSecond(float blksPerSecond) {
    this.blksPerSecond = blksPerSecond;
  }

  public float getAvwait() {
    return avwait;
  }

  public void setAvwait(float avwait) {
    this.avwait = avwait;
  }

  public float getAvserv() {
    return avserv;
  }

  public void setAvserv(float avserv) {
    this.avserv = avserv;
  }

  public float getMaxAvque() {
    return maxAvque;
  }

  public void setMaxAvque(float maxAvque) {
    this.maxAvque = maxAvque;
  }

  public float getMaxAvserv() {
    return maxAvserv;
  }

  public void setMaxAvserv(float maxAvserv) {
    this.maxAvserv = maxAvserv;
  }
}
