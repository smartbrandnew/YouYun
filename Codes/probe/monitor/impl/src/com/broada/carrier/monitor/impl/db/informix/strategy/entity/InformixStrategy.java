package com.broada.carrier.monitor.impl.db.informix.strategy.entity;

import com.broada.carrier.monitor.impl.db.informix.strategy.interceptor.StrategyResultIntercetor;
import com.broada.utils.Condition;

import java.util.ArrayList;
import java.util.List;

/**
 * Informix监测性能项计算策略
 * 
 * @author lixy Sep 2, 2008 11:49:12 AM
 */
public class InformixStrategy {
  private String itemCode;
  private String name;
  private String field;
  private String bsh;
  private String sql;
  private List<StrategyResultIntercetor> intercetors = new ArrayList<StrategyResultIntercetor>();

  /**是否为Condition*/
  private boolean isCondition = Boolean.FALSE.booleanValue();
  /**比较类型*/
  private int type = Condition.GREATERTHAN;
  /**阈值*/
  private double threshold;
  /**单位*/
  private String unit;

  public String getItemCode() {
    return itemCode;
  }

  public void setItemCode(String itemCode) {
    this.itemCode = itemCode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getBsh() {
    return bsh;
  }

  public void setBsh(String bsh) {
    this.bsh = bsh;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public boolean isCondition() {
    return isCondition;
  }

  public void setCondition(boolean isCondition) {
    this.isCondition = isCondition;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public double getThreshold() {
    return threshold;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  public void addIntercetor(StrategyResultIntercetor intercetor) {
    this.intercetors.add(intercetor);
  }

  public List<StrategyResultIntercetor> getIntercetors() {
    return intercetors;
  }
}
