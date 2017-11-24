package com.broada.carrier.monitor.impl.db.oracle.patchRate;

/**
 * Oralce数据库监测的表空间碎片FSFI实体
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-10-9 下午03:30:44
 */
public class OraclePatchRate {
  public  static String alarmItem = "_FSFI";
  private Boolean isWacthed = Boolean.FALSE;
  private String tsName = "";
  private Double currFSFI  = new Double(100);
  private Double leastFSFI  = new Double(30);
  
  public Boolean getIsWacthed() {
    return isWacthed;
  }
  public String getTsName() {
    return tsName;
  }
  public Double getCurrFSFI(){
    return currFSFI;
  }  
  public Double getLeastFSFI(){
    return leastFSFI;
  }
  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }
  public void setTsName(String tsName) {
    this.tsName = tsName;
  }
  public void setCurrFSFI(Double currFSFI){
    this.currFSFI = currFSFI;
  }  
  public void setLeastFSFI(Double leastFSFI){
    this.leastFSFI = leastFSFI;
  }
  public String getFSFIConditionName(){
    return tsName + alarmItem;
  }
}