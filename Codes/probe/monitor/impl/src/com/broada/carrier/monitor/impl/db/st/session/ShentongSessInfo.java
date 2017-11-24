package com.broada.carrier.monitor.impl.db.st.session;

/**
 * 会话实体类
 * 
 * @author Zhouqa
 * Create By 2016年4月13日 下午3:28:08
 */
public class ShentongSessInfo {
  private String sessId = "";                   //会话ID
  private String userName = "";                 //用户名
  private Double sessSorts = new Double(0);     //内存排序次数
  private Double tableScans = new Double(0);    //扫描表次数
  private Double sessReads = new Double(0);     //读次数
  private Double sessWrites = new Double(0);    //写次数
  private Double sessCommits = new Double(0);   //提交次数
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
  
  public void setSessSorts(Double sessSorts) {
    this.sessSorts = sessSorts;
  }

  public Double getSessSorts() {
    return sessSorts == null ? new Double(0) : sessSorts;
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

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }
  
  public Boolean getIsWacthed() {
    return isWacthed == null ? Boolean.FALSE : isWacthed;
  }
}
