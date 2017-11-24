package com.broada.carrier.monitor.impl.db.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.db2.lock.Db2Lock;
import com.broada.carrier.monitor.impl.db.db2.lockedtable.Db2LockedTable;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.utils.JDBCUtil;

@SuppressWarnings("unchecked")
public class Db2LockManager extends Db2Manager {
  private static final Log logger = LogFactory.getLog(Db2LockManager.class);
  
  private static final String SQL_LOCKED_TAB = " SELECT * FROM (SELECT coalesce(table_name,'null') as TABLE_NAME,"
      + "coalesce(table_schema,'null') as TABLE_SCHEMA,"
      + "coalesce(tablespace_name,'null') as TABLESPACE_NAME,"
      + "LOCK_MODE,LOCK_STATUS,row_number()over() ROW_NUMBER FROM TABLE (SNAPSHOT_LOCK  ('#db_sid#', -1) ) AS SNAPSHOT_LOCK order by table_name,table_schema,tablespace_name)t where t.ROW_NUMBER<= #row_number#";

  private static final String SQL_LOCK_INFO = 
		  "SELECT deadlocks, lock_escals, locks_waiting,  appls_cur_cons FROM TABLE(SNAPSHOT_DATABASE('#db_sid#', -1)) AS SNAPSHOT_DATABASE";
  private static final String SQL_LOCK_INFO_NEW = 
		  "SELECT deadlocks, lock_escals, locks_waiting,  appls_cur_cons FROM TABLE(SNAP_GET_DB('#db_sid#', -1)) AS SNAP_GET_DB";

  private static Map<String,Double> mapDeadlocks = new HashMap<String,Double>();
  
  /**
   * 这个变量用于保存上一次保存的值 
   */
  private Db2Lock lastValue = null;
  
  public final Map lockBeanMap = new HashMap();

  {
    lockBeanMap.put("TABLE_NAME", "tableName");
    lockBeanMap.put("TABLE_SCHEMA", "tableSchema");
    lockBeanMap.put("TABLESPACE_NAME", "tableSpaceName");
    lockBeanMap.put("LOCK_MODE", "lockMode");
    lockBeanMap.put("LOCK_STATUS", "lockStatus");
    lockBeanMap.put("ROW_NUMBER", "rowNumber");
  }

  public Db2LockManager(String ip, DB2MonitorMethodOption option) {
    super(ip, option);
  }

