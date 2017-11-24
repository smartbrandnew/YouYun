package com.broada.carrier.monitor.impl.db.db2;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import com.broada.carrier.monitor.impl.db.db2.bp.DbBufferPool;
import com.broada.carrier.monitor.impl.db.db2.sort.DBSort;
import com.broada.carrier.monitor.method.cli.error.CLIException;

/**
 * DB2数据库管理扩展类接口
 * @author 杨帆
 * 
 */
public interface DB2ExtendManager {
  /**
   * 获取缓冲池数据
   * @return
   * @throws SQLException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public DbBufferPool getBufferPoolData() throws SQLException, InstantiationException, IllegalAccessException,
      InvocationTargetException,CLIException;

  /**
   * 获取排序数据
   * @return
   * @throws SQLException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public DBSort getDBSortData() throws SQLException, InstantiationException, IllegalAccessException,
      InvocationTargetException,CLIException;
}
