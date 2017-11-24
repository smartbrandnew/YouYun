package com.broada.carrier.monitor.impl.db.mysql;

import java.util.List;

/**
 * 获取与表相关的信息
 *
 * @author zhoucy (zhoucy@broada.com.cn)
 * @Email : zhoucy@broada.com
 * @Create By 2006-7-27 上午11:01:02
 */
public interface MySQLTableInfoRetriever {
  /**
   * 获取指定数据中所有表的状态信息
   * @param databaseName
   * @return
   */
  public List getTableStatusInfos(String databaseName);

  /**
   * 获取指定数据指定表的状态信息
   * @param databaseName
   * @param tableName
   * @return
   */
  public TableStatus getTableStatusInfo(String databaseName, String tableName);
}
