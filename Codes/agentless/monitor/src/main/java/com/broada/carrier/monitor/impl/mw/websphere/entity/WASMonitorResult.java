package com.broada.carrier.monitor.impl.mw.websphere.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lixy Sep 17, 2008 11:17:33 AM
 */
public class WASMonitorResult implements Serializable{
  /**
   * 
   */
  private static final long serialVersionUID = -2588908526883717585L;
  private Map<String, Double> perfMap = new HashMap<String, Double>();
  private Map<String, Double> condMap = new HashMap<String, Double>();
  private boolean watched = Boolean.FALSE; 
  private String instKey;

  public String getInstKey() {
    return instKey;
  }

  public void setInstKey(String instKey) {
    this.instKey = instKey;
  }
  
  public String getInstName() {
    return instKey;
  }

  public void addPerfItem(String itemCode, double value) {
    perfMap.put(itemCode, new Double(value));
  }

  public double getPerfValue(String itemCode) {
    return perfMap.get(itemCode).doubleValue();
  }

  public Map<String, Double> getPerfs() {
    return perfMap;
  }
  
  public List<String> toPerfIndexList() {
    List<String> result = new ArrayList<String>(perfMap.keySet());
//    for (int i = 1; i < 20; i++) {
//    	Integer key = new Integer(i);
//      if (perfMap.containsKey(key)) {
//        result.add(key);
//      }
//    }
//    return result;
  	return result;
  }

  public boolean isWatched() {
    return watched;
  }

  public void setWatched(boolean watched) {
    this.watched = watched;
  }
  
  public void setCondValue(String itemCode, Double value) {
    condMap.put(itemCode, value);
  }
  
  public Double getCondValue(String itemCode) {
    return condMap.get(itemCode);
  }
  
  public boolean containsCond(String itemCode) {
    return condMap.containsKey(itemCode);
  }
}
