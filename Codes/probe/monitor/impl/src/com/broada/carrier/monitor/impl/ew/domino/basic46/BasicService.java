package com.broada.carrier.monitor.impl.ew.domino.basic46;

import java.sql.Timestamp;

public interface BasicService {
  
  public boolean isConnected();

  public double getFirstDocDoubleValue(String dbName, String viewName, String itemName) throws Domino46Exception;
  
  public int getFirstDocIntValue(String dbName, String viewName, String itemName) throws Domino46Exception;
  
  public Timestamp getFirstDocTimestampValue(String dbName, String viewName, String itemName) throws Domino46Exception;
  
  public String getFirstDocStringValue(String dbName, String viewName, String itemName) throws Domino46Exception;
  
  public double getDBPercentUsed(String dbName) throws Domino46Exception;
  
  public String[] getDBList() throws Domino46Exception;
  
  //public Object getSessionValue(String sessionMethodName,Object[] args);
  
  //public Object getDatabaseValue(String dbName,String dbMethodName,Object[] args);
  
  //public Object getViewValue(String dbName,String viewName,String viewMethodName,Object[] args);
  
}
