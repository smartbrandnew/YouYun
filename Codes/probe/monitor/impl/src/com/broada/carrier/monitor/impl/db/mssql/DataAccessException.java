package com.broada.carrier.monitor.impl.db.mssql;

public class DataAccessException extends Exception {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -7544462211427553865L;

  public DataAccessException(String message){
   super(message); 
  }
  
  public DataAccessException(String message, Throwable t){
    super(message, t); 
   }
}
