package com.broada.carrier.monitor.impl.mw.weblogic.snmp.jdbc;

import org.jdom2.Element;

import com.broada.carrier.monitor.method.common.MonitorCondition;

/**
 * 放置Weblogic Jdbc实例信息
 *
 * <p>Title: </p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 1.0
 */
public class WlsJdbcInstanceInfo extends MonitorCondition{
	/**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -8512642280687341244L;

	private long activeConn;
	
	private long waitingConn;
	//阈值 - 最大活动连接数
	private long maxActiveConn = 10;
	//阈值 - 最大等待连接数
	private long maxWaitingConn = 10;
	
	private long totalConn = 0;
	private long maxActivedConn;
	private long maxWaitConn;
	private long maxWaitingTime;
	private long capacity;
	public WlsJdbcInstanceInfo(){	  
	}
  public long getActiveConn() {
    return activeConn;
  }

  public void setActiveConn(long activeConn) {
    this.activeConn = activeConn;
  }

  public long getWaitingConn() {
    return waitingConn;
  }

  public void setWaitingConn(long waitingConn) {
    this.waitingConn = waitingConn;
  }

  public long getMaxActiveConn() {
    return maxActiveConn;
  }

  public void setMaxActiveConn(long maxActiveConn) {
    this.maxActiveConn = maxActiveConn;
  }

  public long getMaxWaitingConn() {
    return maxWaitingConn;
  }

  public void setMaxWaitingConn(long maxWaitingConn) {
    this.maxWaitingConn = maxWaitingConn;
  }
  public Element toXMLElement(String name) {
    Element cond_e = new Element(name);
    cond_e.setAttribute("field", getField());
    cond_e.setAttribute("type", String.valueOf(getType()));
    cond_e.setAttribute("maxActiveConn", String.valueOf(getMaxActiveConn()));
    cond_e.setAttribute("maxWaitingConn", String.valueOf(getMaxWaitingConn()));
    return cond_e;
  }
  
  public void putPropertys(Element condition_e) {
    if (condition_e == null) {
      return;
    }
    Element e = condition_e;
    setField(e.getAttributeValue("field"));
    try {
      setType(Integer.parseInt(e.getAttributeValue("type")));
    } catch (NumberFormatException ex) {
    }
    setMaxActiveConn(Integer.parseInt(e.getAttributeValue("maxActiveConn")));
    setMaxWaitingConn(Integer.parseInt(e.getAttributeValue("maxWaitingConn")));
  }
  public long getTotalConn() {
    return totalConn;
  }
  public void setTotalConn(long totalConn) {
    this.totalConn = totalConn;
  }
  public long getMaxActivedConn() {
    return maxActivedConn;
  }
  public void setMaxActivedConn(long maxActivedConn) {
    this.maxActivedConn = maxActivedConn;
  }
  public long getMaxWaitConn() {
    return maxWaitConn;
  }
  public void setMaxWaitConn(long maxWaitConn) {
    this.maxWaitConn = maxWaitConn;
  }
  public long getMaxWaitingTime() {
    return maxWaitingTime;
  }
  public void setMaxWaitingTime(long maxWaitingTime) {
    this.maxWaitingTime = maxWaitingTime;
  }
  public long getCapacity() {
    return capacity;
  }
  public void setCapacity(long capacity) {
    this.capacity = capacity;
  }
}
