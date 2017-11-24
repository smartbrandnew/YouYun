package com.broada.carrier.monitor.impl.mw.tomcat;

/**
 * Tomcat一个连接器下面的工作情况,包括请求IP、接收字节、发送字节、响应时间、请求类型等信息
 */
public class TomcatWorker implements Tomcat{

  private String stage;

  private long requestProcessingTime;

  private long requestBytesSent;

  private long requestBytesRecieved;

  private String remoteAddr;

  private String virtualHost;

  private String method;

  private String currentUri;

  private String currentQueryString;

  private String protocol;

  public String getCurrentQueryString() {
    return currentQueryString;
  }

  public void setCurrentQueryString(String currentQueryString) {
    this.currentQueryString = currentQueryString;
  }

  public String getCurrentUri() {
    return currentUri;
  }

  public void setCurrentUri(String currentUri) {
    this.currentUri = currentUri;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getRemoteAddr() {
    return remoteAddr;
  }

  public long getRequestBytesRecieved() {
    return requestBytesRecieved;
  }

  public void setRequestBytesRecieved(long requestBytesRecieved) {
    this.requestBytesRecieved = requestBytesRecieved;
  }

  public long getRequestBytesSent() {
    return requestBytesSent;
  }

  public void setRequestBytesSent(long requestBytesSent) {
    this.requestBytesSent = requestBytesSent;
  }

  public long getRequestProcessingTime() {
    return requestProcessingTime;
  }

  public void setRequestProcessingTime(long requestProcessingTime) {
    this.requestProcessingTime = requestProcessingTime;
  }

  public void setRemoteAddr(String remoteAddr) {
    this.remoteAddr = remoteAddr;
  }

  public String getStage() {
    return stage;
  }

  public void setStage(String stage) {
    this.stage = stage;
  }

  public String getVirtualHost() {
    return virtualHost;
  }

  public void setVirtualHost(String virtualHost) {
    this.virtualHost = virtualHost;
  }
}
