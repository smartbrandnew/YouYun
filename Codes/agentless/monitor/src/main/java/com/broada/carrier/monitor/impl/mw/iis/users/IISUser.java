package com.broada.carrier.monitor.impl.mw.iis.users;

public class IISUser {
  private Boolean isWacthed = Boolean.FALSE;

  private String webName;

  private Integer currAnmUsers;

  private Integer currAnmUsersValue = new Integer(10);

  private Integer currUnAnmUsers;

  private Integer currUnAnmUsersValue = new Integer(10);

  private Integer anmUserPers;

  private Integer anmUserPersValue = new Integer(15);

  private Integer unAnmUserPers;

  private Integer unAnmUserPersValue = new Integer(15);

  public Integer getUnAnmUserPers() {
    return unAnmUserPers;
  }

  public void setUnAnmUserPers(Integer unAnmUserPers) {
    this.unAnmUserPers = unAnmUserPers;
  }

  public Integer getUnAnmUserPersValue() {
    return unAnmUserPersValue;
  }

  public void setUnAnmUserPersValue(Integer unAnmUserPersValue) {
    this.unAnmUserPersValue = unAnmUserPersValue;
  }

  public Integer getAnmUserPers() {
    return anmUserPers;
  }

  public void setAnmUserPers(Integer anmUserPers) {
    this.anmUserPers = anmUserPers;
  }

  public Integer getAnmUserPersValue() {
    return anmUserPersValue;
  }

  public void setAnmUserPersValue(Integer anmUserPersValue) {
    this.anmUserPersValue = anmUserPersValue;
  }

  public Integer getCurrAnmUsers() {
    return currAnmUsers;
  }

  public void setCurrAnmUsers(Integer currAnmUsers) {
    this.currAnmUsers = currAnmUsers;
  }

  public Integer getCurrAnmUsersValue() {
    return currAnmUsersValue;
  }

  public void setCurrAnmUsersValue(Integer currAnmUsersValue) {
    this.currAnmUsersValue = currAnmUsersValue;
  }

  public Integer getCurrUnAnmUsers() {
    return currUnAnmUsers;
  }

  public void setCurrUnAnmUsers(Integer currUnAnmUsers) {
    this.currUnAnmUsers = currUnAnmUsers;
  }

  public Integer getCurrUnAnmUsersValue() {
    return currUnAnmUsersValue;
  }

  public void setCurrUnAnmUsersValue(Integer currUnAnmUsersValue) {
    this.currUnAnmUsersValue = currUnAnmUsersValue;
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
