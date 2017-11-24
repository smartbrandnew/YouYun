package com.broada.carrier.monitor.impl.mw.webspheremq;

/**
 * <p>Title: ibmMqQueueCondition</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Broada</p>
 * @author plx (panlx@broada.com.cn)
 * @version 2.4
 */

public class IbmMqQueue {
  public static final int GREATERTHAN = 2;
  private Boolean isWacthed = Boolean.TRUE;
  private String qName;
  private Integer curValue;
  private Integer maxValue;
  private Integer type = new Integer(GREATERTHAN);
  private Integer value = new Integer(1000);
  private Integer queueType ;//队列类型
  private int getAllowed;//是否允许取出消息
  private int putAllowed;//是否允许放入消息
  private Integer openInput;//打开输入记数
  private Integer openOutput;//打开输出记数
  private Integer maxMsgLength;//队列消息的最大字节数;

  public int getGetAllowed() {
    return getAllowed;
  }
  public void setGetAllowed(int getAllowed) {
    this.getAllowed = getAllowed;
  }
  public Integer getMaxMsgLength() {
    return maxMsgLength;
  }
  public void setMaxMsgLength(Integer maxMsgLength) {
    this.maxMsgLength = maxMsgLength;
  }
  public Integer getOpenInput() {
    return openInput;
  }
  public void setOpenInput(Integer openInput) {
    this.openInput = openInput;
  }
  public Integer getOpenOutput() {
    return openOutput;
  }
  public void setOpenOutput(Integer openOutput) {
    this.openOutput = openOutput;
  }
  public int getPutAllowed() {
    return putAllowed;
  }
  public void setPutAllowed(int putAllowed) {
    this.putAllowed = putAllowed;
  }
  public Integer getQueueType() {
    return queueType;
  }
  public void setQueueType(Integer queueType) {
    this.queueType = queueType;
  }
  public Boolean getIsWacthed() {
    return isWacthed;
  }
  public String getQName() {
    return qName;
  }
  public Integer getCurValue() {
    return curValue;
  }
  public Integer getMaxValue() {
    return maxValue;
  }
  public Integer getType() {
    return type;
  }
  public Integer getValue() {
    return value;
  }
  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }
  public void setQName(String qName) {
    this.qName = qName;
  }
  public void setCurValue(Integer curValue) {
    this.curValue = curValue;
  }
  public void setType(Integer type) {
    this.type = type;
  }
  public void setValue(Integer value) {
    this.value = value;
  }
  public void setMaxValue(Integer maxValue) {
    this.maxValue = maxValue;
  }

}
