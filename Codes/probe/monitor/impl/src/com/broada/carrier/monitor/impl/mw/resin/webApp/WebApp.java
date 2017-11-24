package com.broada.carrier.monitor.impl.mw.resin.webApp;

public class WebApp {
  private Boolean isWacthed = Boolean.FALSE;

  /*监测应用部署的路径*/
  private String contextPath;

  /*请求连接数*/
  private Integer requestCount;

  private Integer requestCountValue = new Integer(5);

  /*会话活跃数*/
  private Integer sessionActiveCount;

  private Integer sessionActiveCountValue = new Integer(5);

  public String getContextPath() {
    return contextPath;
  }

  public void setContextPath(String contextPath) {
    this.contextPath = contextPath;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public Integer getRequestCount() {
    return requestCount;
  }

  public void setRequestCount(Integer requestCount) {
    this.requestCount = requestCount;
  }

  public Integer getRequestCountValue() {
    return requestCountValue;
  }

  public void setRequestCountValue(Integer requestCountValue) {
    this.requestCountValue = requestCountValue;
  }

  public Integer getSessionActiveCount() {
    return sessionActiveCount;
  }

  public void setSessionActiveCount(Integer sessionActiveCount) {
    this.sessionActiveCount = sessionActiveCount;
  }

  public Integer getSessionActiveCountValue() {
    return sessionActiveCountValue;
  }

  public void setSessionActiveCountValue(Integer sessionActiveCountValue) {
    this.sessionActiveCountValue = sessionActiveCountValue;
  }
}
