package com.broada.carrier.monitor.impl.mw.websphere.entity.was;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lixy Sep 16, 2008 4:52:30 PM
 */
public class Type {
  private String id;
  private String url;
  private String attr;
  private String parsePath;
  private Map<String, Item> items = new HashMap<String, Item>();
  private String itemAttr;
  private String instAttr;
  private String childName;

  public String getChildName() {
    return childName;
  }

  public void setChildName(String childName) {
    this.childName = childName;
  }

  public String getInstAttr() {
    return instAttr;
  }

  public void setInstAttr(String instAttr) {
    this.instAttr = instAttr;
  }

  public String getItemAttr() {
    return itemAttr;
  }

  public void setItemAttr(String itemAttr) {
    this.itemAttr = itemAttr;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAttr() {
    return attr;
  }

  public void setAttr(String attr) {
    this.attr = attr;
  }

  public String getParsePath() {
    return parsePath;
  }

  public void setParsePath(String parsePath) {
    this.parsePath = parsePath;
  }
  
  public void addItem(Item item) {
    items.put(item.getCode(), item);
  }
  
  public Item getItem(String itemCode) {
    return items.get(itemCode);
  }
  
  public Map<String, Item> getItems() {
    return items;
  }
}
