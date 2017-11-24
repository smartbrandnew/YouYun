package com.broada.carrier.monitor.impl.host.cli.directory.unix;

import java.util.Date;

import com.broada.carrier.monitor.impl.host.cli.file.CLIFileMonitor;
import com.broada.carrier.monitor.impl.host.cli.file.CLIFileMonitorCondition;
import com.broada.carrier.monitor.method.common.MonitorCondition;

public class CLIFile extends MonitorCondition {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -8539281522303081511L;
  /** 文件名称 */
  private String name = "";
  /** 文件大小 */
  private Double size = new Double(0);
  /** 创建时间 */
  private Date createTime;
  /** 链接数 */
  private Integer linkedCount = new Integer(1);
  /** 文件主 */
  private String owner = "";
  /** 文件组 */
  private String group = "";
  
  public CLIFile() {  	
  }
  
  public CLIFile(CLIFileMonitorCondition file) {
		this.name = file.getFilepath();
		this.size = file.getSize();
		this.createTime = file.getModifiedTime();
		this.linkedCount = file.getLinks();
		this.owner = file.getUser();
		this.group = file.getGroup();
	}

	public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
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

  public void setCreateTime(String createTime) {
    this.createTime = CLIFileMonitor.formateMtimeToDatetim(createTime);
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public Integer getLinkedCount() {
    return linkedCount;
  }

  public void setLinkedCount(Integer linkedCount) {
    this.linkedCount = linkedCount;
  }
}
