package com.broada.carrier.monitor.impl.mw.apache;

import com.broada.carrier.monitor.impl.common.BaseMonitorParameter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * HTTP 监测参数实体类
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang,amyson
 * @version 1.0
 */

public class ApacheParameter extends BaseMonitorParameter {
	private static final long serialVersionUID = 1L;
	public static final String CON_RET_CODE = "returnCode";//检查返回码是否200
	public static final String CON_REPLY_TIME = "replyTime";//检查本次请求的响应时间是否小于等于某个值
	public static final String CON_CPU_LOAD = "cpuLoad";//检查CPU负载是否小于等于某个值
	public static final String CON_RPS = "requestPsec";//检查每秒请求数是否小于等于某个值

  /**
   * 默认创建
   */
  protected void create() {
    set("port", 80); //端口
    set("url", "/");
    set("status", true); //是否监测返回状态码
  }

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
  //设置是否使用SSL协议,传入任意非NULL值表示使用SSL,反之亦然
  public void setUseSSL(String useSSL){
    if(useSSL == null){
      remove("useSSL");
    }else{
      set("useSSL",useSSL);
    }
  }
  //判断是否使用SSL协议
  public boolean isUseSSL(){
    return get("useSSL", false);
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

  public String getNotContain() {
    return (String) get("notcontain");
  }
  
  public void setNotContain(String value) {
  	set("notcontain", value);
  }

  @JsonIgnore
  public boolean isChkNotContain() {
    return getNotContain() != null;
  }  
}
