package com.broada.carrier.monitor.impl.db.st.patchRate;

/**
 * shentong 数据库监测的表空间碎片FSFI实体
 * 
 * @author Zhouqa
 * Create By 2016年4月13日 上午10:35:38
 */
public class ShentongPatchRate {
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