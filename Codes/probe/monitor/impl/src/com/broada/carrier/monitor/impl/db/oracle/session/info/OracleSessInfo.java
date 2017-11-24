package com.broada.carrier.monitor.impl.db.oracle.session.info;

/**
 * Oracle 会话实体类
 * 
 * @author Lixy (lixy@broada.com.cn)
 * Create By 2006-11-15 11:01:50
 */
public class OracleSessInfo {
  private String sessId = "";                   //会话ID
  private String userName = "";                 //用户名
  private Double sessCpu = new Double(0);       //占用CPU时间
  private Double maxCpuTim;                     //占用CPU时间阈值
  private Double sessSorts = new Double(0);     //内存排序次数
  private Double maxSorts;                      //内存排序次数阈值
  private Double tableScans = new Double(0);    //扫描表次数
  private Double sessReads = new Double(0);     //读次数
  private Double sessWrites = new Double(0);    //写次数
  private Double sessCommits = new Double(0);   //提交次数
  private Double maxCommits;                    //提交次数阈值
  private Double sessCursors = new Double(0);   //所占用光标数
  private Double maxCursors;                    //所占用光标数阈值
  private Double sessRatio = new Double(100);     //缓冲区命中率
  private Double maxRatio = new Double(90);       //缓冲区命中率阈值
  private Boolean isWacthed = Boolean.FALSE;

  public void setSessId(String sessId) {
    this.sessId = sessId;
  }

  public String getSessId() {
    return sessId;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserName() {
    return userName;
  }

  public void setSessCpu(Double sessCpu) {
    this.sessCpu = sessCpu;
  }
  
  public Double getSessCpu() {
    return sessCpu == null ? new Double(0) : sessCpu;
  }

  public void setMaxCpuTim(Double maxCpuTim){
    this.maxCpuTim = maxCpuTim;    
  }
  
  public Double getMaxCpuTim(){
    return maxCpuTim == null ? new Double(0) : maxCpuTim;
  }
  
  public void setSessSorts(Double sessSorts) {
    this.sessSorts = sessSorts;
  }

  public Double getSessSorts() {
    return sessSorts == null ? new Double(0) : sessSorts;
  }

  public void setMaxSorts(Double maxSorts){
    this.maxSorts = maxSorts;
  }
  
  public Double getMaxSorts(){
    return maxSorts == null ? new Double(0) : maxSorts;
  }
  
  public void setTableScans(Double tableScans) {
    this.tableScans = tableScans;
  }
  
  public Double getTableScans() {
    return tableScans == null ? new Double(0) : tableScans;
  }

  public void setSessReads(Double sessReads) {
    this.sessReads = sessReads;
  }

  public Double getSessReads() {
    return sessReads == null ? new Double(0) : sessReads;
  }

  public void setSessWrites(Double sessWrites) {
    this.sessWrites = sessWrites;
  }

  public Double getSessWrites() {
    return sessWrites == null ? new Double(0) : sessWrites;
  }

  public void setSessCommits(Double sessCommits) {
    this.sessCommits = sessCommits;
  }

  public Double getSessCommits() {
    return sessCommits == null ? new Double(0) : sessCommits;
  }

  public void setMaxCommits(Double maxCommits){
    this.maxCommits = maxCommits;
  }
  
  public Double getMaxCommits(){
    return maxCommits == null ? new Double(0) : maxCommits;
  }
  
  public void setSessCursors(Double sessCursors) {
    this.sessCursors = sessCursors;
  }

  public Double getSessCursors() {
    return sessCursors == null ? new Double(0) : sessCursors;
  }

  public void setMaxCursors(Double maxCursors){
    this.maxCursors = maxCursors;
  }
  
  public Double getMaxCursors(){
    return maxCursors == null ? new Double(0) : maxCursors;
  }
  
  public void setSessRatio(Double sessRatio) {
    this.sessRatio = sessRatio;
  }

  public Double getSessRatio() {
    return sessRatio == null ? new Double(0) : sessRatio;
  }
  
  public void setMaxRatio(Double maxRatio){
    this.maxRatio = maxRatio;
  }

  public Double getMaxRatio(){
    return maxRatio == null ? new Double(0) : maxRatio;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }
  
  public Boolean getIsWacthed() {
    return isWacthed == null ? Boolean.FALSE : isWacthed;
  }
}
