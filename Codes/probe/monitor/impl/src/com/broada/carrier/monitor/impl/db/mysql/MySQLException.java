package com.broada.carrier.monitor.impl.db.mysql;

import java.sql.SQLException;

/**
 * 
 * @author zhoucy (zhoucy@broada.com.cn) Email : zhoucy@broada.com Create By 2006-7-27 上午11:17:27
 */
public class MySQLException extends Exception {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 607449736234450118L;
  private String message=null;
  private int errorCode=-1;
  //用户名或密码错误
  public static final int ERRCODE_USERPWD=1045;

  
  public MySQLException(String message) {
    super(message);
    this.message=message;
  }

  public MySQLException(SQLException e) {
    super(e);
    errorCode=e.getErrorCode();
    switch (errorCode) {
    case 0:
      message = "MySQL服务器地址或者端口不正确";
      break;
    case 1045:
      message = "用户名或者密码错误";
      break;
    case 1130:
      message = "MySQL不允许当前主机连接";
      break;
    default:
      message = "未知的数据库错误";
    }
  }
  public MySQLException(String message,Throwable e) {
    super(message,e);
    this.message=message;
  }
  
  public String getLocalizedMessage() {
    return message==null?super.getLocalizedMessage():message;
  }

  public String getMessage() {
    return message==null?super.getMessage():message;
  }

  public String toString() {
    return message==null?super.toString():message;
  }

  public int getErrorCode() {
    return errorCode;
  }

}
