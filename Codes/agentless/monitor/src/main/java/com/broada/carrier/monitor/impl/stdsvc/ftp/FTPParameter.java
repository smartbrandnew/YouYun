package com.broada.carrier.monitor.impl.stdsvc.ftp;

import com.broada.carrier.monitor.impl.common.BaseMonitorParameter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * FTP 监测参数实体类
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */
public class FTPParameter extends BaseMonitorParameter {
	private static final long serialVersionUID = 1L;

	public FTPParameter() {
  }

  public FTPParameter(String str) {
    super(str);
  }

  public void setPort(int port) {
    set("port", String.valueOf(port));
  }

  public int getPort() {
    return get("port", 21);
  }

  public void setAnonymous(boolean b) {
    if (b) {
      set("anonymous", Boolean.toString(b));
    } else {
      remove("anonymous");
    }
  }

  public boolean isAnonymous() {
    return get("anonymous", true);
  }

  public void setNotAnonymous(boolean b) {
    if (b) {
      set("notanonymous", Boolean.toString(b));
    } else {
      remove("notanonymous");
    }
  }

  public boolean isNotAnonymous() {
    return get("notanonymous", false);
  }

  public void setChkLogin(boolean b) {
    if (b) {
      set("login", Boolean.toString(b));
    } else {
      remove("login");
      setUser(null);
    }
  }

  public boolean isChkLogin() {
    return get("login", false);
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

  /**
   * 设置要检查的文件url
   *
   * url  为空时表示不选择该选项
   *
   * @param url
   */
  public void setFilename(String url) {
    if (url == null || url.trim().length() == 0) {
      remove("filename");
    } else {
      set("filename", url);
    }
  }

  public String getFilename() {
    return (String) get("filename");
  }

  @JsonIgnore
  public boolean isChkFile() {
    return getFilename() != null;
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
