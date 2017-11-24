package com.broada.carrier.monitor.impl.host.cli.process;

import com.broada.carrier.monitor.method.common.MonitorCondition;

public class CLIProcessMonitorCondition extends MonitorCondition {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -3468797979078050045L;

  private Boolean select = Boolean.FALSE;

  private boolean validate = true;

  /*
   * 内存使用率
   */
  private String memory;

  private String currmemory;

  private String memoryUtil;

  private String currMemoryUtil;

  /*
   * CPU使用率
   */
  private String cpu;

  private String currcpu;

  private String status;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
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

  public String getUnit() {
    return "%";
  }

  public void setField(String field) {
    super.setField(field);
  }

  public String getCpu() {
    return cpu;
  }

  public void setCpu(String cpu) {
    this.cpu = cpu;
  }

  public String getCurrcpu() {
    return currcpu;
  }

  public void setCurrcpu(String currcpu) {
    this.currcpu = currcpu;
  }

  public String getCurrmemory() {
    return currmemory;
  }

  public void setCurrmemory(String currmemory) {
    this.currmemory = currmemory;
  }

  public String getMemory() {
    return memory;
  }

  public void setMemory(String memory) {
    this.memory = memory;
  }

  public String getMemoryUtil() {
    return memoryUtil;
  }

  public void setMemoryUtil(String memoryUtil) {
    this.memoryUtil = memoryUtil;
  }

  public String getCurrMemoryUtil() {
    return currMemoryUtil;
  }

  public void setCurrMemoryUtil(String currMemoryUtil) {
    this.currMemoryUtil = currMemoryUtil;
  }

  public String getFieldCondition() {
    return "的内存使用量和使用率";
  }
}
