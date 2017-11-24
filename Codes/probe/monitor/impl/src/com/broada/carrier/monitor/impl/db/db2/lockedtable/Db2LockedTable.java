package com.broada.carrier.monitor.impl.db.db2.lockedtable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * DB2锁的实体类
 * 
 * @author Wangx
 * 
 * 2008-8-13
 */
@SuppressWarnings("unchecked")
public class Db2LockedTable {
  /**
   * 锁模式对照表
   */
  private final static Map lockModeConvertor = new HashMap();

  static {
    lockModeConvertor.put("0", "No Lock");
    lockModeConvertor.put("1", "Intention Share Lock");
    lockModeConvertor.put("2", "Intention Exclusive Lock");
    lockModeConvertor.put("3", "Share Lock");
    lockModeConvertor.put("4", "Share with Intention Exclusive Lock");
    lockModeConvertor.put("5", "Exclusive Lock");
    lockModeConvertor.put("6", "Intent None (For Dirty Read)");
    lockModeConvertor.put("7", "Super Exclusive Lock");
    lockModeConvertor.put("8", "Update Lock");
    lockModeConvertor.put("9", "Next-key Share Lock");
    lockModeConvertor.put("10", "Next-key Exclusive Lock");
    lockModeConvertor.put("11", "Weak Exclusive Lock");
    lockModeConvertor.put("12", "Next-key Weak Exclusive Lock");
  }

  /**
   * 锁状态对照表
   */
  private final static Map lockStatusConvertor = new HashMap();

  static {
    lockStatusConvertor.put("1", "Granted State");
    lockStatusConvertor.put("2", "Converting State");
  }

  private String tableName;

  private String tableSchema;

  private String tableSpaceName;

  private String lockMode;

  private String lockStatus;

  /**
   * 行号，做为Instance的Key
   */
  private String rowNumber;

  public String getRowNumber() {
    return rowNumber;
  }

  public void setRowNumber(String rowNumber) {
    this.rowNumber = rowNumber;
  }

  public String getRawLockMode() {
    return lockMode;
  }

  public String getLockMode() {
    return (String) lockModeConvertor.get(lockMode);
  }
  
  public static String getLockModeNum(String value){
  	for (Iterator iterator = lockModeConvertor.entrySet().iterator(); iterator.hasNext();) {
			Entry type = (Entry) iterator.next();
			if(value.equals(type.getValue())){
				return type.getKey().toString();
			}
		}
  	return "";
  }

  public void setLockMode(String lockMode) {
    this.lockMode = lockMode;
  }

  public String getRawLockStatus() {
    return lockStatus;
  }

  public String getLockStatus() {
    return (String) lockStatusConvertor.get(lockStatus);
  }
  public static String getLockStatusNum(String value){
  	for (Iterator iterator = lockStatusConvertor.entrySet().iterator(); iterator.hasNext();) {
			Entry type = (Entry) iterator.next();
			if(value.equals(type.getValue())){
				return type.getKey().toString();
			}
		}
  	return "";
  }
  

  public void setLockStatus(String lockStatus) {
    this.lockStatus = lockStatus;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getTableSchema() {
    return tableSchema;
  }

  public void setTableSchema(String tableSchema) {
    this.tableSchema = tableSchema;
  }

  public String getTableSpaceName() {
    return tableSpaceName;
  }

  public void setTableSpaceName(String tableSpaceName) {
    this.tableSpaceName = tableSpaceName;
  }
}
