package com.broada.carrier.monitor.impl.db.informix.strategy.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lixy Sep 2, 2008 2:55:28 PM
 */
public class InformixStrategyResult {
  private String groupId;
  private Map<String, Double> results = new HashMap<String, Double>();
  
  public InformixStrategyResult(String groupId) {
    this.groupId = groupId;
  }

  public String getGroupId() {
    return groupId;
  }

  public Map<String, Double> getRresults() {
    return results;
  }

  public double getResult(String itemCode) {
    Double result = results.get(itemCode);
    if (result == null) {
      return -1;
    }
    return result.doubleValue();
  }

  public void putResult(String itemCode, double result) {
    results.put(itemCode, new Double(result));
  }
}
