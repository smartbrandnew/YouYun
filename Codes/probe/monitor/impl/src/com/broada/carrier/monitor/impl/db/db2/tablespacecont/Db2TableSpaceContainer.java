package com.broada.carrier.monitor.impl.db.db2.tablespacecont;

public class Db2TableSpaceContainer {
  private String tableSpaceName;

  private String containerName;

  private String containerType;

  private Double usablePages = new Double(0);

  private Double totalPages = new Double(0);

  public String getContainerName() {
    return containerName;
  }

  public void setContainerName(String containerName) {
    this.containerName = containerName;
  }

  public String getContainerType() {
    return containerType;
  }

  public void setContainerType(String containerType) {
    this.containerType = containerType;
  }

  public String getTableSpaceName() {
    return tableSpaceName;
  }

  public void setTableSpaceName(String tableSpaceName) {
    this.tableSpaceName = tableSpaceName;
  }

  public Double getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Double totalPages) {
    this.totalPages = totalPages;
  }

  public Double getUsablePages() {
    return usablePages;
  }

  public void setUsablePages(Double usablePages) {
    this.usablePages = usablePages;
  }

  public Double getUsableRate() {
    if(getTotalPages().doubleValue()!=0){
      return new Double(getUsablePages().doubleValue()*100/getTotalPages().doubleValue());
    }else{
      return new Double(0);
    }
  }
}
