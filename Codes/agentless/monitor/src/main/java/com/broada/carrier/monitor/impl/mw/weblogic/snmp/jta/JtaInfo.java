package com.broada.carrier.monitor.impl.mw.weblogic.snmp.jta;


public class JtaInfo {
  private double totalTa;
  private double rbTaRc;
  private double rbTaSys;
  private double rbTaApp;
  
  private String insKey;

  /**
   * @return the totalTa
   */
  public double getTotalTa() {
    return totalTa;
  }

  /**
   * @param totalTa the totalTa to set
   */
  public void setTotalTa(double totalTa) {
    this.totalTa = totalTa;
  }

  /**
   * @return the rbTaRc
   */
  public double getRbTaRc() {
    return rbTaRc;
  }

  /**
   * @param rbTaRc the rbTaRc to set
   */
  public void setRbTaRc(double rbTaRc) {
    this.rbTaRc = rbTaRc;
  }

  /**
   * @return the rbTaSys
   */
  public double getRbTaSys() {
    return rbTaSys;
  }

  /**
   * @param rbTaSys the rbTaSys to set
   */
  public void setRbTaSys(double rbTaSys) {
    this.rbTaSys = rbTaSys;
  }

  /**
   * @return the rbTaApp
   */
  public double getRbTaApp() {
    return rbTaApp;
  }

  /**
   * @param rbTaApp the rbTaApp to set
   */
  public void setRbTaApp(double rbTaApp) {
    this.rbTaApp = rbTaApp;
  }

  /**
   * @return the insKey
   */
  public String getInsKey() {
    return insKey;
  }

  /**
   * @param insKey the insKey to set
   */
  public void setInsKey(String insKey) {
    this.insKey = insKey;
  }
  
  
}
