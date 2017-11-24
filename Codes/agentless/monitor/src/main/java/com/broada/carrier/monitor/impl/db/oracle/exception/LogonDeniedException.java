package com.broada.carrier.monitor.impl.db.oracle.exception;

import java.sql.SQLException;

/**
 * 数据库登陆拒绝异常
 * 
 * @author lixy (lixy@broada.com) Create By Oct 22, 2007 3:08:27 PM
 */
public class LogonDeniedException extends SQLException {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = 1L;

  public LogonDeniedException(String message) {
    super(message);
  }
}
