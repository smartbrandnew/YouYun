package com.broada.carrier.monitor.impl.mw.iis.bytes;

/**
 * 字节传输实体类,用于配置端数据展现
 * @author 杨帆
 * 
 */
public class IISBytes {
  private Boolean isWacthed = Boolean.FALSE;

  private String webName;

  private Integer bytesSentPersec;

  private Integer bytesSentPersecValue = new Integer(300);

  private Integer bytesReceivedPersec;

  private Integer bytesReceivedPersecValue = new Integer(300);

  private Integer bytesTotalPersec;

  private Integer bytesTotalPersecValue = new Integer(600);

  public Integer getBytesReceivedPersec() {
    return bytesReceivedPersec;
  }

  public void setBytesReceivedPersec(Integer bytesReceivedPersec) {
    this.bytesReceivedPersec = bytesReceivedPersec;
  }

  public Integer getBytesReceivedPersecValue() {
    return bytesReceivedPersecValue;
  }

  public void setBytesReceivedPersecValue(Integer bytesReceivedPersecValue) {
    this.bytesReceivedPersecValue = bytesReceivedPersecValue;
  }

  public Integer getBytesSentPersec() {
    return bytesSentPersec;
  }

  public void setBytesSentPersec(Integer bytesSentPersec) {
    this.bytesSentPersec = bytesSentPersec;
  }

  public Integer getBytesSentPersecValue() {
    return bytesSentPersecValue;
  }

  public void setBytesSentPersecValue(Integer bytesSentPersecValue) {
    this.bytesSentPersecValue = bytesSentPersecValue;
  }

  public Integer getBytesTotalPersec() {
    return bytesTotalPersec;
  }

  public void setBytesTotalPersec(Integer bytesTotalPersec) {
    this.bytesTotalPersec = bytesTotalPersec;
  }

  public Integer getBytesTotalPersecValue() {
    return bytesTotalPersecValue;
  }

  public void setBytesTotalPersecValue(Integer bytesTotalPersecValue) {
    this.bytesTotalPersecValue = bytesTotalPersecValue;
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
