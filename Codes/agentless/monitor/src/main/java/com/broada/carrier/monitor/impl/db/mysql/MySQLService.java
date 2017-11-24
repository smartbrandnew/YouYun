package com.broada.carrier.monitor.impl.db.mysql;

import java.util.List;
import java.util.Map;

/**
 * 封装了获取MySQL数据库和表信息的方法。作为一个Facade。
 *
 * @author zhoucy (zhoucy@broada.com.cn)
 * @Email : zhoucy@broada.com
 * @Create By 2006-7-27 上午10:53:29
 */
public interface MySQLService {

  /**
   * 初始化数据库
   *
   * @return
   * @throws MySQLException
   */
  public boolean initConnection() throws MySQLException;

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

  /**
   * 获取所有数据的大小
   *
   * @return
   */
  public Map<String, Long> getAllDatabaseSize();

  /**
   * 关闭连接并释放资源
   * 
   */
  public void close();
}