  private List getLockedTablesListByMap(Map beanMap, String sqlStr, Class clazz) throws Exception {
    List db2tsList = new ArrayList();
    String sql = sqlStr.replaceAll("#db_sid#", option.getDb()).replaceAll("#row_number#", getMaxLocktable());
    if (this.option.getOptType().equalsIgnoreCase(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD)) {
      List result = new DB2AgentExecutor(ip).execute(ip, option, CLIConstant.DB2AGENT,
          new String[] { option.getUsername(), option.getDb(), sql });
      if (result != null && result.size() > 0) {
        for (int i = 0; i < result.size(); i++) {
        	Object temp = result.get(i);
        	if (temp instanceof Map) {
            Map map = (Map) result.get(i);
            Object obj = clazz.newInstance();
            for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
              String colName = iter.next().toString();
              BeanUtils.setProperty(obj, (String) beanMap.get(colName), map.get(colName));
            }
            db2tsList.add(obj);
        	} else if (temp instanceof String) {
        		DB2CmdTable table = new DB2CmdTable((String)temp);
        		
        		for (int j = 0; j < table.getRowsSize(); j++) {
							Object obj = clazz.newInstance();
							for (int c = 0; c < table.getColumnsSize(); c++) {									
								String colName = table.getColumn(c);
								BeanUtils.setProperty(obj, (String) beanMap.get(colName), table.getData(j, c));
							}
							db2tsList.add(obj);
        		}
        	} else 
        		throw new IllegalArgumentException(String.format("无法识别的数据类型：%s", temp));                    	
        }
      }
    } else {
      Connection connection = getConnectionIgnoreRole();
      PreparedStatement ps = null;
      ResultSet rs = null;
      ResultSetMetaData rsm = null;
      try {
        ps = connection.prepareStatement(sql);
        rs = ps.executeQuery();
        rsm = ps.getMetaData();
        while (rs.next()) {
          Object obj = clazz.newInstance();
          for (int i = 1, size = rsm.getColumnCount(); i <= size; i++) {
            String colName = rsm.getColumnName(i);
            BeanUtils.setProperty(obj, (String) beanMap.get(colName), rs.getObject(colName));
          }
          db2tsList.add(obj);
        }
      } catch (Throwable t) {
        throw new Exception(t);
      } finally {
        JDBCUtil.close(connection);
      }
    }
    return db2tsList;
  }

  public List getLockedTablesList() throws Exception {
    return getLockedTablesListByMap(lockBeanMap, SQL_LOCKED_TAB, Db2LockedTable.class);
  }
  
  public Db2Lock getLockInfo(String resourceId) throws Exception {
  	Db2Lock info = new Db2Lock();
    String sql = (option.ifNewVersion() ? SQL_LOCK_INFO_NEW : SQL_LOCK_INFO).replaceAll("#db_sid#", option.getDb());
    if (this.option.getOptType().equalsIgnoreCase(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD)) {
      List result = new DB2AgentExecutor(ip).execute(ip, option, CLIConstant.DB2AGENT,
          new String[] { option.getUsername(), option.getDb(), sql });
      if (result != null && result.size() > 0) {
      	Object temp = result.get(0);
      	Map map;
      	if (temp instanceof Map) {
	        map = (Map) result.get(0);
      	} else if (temp instanceof String) {
      		DB2CmdTable table = new DB2CmdTable((String)temp);
      		map = new HashMap<String, String>();
      		for (int i = 0; i < table.getColumnsSize(); i++) {
      			map.put(table.getColumn(i), table.getData(0, i));
      		}
      	} else 
      		throw new IllegalArgumentException(String.format("无法识别的数据类型：%s", temp));  
      	
      	Double currDeadlocksSummary = map.get("DEADLOCKS") == null ? 0D : Double.valueOf(map.get("DEADLOCKS").toString());
        String key = (resourceId+"?"+option.getDb()).trim();
        Double lastDeadlocksSummary = mapDeadlocks.get(key);
        if (lastDeadlocksSummary == null)
        	lastDeadlocksSummary = currDeadlocksSummary;          
        mapDeadlocks.put(key, currDeadlocksSummary);               
        info.setDeadLockCnt(currDeadlocksSummary - lastDeadlocksSummary);
        info.setEscalLockCnt(map.get("LOCK_ESCALS") == null ? 0D : Double.valueOf(map.get("LOCK_ESCALS").toString()));
        info.setWaitingLockCnt(map.get("LOCKS_WAITING") == null ? 0D
            : Double.valueOf(map.get("LOCKS_WAITING").toString()));
        info.setCurrAppCnt(map.get("APPLS_CUR_CONS") == null ? 0D
            : Double.valueOf(map.get("APPLS_CUR_CONS").toString()));
        info.setTime(System.currentTimeMillis());
      }
    } else {
      Connection connection = getConnectionIgnoreRole();
      PreparedStatement ps = null;
      ResultSet rs = null;
      try {
        ps = connection.prepareStatement(sql);
        rs = ps.executeQuery();
        if (rs.next()) {        
        	Double currDeadlocksSummary = rs.getDouble("deadlocks");        	
          String key = (resourceId+"?"+option.getDb()).trim();
          Double lastDeadlocksSummary = mapDeadlocks.get(key);
          if (lastDeadlocksSummary == null)
          	lastDeadlocksSummary = currDeadlocksSummary;          
          mapDeadlocks.put(key, currDeadlocksSummary);          
          info.setDeadLockCnt(currDeadlocksSummary - lastDeadlocksSummary);
          
          info.setEscalLockCnt(rs.getDouble("lock_escals"));
          info.setWaitingLockCnt(rs.getDouble("locks_waiting"));
          info.setCurrAppCnt(rs.getDouble("appls_cur_cons"));
          info.setTime(System.currentTimeMillis());
        }

      } catch (Throwable t) {
        throw new Exception(t);
      } finally {
        JDBCUtil.close(connection);
      }
    }
    calcLockRatio(info);

    return info;
  }

  /**
   * 计算死锁率,锁定升级率
   * @param lockInfo
   */
  protected void calcLockRatio(Db2Lock lockInfo){

    if(lastValue==null){
      //第一次监测
      lockInfo.setEscalLockRatio(new Double(0));
    }else{
      
      double margin = System.currentTimeMillis() - lastValue.getTime();
      
      double currEscal = lockInfo.getEscalLockCnt().doubleValue();
      double lastEscal = lastValue.getEscalLockCnt().doubleValue();
      
      if(currEscal<lastEscal){
        //DB2有重启过
        lastEscal = 0;
      }
      
      if (margin <= 0) {
        lockInfo.setEscalLockRatio(lastValue.getEscalLockRatio());
      } else {
        double ratio = (currEscal-lastEscal)*(1000D/margin);
        double ORACLE_DOUBLE_MAX_VALUE = 9.99999999999999999999999999999999999999e125;
        if (ratio > ORACLE_DOUBLE_MAX_VALUE)
          ratio = ORACLE_DOUBLE_MAX_VALUE;
        lockInfo.setEscalLockRatio(new Double(ratio));
      }
    }
    
    lastValue=lockInfo;    
  }

  /*
   * 获取到锁表信息的最大值（默认100）
   */
  public static String  getMaxLocktable(){
	  int max = 100;
	  String text = System.getProperty("monitor.db2.locktable.max");
	  if(text != null){
		  try{//防止用户配置的monitor.db2.locktable.max不为整数
			  max = Integer.parseInt(System.getProperty("monitor.db2.locktable.max"));
		  }catch(Exception e){
			  logger.warn(String.format("配置项monitor.db2.locktable.max的值[%s]失败,将使用默认值100。错误:%s",text,e));
			  logger.debug("堆栈：", e);
		  }
	  }
	  return max+"";
  }

  @Override
  public void setOption(DB2MonitorMethodOption option) {
    if (!StringUtils.equals(this.option.getDb(), option.getDb()) || !StringUtils.equals(this.option.getUsername(), option.getUsername())
    		|| !StringUtils.equals(this.option.getPassword(), option.getPassword())) {
      lastValue = null;
      super.setOption(option);
    }
  }
}