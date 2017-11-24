package com.broada.carrier.monitor.impl.host.cli.linuxio;

import com.broada.carrier.monitor.method.common.MonitorCondition;

public class CLIIOMonitorCondition extends MonitorCondition {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -3468797979078050045L;

  /**一秒中有百分之多少的时间用于 I/O 操作，或者说一秒中有多少时间 I/O 队列是非空的(%util)*/
  private float util;
  /**每秒进行 merge 的读操作数目(rrqm/s)*/
  private float rrqmPerSecond;
  /**每秒进行 merge 的写操作数目(wrqm/s)*/
  private float wrqmPerSecond;
  /**每秒完成的读 I/O 设备次数(r/s)*/
  private float readPerSecond;
  /**每秒完成的写 I/O 设备次数(w/s)*/
  private float writePerSecond;
  /**每秒读扇区数(rsec/s)*/
  private float rsecPerSecond;
  /**每秒写扇区数(wsec/s)*/
  private float wsecPerSecond;
  /**每秒读K字节数(rkB/s)。是rsect/s 的一半，因为每扇区大小为512字节。*/
  private float rkbPerSecond;
  /**每秒读K写节数(wkB/s)。是wsect/s 的一半，因为每扇区大小为512字节。*/
  private float wkbPerSecond;
  /**平均每次设备I/O操作的数据大小(扇区)(avgrq-sz)*/
  private float avgrqsz;
  /**平均I/O队列长度(avgqu-sz)*/
  private float avgqusz;
  /**平均每次设备I/O操作的等待时间(毫秒)(await)*/
  private float await;
  /**平均每次设备I/O操作的服务时间(毫秒)(svctm)*/
  private float svctm;
  
  private float maxUtil = 30;
  private float maxAvgqusz = 30;

  public CLIIOMonitorCondition() {
    
  }

  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof CLIIOMonitorCondition)) {
      return false;
    }
    return getField().equals(((CLIIOMonitorCondition) obj).getField());
  }

  public String getUnit() {
    return "%";
  }

  public String getDescription() {
    return "";
  }

  public String getFieldDescription() {
    return "";
  }

  public String getComDescription() {
    return "";
  }

  public void setField(String field) {
    super.setField(field);
  }

  public String getFieldCondition() {
    return "的使用率";
  }

  public float getUtil() {
    return util;
  }

  public void setUtil(float util) {
    this.util = util;
  }

  public float getRrqmPerSecond() {
    return rrqmPerSecond;
  }

  public void setRrqmPerSecond(float rrqmPerSecond) {
    this.rrqmPerSecond = rrqmPerSecond;
  }

  public float getWrqmPerSecond() {
    return wrqmPerSecond;
  }

  public void setWrqmPerSecond(float wrqmPerSecond) {
    this.wrqmPerSecond = wrqmPerSecond;
  }

  public float getReadPerSecond() {
    return readPerSecond;
  }

  public void setReadPerSecond(float readPerSecond) {
    this.readPerSecond = readPerSecond;
  }

  public float getWritePerSecond() {
    return writePerSecond;
  }

  public void setWritePerSecond(float writePerSecond) {
    this.writePerSecond = writePerSecond;
  }

  public float getRsecPerSecond() {
    return rsecPerSecond;
  }

  public void setRsecPerSecond(float rsecPerSecond) {
    this.rsecPerSecond = rsecPerSecond;
  }

  public float getWsecPerSecond() {
    return wsecPerSecond;
  }

  public void setWsecPerSecond(float wsecPerSecond) {
    this.wsecPerSecond = wsecPerSecond;
  }

  public float getRkbPerSecond() {
    return rkbPerSecond;
  }

  public void setRkbPerSecond(float rkbPerSecond) {
    this.rkbPerSecond = rkbPerSecond;
  }

  public float getWkbPerSecond() {
    return wkbPerSecond;
  }

  public void setWkbPerSecond(float wkbPerSecond) {
    this.wkbPerSecond = wkbPerSecond;
  }

  public float getAvgrqsz() {
    return avgrqsz;
  }

  public void setAvgrqsz(float avgrqsz) {
    this.avgrqsz = avgrqsz;
  }

  public float getAvgqusz() {
    return avgqusz;
  }

  public void setAvgqusz(float avgqusz) {
    this.avgqusz = avgqusz;
  }

  public float getAwait() {
    return await;
  }

  public void setAwait(float await) {
    this.await = await;
  }

  public float getSvctm() {
    return svctm;
  }

  public void setSvctm(float svctm) {
    this.svctm = svctm;
  }

  public float getMaxUtil() {
    return maxUtil;
  }

  public void setMaxUtil(float maxUtil) {
    this.maxUtil = maxUtil;
  }

  public float getMaxAvgqusz() {
    return maxAvgqusz;
  }

  public void setMaxAvgqusz(float maxAvgqusz) {
    this.maxAvgqusz = maxAvgqusz;
  }
}
