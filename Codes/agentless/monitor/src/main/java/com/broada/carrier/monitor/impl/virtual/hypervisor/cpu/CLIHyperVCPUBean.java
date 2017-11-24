package com.broada.carrier.monitor.impl.virtual.hypervisor.cpu;

public class CLIHyperVCPUBean {
  private String name;

  private String caption;

  private String loadPercentage;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCaption() {
    return caption;
  }

  public void setCaption(String caption) {
    this.caption = caption;
  }

  public String getLoadPercentage() {
    return loadPercentage;
  }

  public void setLoadPercentage(String loadPercentage) {
    this.loadPercentage = loadPercentage;
  }
}
