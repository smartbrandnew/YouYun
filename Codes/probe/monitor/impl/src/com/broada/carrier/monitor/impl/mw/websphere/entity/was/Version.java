package com.broada.carrier.monitor.impl.mw.websphere.entity.was;

/**
 * @author lixy Sep 17, 2008 11:05:26 AM
 */
public class Version {
  private String url;
  private String eleId;
  private String value;
  private String defUrl;
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getEleId() {
    return eleId;
  }

  public void setEleId(String eleId) {
    this.eleId = eleId;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getDefUrl() {
    return defUrl;
  }

  public void setDefUrl(String defUrl) {
    this.defUrl = defUrl;
  }
}
