package com.broada.carrier.monitor.impl.mw.iis.webRequest;

public class IISWebRequest {
  private Boolean isWacthed = Boolean.FALSE;

  private String webName;

  private Integer getRequestsPersec;

  private Integer getRequestsPersecValue = new Integer(40);

  private Integer headRequestsPersec;

  private Integer headRequestsPersecValue = new Integer(40);

  private Integer postRequestsPersec;

  private Integer postRequestsPersecValue = new Integer(40);

  private Integer otherRequestMethodsPersec;

  private Integer otherRequestMethodsPersecValue = new Integer(40);

  public Integer getGetRequestsPersec() {
    return getRequestsPersec;
  }

  public void setGetRequestsPersec(Integer getRequestsPersec) {
    this.getRequestsPersec = getRequestsPersec;
  }

  public Integer getGetRequestsPersecValue() {
    return getRequestsPersecValue;
  }

  public void setGetRequestsPersecValue(Integer getRequestsPersecValue) {
    this.getRequestsPersecValue = getRequestsPersecValue;
  }

  public Integer getHeadRequestsPersec() {
    return headRequestsPersec;
  }

  public void setHeadRequestsPersec(Integer headRequestsPersec) {
    this.headRequestsPersec = headRequestsPersec;
  }

  public Integer getHeadRequestsPersecValue() {
    return headRequestsPersecValue;
  }

  public void setHeadRequestsPersecValue(Integer headRequestsPersecValue) {
    this.headRequestsPersecValue = headRequestsPersecValue;
  }

  public Boolean getIsWacthed() {
    return isWacthed;
  }

  public void setIsWacthed(Boolean isWacthed) {
    this.isWacthed = isWacthed;
  }

  public Integer getOtherRequestMethodsPersec() {
    return otherRequestMethodsPersec;
  }

  public void setOtherRequestMethodsPersec(Integer otherRequestMethodsPersec) {
    this.otherRequestMethodsPersec = otherRequestMethodsPersec;
  }

  public Integer getOtherRequestMethodsPersecValue() {
    return otherRequestMethodsPersecValue;
  }

  public void setOtherRequestMethodsPersecValue(Integer otherRequestMethodsPersecValue) {
    this.otherRequestMethodsPersecValue = otherRequestMethodsPersecValue;
  }

  public Integer getPostRequestsPersec() {
    return postRequestsPersec;
  }

  public void setPostRequestsPersec(Integer postRequestsPersec) {
    this.postRequestsPersec = postRequestsPersec;
  }

  public Integer getPostRequestsPersecValue() {
    return postRequestsPersecValue;
  }

  public void setPostRequestsPersecValue(Integer postRequestsPersecValue) {
    this.postRequestsPersecValue = postRequestsPersecValue;
  }

  public String getWebName() {
    return webName;
  }

  public void setWebName(String webName) {
    this.webName = webName;
  }
}
