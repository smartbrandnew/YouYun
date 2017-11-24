package com.broada.carrier.monitor.impl.stdsvc.http;

import com.broada.carrier.monitor.impl.common.BaseMonitorParameter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * HTTP 监测参数实体类
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */
public class HTTPParameter extends BaseMonitorParameter {
	private static final long serialVersionUID = 1L;

  public HTTPParameter() {
  }

  public HTTPParameter(String str) {
    super(str);
  }

  private static final int DEFAULT_REPLYTIME = 5;
  
  public void setPort(int port) {
    set("port", String.valueOf(port));
  }

  public int getPort() {
    return get("port", 80);
  }

  public void setURL(String url) {
    if (url == null) {
      url = "/";
    } else if (!url.startsWith("/")) {
      url = "/" + url;
    }
    set("url", url);
  }

  public String getURL() {
    String url = (String) get("url");
    if (url == null) {
      url = "/";
    } else if (!url.startsWith("/")) {
      url = "/" + url;
    }
    return url;
  }

  public void setDomain(String domain) {
    if (domain == null || domain.trim().length() == 0) {
      remove("domain");
    } else {
      set("domain", domain);
    }
  }

  public String getDomain() {
    return (String) get("domain");
  }

  @JsonIgnore
  public boolean isChkDomain() {
    return getDomain() != null;
  }

  public void setUsername(String username) {
    if (username == null || username.trim().length() == 0) {
      remove("username");
    } else {
      set("username", username);
    }
  }

  public String getUsername() {
    return (String) get("username");
  }

  public void setPassword(String password) {
    if (password == null || password.trim().length() == 0) {
      remove("password");
    } else {
      set("password", password);
    }
  }

  public String getPassword() {
    return (String) get("password");
  }

  public void setRealm(String realm) {
    if (realm == null || realm.trim().length() == 0) {
      remove("realm");
    } else {
      set("realm", realm);
    }
  }

  public String getRealm() {
    return (String) get("realm");
  }

  @JsonIgnore
  public boolean isChkAuth() {
    return getUsername() != null;
  }

  /**
   * 设置是否检查状态返回值等于200
   * @param chk
   */
  public void setChkStatusCode(boolean chk) {
    set("status", Boolean.toString(chk));
  }

  public boolean isChkStatusCode() {
    return get("status", true);
  }

  /**
   * 设置页面内容不包含的文本值
   *
   * txt  为空时表示不选择该选项
   *
   * @param txt
   */
  public void setNotContain(String txt) {
    if (txt == null || txt.trim().length() == 0) {
      remove("notcontain");
    } else {
      set("notcontain", txt);
    }
  }

  public String getNotContain() {
    return (String) get("notcontain");
  }

  @JsonIgnore
  public boolean isChkNotContain() {
    return getNotContain() != null;
  }

  /**
   * 设置页面内容包含的文本值
   *
   * txt  为空时表示不选择该选项
   *
   * @param txt
   */
  public void setContain(String txt) {
    if (txt == null || txt.trim().length() == 0) {
      remove("contain");
    } else {
      set("contain", txt);
    }
  }

  public String getContain() {
    return (String) get("contain");
  }

  @JsonIgnore
  public boolean isChkContain() {
    return getContain() != null;
  }
  
  public void setReplyTime(int replyTim) {
    if(replyTim == -1){
      remove("replyTime");
    }else{
    set("replyTime", String.valueOf(replyTim));
    }
  }

  public int getReplyTime() {
    //为兼容老版本监测器配置,设置默认值
    if(get("replyTime", DEFAULT_REPLYTIME) == Integer.MIN_VALUE){
      return DEFAULT_REPLYTIME;
    }
    return get("replyTime", DEFAULT_REPLYTIME);
  }

  @JsonIgnore
  public boolean isChkReplyTime() {
    return getReplyTime() != Integer.MIN_VALUE;
  }
  
}
