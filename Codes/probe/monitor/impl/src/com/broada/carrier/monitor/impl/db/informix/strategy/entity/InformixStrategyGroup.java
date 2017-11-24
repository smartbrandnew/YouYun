package com.broada.carrier.monitor.impl.db.informix.strategy.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lixy Sep 2, 2008 1:21:49 PM
 */
public class InformixStrategyGroup {
  private Map<String, InformixStrategy> strategies = new HashMap<String, InformixStrategy>();
  private String goupId = "";
  private String name = "";
  private String sql = "";
  private String desc = "";

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public Map<String, InformixStrategy> getStrategies() {
    return strategies;
  }

  public InformixStrategy getStrategy(String itemCode) {
    return strategies.get(itemCode);
  }

  public void setStrategy(String itemCode, InformixStrategy strategy) {
    strategies.put(itemCode, strategy);
  }

  public String getGoupId() {
    return goupId;
  }

  public void setGoupId(String goupId) {
    this.goupId = goupId;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
