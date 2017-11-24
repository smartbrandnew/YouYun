package com.broada.carrier.monitor.impl.db.oracle.dtfile;

import com.broada.utils.StringUtil;

/**
 * Oracle 数据文件模型
 * 
 * @author Lixy (lixy@broada.com.cn)
 * Create By 2006-10-20 10:24:03
 */
public class OracleDtfile {  
  private String dfName;                     //文件名称
  private String dfStatus;                   //文件状态
  private Double dfSize = new Double(0);     //文件大小
  private Double maxDfSize = new Double(2048);  //文件大小阈值
  private Double dfRTS = new Double(0);      //文件读次数
  private Double dfWRTS = new Double(0);     //文件写次数
  private Double phyblkrd = new Double(0);      //文件读block数
  private Double phyblkwrt = new Double(0);     //文件写block数
  private Double totalBlock = new Double(0);     //文件读写block总数
  private Double dfRTim = new Double(0);     //文件读取时间
  private Double dfWRTim = new Double(0);    //文件写时间
  private Boolean isWacthed = Boolean.FALSE;
  
  public String getDfName(){
    return dfName;
  }
  
  public String getDfStatus(){
    return dfStatus;
  }
  
  public Double getDfSize(){
    return dfSize;
  }
  
  public Double getMaxDfSize(){
    return maxDfSize;
  }
  
  public Double getDfRTS(){
    return dfRTS;
  }
  
  public Double getDfWRTS(){
    return dfWRTS;
  }
    
  public Double getDfRTim(){
    return dfRTim;
  }

  public Double getDfWRTim(){
    return dfWRTim;
  }
  
  public Boolean getIsWacthed() {
    return isWacthed;
  }
  
  public void setDfName(String dfName){
    this.dfName = dfName;
  }
  
  public void setDfStatus(String dfStatus){
    this.dfStatus = dfStatus;
  }
  
  public void setDfSize(Double dfSize){
    this.dfSize = dfSize;
  }

  public void setMaxDfSize(Double maxDfSize){
    this.maxDfSize = maxDfSize;
  }
  
  public void setDfRTS(Double dfRTS){
    this.dfRTS = dfRTS;
  }
  
  public void setDfWRTS(Double dfWRTS){
    this.dfWRTS = dfWRTS;
  }

  public void setDfRTim(Double dfRTim){
    this.dfRTim = dfRTim;
  }

  public void setDfWRTim(Double dfWRTim){
    this.dfWRTim = dfWRTim;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }
  
  public String getDesc() {
    StringBuffer sb = new StringBuffer("");
    if (!StringUtil.isNullOrBlank(dfName)) {
      sb.append(dfName).append(";");
    }
    if (!StringUtil.isNullOrBlank(dfStatus)) {
      sb.append(dfStatus).append(";");
    }
    sb.append(dfSize).append(";");
    sb.append(dfRTS).append(";");
    sb.append(dfWRTS).append(";");
    sb.append(dfRTim).append(";");
    sb.append(dfWRTim).append(";");
    return sb.toString();
  }

  public Double getPhyblkrd() {
    return phyblkrd;
  }

  public void setPhyblkrd(Double phyblkrd) {
    this.phyblkrd = phyblkrd;
  }

  public Double getPhyblkwrt() {
    return phyblkwrt;
  }

  public void setPhyblkwrt(Double phyblkwrt) {
    this.phyblkwrt = phyblkwrt;
  }

  public Double getTotalBlock() {
    return totalBlock;
  }

  public void setTotalBlock(Double totalBlock) {
    this.totalBlock = totalBlock;
  }
}
