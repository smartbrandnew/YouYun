package com.broada.carrier.monitor.impl.db.sybase.segment;

public class SybaseSegment {
  private String name;

  private Double totalSize;

  private Double segData;

  private Double segIndex;

  private Double segUnused;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getSegData() {
    return segData;
  }

  public void setSegData(Double segData) {
    this.segData = segData;
  }

  public Double getSegIndex() {
    return segIndex;
  }

  public void setSegIndex(Double segIndex) {
    this.segIndex = segIndex;
  }

  public Double getSegUnused() {
    return segUnused;
  }

  public void setSegUnused(Double segUnused) {
    this.segUnused = segUnused;
  }

  public Double getTotalSize() {
    return totalSize;
  }

  public void setTotalSize(Double totalSize) {
    this.totalSize = totalSize;
  }
}
