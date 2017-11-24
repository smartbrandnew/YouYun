package com.broada.carrier.monitor.impl.mw.resin.ratio;

/**
 * Resin命中率监测实体
 * @author 杨帆
 * 
 */
public class ResinRatio {
  private Boolean isWacthed = Boolean.FALSE;

  private String name;

  private Double ratio;

  private Integer hitCount;

  private Integer totalCount;

  private Double ratioValue = new Double(20);

  /**
   * 获取字符串表示的命中率比率
   * @return
   */
  public String getRatioStr() {
    return hitCount.toString() + "/" + totalCount.toString();
  }

  public Integer getHitCount() {
    return hitCount;
  }

  public void setHitCount(Integer hitCount) {
    this.hitCount = hitCount;
  }

  public Double getRatio() {
    return ratio;
  }

  public void setRatio(Double ratio) {
    this.ratio = ratio;
  }

  public Double getRatioValue() {
    return ratioValue;
  }

  public void setRatioValue(Double ratioValue) {
    this.ratioValue = ratioValue;
  }

  public Integer getTotalCount() {
    return totalCount;
  }

  public void setTotalCount(Integer totalCount) {
    this.totalCount = totalCount;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }
}
