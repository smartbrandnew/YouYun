package com.broada.carrier.monitor.impl.host.cli.directory.win;

import java.util.Date;

import com.broada.carrier.monitor.method.common.MonitorCondition;

public class CLIFile extends MonitorCondition {
  private static final long serialVersionUID = -1915513583478291245L;
  private String name = "";
  private Double size = new Double(0);
  private String filetype = "";
  private Date createTime = new Date();
  private Date modifyTime = new Date();
  private Boolean writeable = Boolean.TRUE;
  private Boolean hidden = Boolean.FALSE;
  private String owner = "";
  private String Status = "";

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getStatus() {
    return Status;
  }

  public void setStatus(String status) {
    Status = status;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getSize() {
    return size;
  }

  public void setSize(Double size) {
    this.size = size;
  }


  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public Date getModifyTime() {
    return modifyTime;
  }

  public void setModifyTime(Date modifyTime) {
    this.modifyTime = modifyTime;
  }

  public Boolean getWriteable() {
    return writeable;
  }

  public void setWriteable(Boolean writeable) {
    this.writeable = writeable;
  }

  public Boolean getHidden() {
    return hidden;
  }

  public void setHidden(Boolean hidden) {
    this.hidden = hidden;
  }

  public String getFiletype() {
    return filetype;
  }

  public void setFiletype(String filetype) {
    this.filetype = filetype;
  }
}
