package com.broada.carrier.monitor.impl.stdsvc.smtp;

import com.broada.carrier.monitor.impl.common.BaseMonitorParameter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * SMTP 监测参数实体类
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class SMTPParameter extends BaseMonitorParameter {
	private static final long serialVersionUID = 1L;

  public SMTPParameter() {
  }

  public SMTPParameter(String str) {
    super(str);
  }

  public void setPort(int port) {
    set("port", String.valueOf(port));
  }

  public int getPort() {
    return get("port", 25);
  }

  public void setTimeout(int timeout) {
    set("timeout", String.valueOf(timeout));
  }

  public int getTimeout() {
    return get("timeout", 5000);
  }

  public void setWonted(int alarm) {
    set("wonted", String.valueOf(alarm));
  }

  public int getWonted() {
    return get("wonted", 1);
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
