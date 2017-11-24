package com.broada.carrier.monitor.impl.mw.websphere.entity.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lixy Sep 17, 2008 10:18:05 AM
 */
public class UiGroup {
  private String goupId;
  private String name;
  private String desc;
  private Map<String, PerfItem> items = new HashMap<String, PerfItem>();
  private boolean hasMonitorCol;

  public boolean isHasMonitorCol() {
    return hasMonitorCol;
  }

  public void setHasMonitorCol(boolean hasMonitorCol) {
    this.hasMonitorCol = hasMonitorCol;
  }

  public String getGoupId() {
    return goupId;
  }

  public void setGoupId(String goupId) {
    this.goupId = goupId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDesc() {
    return desc;
  }

  public void setDesc(String desc) {
    this.desc = desc;
  }

  public void addPerfItem(PerfItem perfItem) {
    this.items.put(perfItem.getItemCode(), perfItem);
  }

  public PerfItem getPerfItem(int itemIdx) {
    return items.get(new Integer(itemIdx));
  }

  public Map<String, PerfItem> getPerfItems() {
    return items;
  }
  
  public List<PerfItem> toCondItemLits() {
    List<PerfItem> result = new ArrayList<PerfItem>();
    List<PerfItem> itemValues = new ArrayList<PerfItem>(items.values());
    for(PerfItem item:itemValues){
    	if(item.isShowCondition()){
    		result.add(item);
    	}
    }
    return result;
  }
    
  public List<PerfItem> toPerfItemList() {
    List<PerfItem> result = new ArrayList<PerfItem>();
    List<PerfItem> itemValues = new ArrayList<PerfItem>(items.values());
    for(PerfItem item:itemValues){
    	if(item.isShowPerf() || item.isShowCondition()){
    		result.add(item);
    	}
    }
//    for (int i = 1; i < 20; i++) {
//      Integer key = new Integer(i);
//      if (items.containsKey(key)) {
//        PerfItem item = items.get(key);
//        try {
//          if (item.isShowPerf()) {
//            PerfItem tmp = new PerfItem();
//            BeanUtils.copyProperties(tmp, item);
//            result.add(tmp);
//          }
//          if (item.isShowCondition()) {
//            PerfItem tmp = new PerfItem();
//            BeanUtils.copyProperties(tmp, item);
//            result.add(tmp);
//          }
//        } catch (Exception e) {
//        }
//      }
//    }
    return result;
  }
}
