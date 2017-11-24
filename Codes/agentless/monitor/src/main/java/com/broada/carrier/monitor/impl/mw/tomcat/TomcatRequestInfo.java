package com.broada.carrier.monitor.impl.mw.tomcat;

/**
 * Tomcat一个连接器下面的请求信息.
 */
public class TomcatRequestInfo implements Tomcat{
  private long maxTime;
  private long processingTime;
  private long requestCount;
  private long errorCount;
  private long bytesReceived;
  private long bytesSent;
  public long getBytesReceived() {
    return bytesReceived;
  }
  public void setBytesReceived(long bytesReceived) {
    this.bytesReceived = bytesReceived;
  }
  public long getBytesSent() {
    return bytesSent;
  }
  public void setBytesSent(long bytesSent) {
    this.bytesSent = bytesSent;
  }
  public long getErrorCount() {
    return errorCount;
  }
  public void setErrorCount(long errorCount) {
    this.errorCount = errorCount;
  }
  public long getMaxTime() {
    return maxTime;
  }
  public void setMaxTime(long maxTime) {
    this.maxTime = maxTime;
  }
  public long getProcessingTime() {
    return processingTime;
  }
  public void setProcessingTime(long processingTime) {
    this.processingTime = processingTime;
  }
  public long getRequestCount() {
    return requestCount;
  }
  public void setRequestCount(long requestCount) {
    this.requestCount = requestCount;
  }

}
