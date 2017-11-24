package com.broada.carrier.monitor.impl.host.cli.netstat;
/**
 * 网络状态
 * @author zhoucy(zhoucy@broada.com.cn)
 * Create By Apr 29, 2008 2:24:04 PM
 */
public class CLINetStatInfo {
  /**名称*/
  private String name;
  /**网络接口NetWork Interface*/
  private String network;
  /**地址*/
  private String address;
  /**最大传输单元*/
  private long mtu;
  /**接收包数*/
  private long ipkts;
  /**接收包错误数*/
  private long ierrs;
  /**发出包数*/
  private long opkts;
  /**发出包错误数*/
  private long oerrs;
  /**冲突数*/
  private long coll;//collision
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getNetwork() {
    return network;
  }
  public void setNetwork(String network) {
    this.network = network;
  }
  public String getAddress() {
    return address;
  }
  public void setAddress(String address) {
    this.address = address;
  }
  public long getMtu() {
    return mtu;
  }
  public void setMtu(long mtu) {
    this.mtu = mtu;
  }
  public long getIpkts() {
    return ipkts;
  }
  public void setIpkts(long ipkts) {
    this.ipkts = ipkts;
  }
  public long getIerrs() {
    return ierrs;
  }
  public void setIerrs(long ierrs) {
    this.ierrs = ierrs;
  }
  public long getOpkts() {
    return opkts;
  }
  public void setOpkts(long opkts) {
    this.opkts = opkts;
  }
  public long getOerrs() {
    return oerrs;
  }
  public void setOerrs(long oerrs) {
    this.oerrs = oerrs;
  }
  public long getColl() {
    return coll;
  }
  public void setColl(long coll) {
    this.coll = coll;
  }
}
