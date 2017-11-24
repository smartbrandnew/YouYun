package com.broada.carrier.monitor.impl.db.informix.strategy.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 最近一次策略监测的结果
 * 
 * @author lixy Sep 5, 2008 10:10:55 AM
 */
public class StrategyLastResult {
  private static Map<String, Date> occurTimeMap = new HashMap<String, Date>();
  private static Map<String, Double> valueMap = new HashMap<String, Double>();

  public static void putReustInfo(String srvId, String itemCode, Date occurTime, Double value) {
    String key = srvId + "_" + itemCode;
    occurTimeMap.put(key, occurTime);
    valueMap.put(key, value);
  }

  public static Date getOccurTime(String srvId, String itemCode) {
    String key = srvId + "_" + itemCode;
    return occurTimeMap.get(key);
  }

  public static Double getValue(String srvId, String itemCode) {
    String key = srvId + "_" + itemCode;
    return valueMap.get(key);
  }
}
