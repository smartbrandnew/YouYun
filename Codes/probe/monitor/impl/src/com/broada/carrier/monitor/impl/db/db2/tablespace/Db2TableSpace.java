package com.broada.carrier.monitor.impl.db.db2.tablespace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Db2TableSpace {
  /**
   * 表空间状态对照表
   */
  private final static Map<String, String> tablespaceStateConvertor = new HashMap<String, String>();
  
  static{
    tablespaceStateConvertor.put("0", "Normal");
    tablespaceStateConvertor.put("1", "Quiesced: SHARE");
    tablespaceStateConvertor.put("2", "Quiesced: UPDATE");
    tablespaceStateConvertor.put("4", "Quiesced: EXCLUSIVE");
    tablespaceStateConvertor.put("8", "Load pending");
    tablespaceStateConvertor.put("16", "Delete pending");
    tablespaceStateConvertor.put("32", "Backup pending");
    tablespaceStateConvertor.put("64", "Roll forward in progress");
    tablespaceStateConvertor.put("128", "Roll forward pending");
    tablespaceStateConvertor.put("256", "Restore pending");
    tablespaceStateConvertor.put("512", "Disable pending");
    tablespaceStateConvertor.put("1024", "Reorg in progress");
    tablespaceStateConvertor.put("2048", "Backup in progress");
    tablespaceStateConvertor.put("4096", "Storage must be defined");
    tablespaceStateConvertor.put("8192", "Restore in progress");
    tablespaceStateConvertor.put("16384", "Offline and not accessible");
    tablespaceStateConvertor.put("32768", "Drop pending");
    tablespaceStateConvertor.put("33554432", "Storage may be defined");
    tablespaceStateConvertor.put("67108864", "Storage Definition is in 'final' state");
    tablespaceStateConvertor.put("134217728", "Storage Definition was changed prior to rollforward");
    tablespaceStateConvertor.put("268435456", "DMS rebalancer is active");
    tablespaceStateConvertor.put("536870912", "TBS deletion in progress");
    tablespaceStateConvertor.put("1073741824", "TBS creation in progress");
  }
  
  private String name;

  private String type;

  private Double pageSize = new Double(0);

  private Double totalPages = new Double(0);

  private Double usedPages = new Double(0);

  private Double freePages = new Double(0);
  
  /**
   * 预取大小(KB)
   */
  private Double prefetchSize = new Double(0);
  
  /**
   * 扩展数据块大小(KB)
   */
  private Double extentSize = new Double(0);
  
  private String tablespaceState;
  
  public String getTablespaceState() {
  	try{
  		Long.parseLong(tablespaceState);
  		return tablespaceStateConvertor.get(tablespaceState);
  	}catch(NumberFormatException e){
  		return tablespaceState;
  	}
  }
  public static String getTablespaceStateKey(String value) {
  	for (Iterator<Entry<String, String>> it = tablespaceStateConvertor.entrySet().iterator(); it.hasNext();) {
  		Entry<String, String> entry = it.next();
  		if(entry.getValue().equals(value)){
  			return entry.getKey().toString();
  		}
  	}
    return "";
  }
  
  public String getRawTablespaceState() {
    return tablespaceState;
  }

  public void setTablespaceState(String tablespaceState) {
    this.tablespaceState = tablespaceState;
  }

  public Double getExtentSize() {
    return extentSize;
  }

  public void setExtentSize(Double extentSize) {
    this.extentSize = extentSize;
  }

  public Double getPrefetchSize() {
    return prefetchSize;
  }

  public void setPrefetchSize(Double prefetchSize) {
    this.prefetchSize = prefetchSize;
  }

  public Double getFreePages() {
    return freePages;
  }

  public void setFreePages(Double freePages) {
    this.freePages = freePages;
  }

  public Double getFreeRate() {
    if(getTotalPages().doubleValue()!=0){
      return new Double(getFreePages().doubleValue()*100/getTotalPages().doubleValue());
    }else{
      return new Double(0);
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getPageSize() {
    return pageSize;
  }

  public void setPageSize(Double pageSize) {
    this.pageSize = pageSize;
  }

  public Double getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Double totalPages) {
    this.totalPages = totalPages;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Double getUsedPages() {
    return usedPages;
  }

  public void setUsedPages(Double usedPages) {
    this.usedPages = usedPages;
  }

  public Double getUsedRate() {
    if(getTotalPages().doubleValue()!=0){
      return new Double(getUsedPages().doubleValue()*100/getTotalPages().doubleValue());
    }else{
      return new Double(0);
    }
  }

}
