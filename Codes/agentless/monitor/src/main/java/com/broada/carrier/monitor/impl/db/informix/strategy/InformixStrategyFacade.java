package com.broada.carrier.monitor.impl.db.informix.strategy;

import com.broada.carrier.monitor.impl.db.informix.strategy.entity.InformixStrategy;
import com.broada.carrier.monitor.impl.db.informix.strategy.entity.InformixStrategyGroup;
import com.broada.carrier.monitor.impl.db.informix.strategy.entity.InformixStrategyResult;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * 策略门面，一般的访问，通过访问这个类就可以了。
 * 
 * @author lixy Sep 3, 2008 4:29:31 PM
 */
public class InformixStrategyFacade {
  /**
   * 获取策略组
   * @param groupId
   * @return
   */
  public static InformixStrategyGroup getStrategyGroup(String groupId) {
    return InformixStrategyLoader.getStrategyGroup(groupId);
  }

  /**
   * 获取某个策略组中的总的策略数
   * @param groupId
   * @return
   */
  public static int getStrategyCount(String groupId) {
    return getStrategyGroup(groupId).getStrategies().size();
  }

  /**
   * 策略组名称获取
   * @param groupId
   * @return
   */
  public static String getStrategyGroupName(String groupId) {
    return getStrategyGroup(groupId).getName();
  }

  /**
   * 获取策略组描述信息
   * @param groupId
   * @return
   */
  public static String getStrategryGroupDesc(String groupId) {
    InformixStrategyGroup group = getStrategyGroup(groupId);
    return group.getDesc();
  }

  /**
   * 获取策略组sql语句
   * @param groupId
   * @return
   */
  public static String getStrategyGroupSql(String groupId) {
    InformixStrategyGroup group = getStrategyGroup(groupId);
    return group.getSql();
  }

  /**
   * 获取特定策略
   * @param groupId
   * @param itemCode
   * @return
   */
  public static InformixStrategy getStrategy(String groupId, String itemCode) {
    InformixStrategyGroup group = getStrategyGroup(groupId);
    InformixStrategy strategy = group.getStrategy(itemCode);
    if (strategy == null) {
      throw new RuntimeException("策略：" + groupId + "-->" + itemCode + "不存在.");
    }
    return strategy;
  }

  public static String getStrategyName(String groupId, String itemCode) {
    InformixStrategy strategy = getStrategy(groupId, itemCode);
    return strategy.getName();
  }

  /**
   * 获取策略组下所有策略的当前结果集
   * @param conn
   * @param groupId
   * @return
   * @throws SQLException
   */
  public static InformixStrategyResult getStrategyResult(Connection conn, String groupId, String srvId) throws SQLException {
    InformixStrategyCurrValueGettor executor = new InformixStrategyCurrValueGettor(conn);
    return executor.getStrategyResult(groupId, srvId);
  }
}
