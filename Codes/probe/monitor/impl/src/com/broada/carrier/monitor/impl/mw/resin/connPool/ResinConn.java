package com.broada.carrier.monitor.impl.mw.resin.connPool;

public class ResinConn {
  private Boolean isWacthed = Boolean.FALSE;

  /** 监测数据库连接池的名称 */
  private String name;

  /** 活跃连接数 */
  private Integer activeCount;

  private Integer activeCountValue=new Integer(5);

  /** 空闲连接数 */
  private Integer idleCount;

  private Integer idleCountValue=new Integer(5);

  /** 连接命中率（ConnectionCountTotal/ConnectionCreateCountTotal） */
  private Double create_ratio;

  private Double create_ratioValue=new Double(50);

  public Integer getActiveCount() {
    return activeCount;
  }

  public void setActiveCount(Integer activeCount) {
    this.activeCount = activeCount;
  }

  public Integer getActiveCountValue() {
    return activeCountValue;
  }

  public void setActiveCountValue(Integer activeCountValue) {
    this.activeCountValue = activeCountValue;
  }

  public Double getCreate_ratio() {
    return create_ratio;
  }

  public void setCreate_ratio(Double create_ratio) {
    this.create_ratio = create_ratio;
  }

  public Double getCreate_ratioValue() {
    return create_ratioValue;
  }

  public void setCreate_ratioValue(Double create_ratioValue) {
    this.create_ratioValue = create_ratioValue;
  }

  public Integer getIdleCount() {
    return idleCount;
  }

  public void setIdleCount(Integer idleCount) {
    this.idleCount = idleCount;
  }

  public Integer getIdleCountValue() {
    return idleCountValue;
  }

  public void setIdleCountValue(Integer idleCountValue) {
    this.idleCountValue = idleCountValue;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
