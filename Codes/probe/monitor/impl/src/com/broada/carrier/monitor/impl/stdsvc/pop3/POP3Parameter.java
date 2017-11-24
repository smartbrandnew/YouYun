package com.broada.carrier.monitor.impl.stdsvc.pop3;

import com.broada.carrier.monitor.impl.common.BaseMonitorParameter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * POP3 监测参数实体类
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class POP3Parameter extends BaseMonitorParameter {
	private static final long serialVersionUID = 1L;

  public POP3Parameter() {
  }

  public POP3Parameter(String str) {
    super(str);
  }

  public void setPort(int port) {
    set("port", String.valueOf(port));
  }

  public int getPort() {
    return get("port", 110);
  }

  public void setTimeout(int timeout) {
    set("timeout", String.valueOf(timeout));
  }

  public int getTimeout() {
    return get("timeout", 5000);
  }

  /**
   * 如果等于Integer.MIN_VALUE表示不设置
   * @param count
   */
  public void setMailCount(int count) {
    if (count == Integer.MIN_VALUE) {
      remove("mailcount");
    } else {
      set("mailcount", String.valueOf(count));
    }
  }

  public int getMailCount() {
    return get("mailcount", Integer.MIN_VALUE);
  }

  @JsonIgnore
  public boolean isChkMailCount() {
    return getMailCount() != Integer.MIN_VALUE;
  }

  /**
   * 如果等于NaN表示不设置
   * 单位 Mb
   * @param size
   */
  public void setBoxSize(double size) {
    if (Double.isNaN(size)) {
      remove("boxsize");
    } else {
      set("boxsize", String.valueOf(size));
    }
  }

  /**
   * 单位 Mb
   * @return
   */
  public double getBoxSize() {
    return get("boxsize", Double.NaN);
  }

  @JsonIgnore
  public boolean isChkBoxSize() {
    return!Double.isNaN(getBoxSize());
  }

  public void setUser(String user) {
    if (user == null || user.trim().length() == 0) {
      remove("user");
      remove("password");
    } else {
      set("user", user);
    }
  }

  public String getUser() {
    return (String) get("user");
  }

  public void setPassword(String password) {
    set("password", password);
  }

  public String getPassword() {
    return (String) get("password");
  }
  
  public void setReplyTime(int replyTim) {
    if (replyTim == -1) {
      remove("replyTime");
    } else {
      set("replyTime", String.valueOf(replyTim));
    }
  }

  public int getReplyTime() {
    return get("replyTime", Integer.MIN_VALUE);
  }

  @JsonIgnore
  public boolean isChkReplyTime() {
    return getReplyTime() != Integer.MIN_VALUE;
  }  
}
