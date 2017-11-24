package com.broada.carrier.monitor.impl.mw.iis.files;

/**
 * 文件传输实体类,用于配置端数据展现
 * @author 杨帆
 * 
 */
public class IISFiles {
  private Boolean isWacthed = Boolean.FALSE;

  private String webName;

  private Integer filesSentPersec;

  private Integer filesSentPersecValue = new Integer(1);

  private Integer filesReceivedPersec;

  private Integer filesReceivedPersecValue = new Integer(1);

  private Integer filesTotalPersec;

  private Integer filesTotalPersecValue = new Integer(2);

  public Integer getFilesReceivedPersec() {
    return filesReceivedPersec;
  }

  public void setFilesReceivedPersec(Integer filesReceivedPersec) {
    this.filesReceivedPersec = filesReceivedPersec;
  }

  public Integer getFilesReceivedPersecValue() {
    return filesReceivedPersecValue;
  }

  public void setFilesReceivedPersecValue(Integer filesReceivedPersecValue) {
    this.filesReceivedPersecValue = filesReceivedPersecValue;
  }

  public Integer getFilesSentPersec() {
    return filesSentPersec;
  }

  public void setFilesSentPersec(Integer filesSentPersec) {
    this.filesSentPersec = filesSentPersec;
  }

  public Integer getFilesSentPersecValue() {
    return filesSentPersecValue;
  }

  public void setFilesSentPersecValue(Integer filesSentPersecValue) {
    this.filesSentPersecValue = filesSentPersecValue;
  }

  public Integer getFilesTotalPersec() {
    return filesTotalPersec;
  }

  public void setFilesTotalPersec(Integer filesTotalPersec) {
    this.filesTotalPersec = filesTotalPersec;
  }

  public Integer getFilesTotalPersecValue() {
    return filesTotalPersecValue;
  }

  public void setFilesTotalPersecValue(Integer filesTotalPersecValue) {
    this.filesTotalPersecValue = filesTotalPersecValue;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public String getWebName() {
    return webName;
  }

  public void setWebName(String webName) {
    this.webName = webName;
  }
}
