package com.broada.carrier.monitor.impl.db.postgresql;

import java.sql.SQLException;

public class PostgreSQLException extends Exception {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 607449736234450118L;
  private String message=null;
  private String sqlState="-1";
  //用户名或密码错误
  public static final String ERRCODE_USERPD="28000";

  
  public PostgreSQLException(String message) {
    super(message);
    this.message=message;
  }

  public PostgreSQLException(SQLException e) {
    super(e);
    sqlState=e.getSQLState();
    message = e.getMessage();
  }
  public PostgreSQLException(String message,Throwable e) {
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

  public String getSQLState() {
    return sqlState;
  }

}
