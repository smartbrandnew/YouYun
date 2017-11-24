package com.broada.carrier.monitor.impl.mw.tongweb.conn;

import com.broada.carrier.monitor.impl.mw.tongweb.TongWeb;

public class TongWebConnInfo implements TongWeb {
  private Integer waitingThreads;

  private Integer liveThreadNumber;

  private Integer maxHandlers;

  private Integer queueSize;

  private Integer clientTimeout;

  private Integer threadTimeout;

  private Integer requestWaitingNumber;

  private Integer throughPut;

  private Long bytesSent;

  private Long bytesReceived;
  
  /**
   * "httpConn"+当前应用的端口.
   */
  private String connectionid;
  
  /**
   * "connectionMethod".
   */
  private String type;
  
  /**
   * "teas".
   */
  private String domain;
  
  /**
   * KB/秒
   */
  private Double bytesSentRatio;
  
  /**
   * KB/秒
   */
  private Double bytesReceivedRatio;

  /**
   * 次/分钟
   */
  private Double throughPutRatio;
  
  public String getConnectionid() {
    return connectionid;
  }

  public void setConnectionid(String connectionid) {
    this.connectionid = connectionid;
  }


  
  public Long getBytesReceived() {
    return bytesReceived;
  }

  public void setBytesReceived(Long bytesReceived) {
    this.bytesReceived = bytesReceived;
  }
  
  public Long getBytesSent() {
    return bytesSent;
  }

  public void setBytesSent(Long bytesSent) {
    this.bytesSent = bytesSent;
  }

  public Integer getClientTimeout() {
    return clientTimeout;
  }

  public void setClientTimeout(Integer clientTimeout) {
    this.clientTimeout = clientTimeout;
  }

  public Integer getLiveThreadNumber() {
    return liveThreadNumber;
  }

  public void setLiveThreadNumber(Integer liveThreadNumber) {
    this.liveThreadNumber = liveThreadNumber;
  }

  public Integer getMaxHandlers() {
    return maxHandlers;
  }

  public void setMaxHandlers(Integer maxHandlers) {
    this.maxHandlers = maxHandlers;
  }

  public Integer getQueueSize() {
    return queueSize;
  }

  public void setQueueSize(Integer queueSize) {
    this.queueSize = queueSize;
  }

  public Integer getRequestWaitingNumber() {
    return requestWaitingNumber;
  }

  public void setRequestWaitingNumber(Integer requestWaitingNumber) {
    this.requestWaitingNumber = requestWaitingNumber;
  }

  public Integer getThreadTimeout() {
    return threadTimeout;
  }

  public void setThreadTimeout(Integer threadTimeout) {
    this.threadTimeout = threadTimeout;
  }

  public Integer getThroughPut() {
    return throughPut;
  }

  public void setThroughPut(Integer throughPut) {
    this.throughPut = throughPut;
  }

  public Integer getWaitingThreads() {
    return waitingThreads;
  }

  public void setWaitingThreads(Integer waitingThreads) {
    this.waitingThreads = waitingThreads;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Double getBytesReceivedRatio() {
    return bytesReceivedRatio;
  }

  public void setBytesReceivedRatio(Double bytesReceivedRatio) {
    this.bytesReceivedRatio = bytesReceivedRatio;
  }

  public Double getBytesSentRatio() {
    return bytesSentRatio;
  }

  public void setBytesSentRatio(Double bytesSentRatio) {
    this.bytesSentRatio = bytesSentRatio;
  }

  public Double getThroughPutRatio() {
    return throughPutRatio;
  }

  public void setThroughPutRatio(Double throughPutRatio) {
    this.throughPutRatio = throughPutRatio;
  }
  

  public Double getKBytesSent(){
    return new Double(getBytesSent().longValue()/(1024));
  }
  
  public Double getKBytesReceived(){
    return new Double(getBytesReceived().longValue()/(1024));
  }  
}
