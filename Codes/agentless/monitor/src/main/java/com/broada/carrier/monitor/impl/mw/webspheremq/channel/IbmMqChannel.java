package com.broada.carrier.monitor.impl.mw.webspheremq.channel;

import java.util.Date;

public class IbmMqChannel {
  private Boolean isWacthed = Boolean.TRUE;

  private String cName;//通道名

  private Integer receiveByte = new Integer(0);//接收字节

  private Integer receiveByteValue = new Integer(10000);

  private Integer sendByte = new Integer(0);//发送字节

  private Integer sendByteValue = new Integer(10000);

  private Integer cState;//通道状态

  private Integer sendSpace;//发送间隔(由两次获取通道数据时间差算出)

  private Integer affairNum = new Integer(0);//消息个数

  private Date lastMsgTime;//上一次保存消息的时间
  
  private Integer cType;

  public void setAffairNum(Integer affairNum) {
    this.affairNum = affairNum;
  }

  public String getCName() {
    return cName;
  }

  public void setCName(String name) {
    cName = name;
  }

  public Integer getCState() {
    return cState;
  }

  public void setCState(Integer state) {
    cState = state;
  }

  public Integer getReceiveByte() {
    return receiveByte;
  }

  public void setReceiveByte(Integer receiveByte) {
    this.receiveByte = receiveByte;
  }

  public Integer getReceiveByteValue() {
    return receiveByteValue;
  }

  public void setReceiveByteValue(Integer receiveByteValue) {
    this.receiveByteValue = receiveByteValue;
  }

  public Integer getSendByte() {
    return sendByte;
  }

  public void setSendByte(Integer sendByte) {
    this.sendByte = sendByte;
  }

  public Integer getSendByteValue() {
    return sendByteValue;
  }

  public void setSendByteValue(Integer sendByteValue) {
    this.sendByteValue = sendByteValue;
  }

  public Integer getSendSpace() {
    return sendSpace;
  }

  public void setSendSpace(Integer sendSpace) {
    this.sendSpace = sendSpace;
  }

  public Integer getAffairNum() {
    return affairNum;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public Date getLastMsgTime() {
    return lastMsgTime;
  }

  public void setLastMsgTime(Date lastMsgTime) {
    this.lastMsgTime = lastMsgTime;
  }

  public boolean equals(Object o) {
    if (o instanceof IbmMqChannel) {
      if (this.cName.equals(((IbmMqChannel) o).getCName()))
        return true;
    }
    return false;
  }

  public Integer getCType() {
    return cType;
  }

  public void setCType(Integer type) {
    cType = type;
  }
}
