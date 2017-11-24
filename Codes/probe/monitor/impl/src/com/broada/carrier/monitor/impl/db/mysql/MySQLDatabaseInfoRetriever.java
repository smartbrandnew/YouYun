package com.broada.carrier.monitor.impl.db.mysql;

import java.util.List;
import java.util.Map;

/**
 * 获取与整个MySQL数据库相关的信息
 *
 * @author zhoucy (zhoucy@broada.com.cn)
 * @Email : zhoucy@broada.com
 * @Create By 2006-7-27 上午11:02:40
 */
public interface MySQLDatabaseInfoRetriever {
  /**
   * 获取MySQL数据库状态，返回的状态名和状态值对，所有的状态名都是大写的
   *
   * @return java.util.Map
   */
  public Map getAllStatus();

  /**
   * 获取MySQL数据库变量，返回的变量名和值对，所有的变量名都是大写的
   *
   * @return java.util.Map
   */
  public Map getAllVariable();

  /**
   * 返回所有数据库的名称列表
   *
   * @return
   */
  public List showDatabases();

  /**
   * 获取指定的状态值
   */
  public String getStatus(String statusName);

  /**
   * 获取指定的变量
   * 
   * @param varName
   * @return
   */
  public String getVariable(String varName);
}
