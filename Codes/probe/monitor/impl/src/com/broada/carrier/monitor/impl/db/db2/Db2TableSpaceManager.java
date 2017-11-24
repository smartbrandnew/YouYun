package com.broada.carrier.monitor.impl.db.db2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import com.broada.carrier.monitor.impl.db.db2.tablespace.Db2TableSpace;
import com.broada.carrier.monitor.impl.db.db2.tablespacecont.Db2TableSpaceContainer;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.utils.JDBCUtil;

public class Db2TableSpaceManager extends Db2Manager {

  private static final String SQL_TABLESPACE = "select TABLESPACE_NAME,PAGE_SIZE,TOTAL_PAGES,USED_PAGES,FREE_PAGES,"
    +"case TABLESPACE_TYPE when 1 then 'SMS' else 'DMS' end TYPE,PREFETCH_SIZE,EXTENT_SIZE,TABLESPACE_STATE  FROM TABLE(SNAPSHOT_TBS_CFG('#db_sid#', -1)) as SNAPSHOT_TBS_CFG";
  
  private static final String SQL_TABLESPACE_NEW = "select t1.TBSP_NAME as TABLESPACE_NAME, t1.TBSP_PAGE_SIZE as PAGE_SIZE, t1.TBSP_TYPE as TYPE, t1.TBSP_EXTENT_SIZE as EXTENT_SIZE,"
  		+ "t2.TBSP_TOTAL_PAGES as TOTAL_PAGES, t2.TBSP_USED_PAGES as USED_PAGES, t2.TBSP_FREE_PAGES as FREE_PAGES, t2.TBSP_PREFETCH_SIZE as PREFETCH_SIZE, t2.TBSP_STATE as TABLESPACE_STATE "
  		+ "from TABLE(SNAP_GET_TBSP('#db_sid#', -1)) as t1 join TABLE(SNAP_GET_TBSP_PART('#db_sid#', -1)) as t2 on t1.TBSP_ID = t2.TBSP_ID";

  private static final String SQL_TABLESPACECONTAINER = "SELECT RTRIM(TABLESPACE_NAME) TABLESPACE_NAME,RTRIM(CONTAINER_NAME) CONTAINER_NAME, "
    +"(CASE WHEN CONTAINER_TYPE = 0 THEN 'SMS DIRECTORY' WHEN CONTAINER_TYPE = 6 THEN 'DMSFILE' ELSE 'DMS DEVICE' END) AS CONTAINER_TYPE,USABLE_PAGES,TOTAL_PAGES "
    +"FROM TABLE (SNAPSHOT_CONTAINER ('#db_sid#', -1) ) AS SNAPSHOT_CONTAINER";
  
  private static final String SQL_TABLESPACECONTAINER_NEW = "SELECT TBSP_NAME as TABLESPACE_NAME, CONTAINER_NAME, CONTAINER_TYPE, USABLE_PAGES, TOTAL_PAGES "
      +"FROM TABLE (SNAP_GET_CONTAINER ('#db_sid#', -1) ) AS SNAP_GET_CONTAINER";
  
  public static final Map<String, String> tbsBeanMap = new HashMap<String, String>();
  static {
    tbsBeanMap.put("TABLESPACE_NAME", "name");
    tbsBeanMap.put("PAGE_SIZE", "pageSize");
    tbsBeanMap.put("TOTAL_PAGES", "totalPages");
    tbsBeanMap.put("USED_PAGES", "usedPages");
    tbsBeanMap.put("FREE_PAGES", "freePages");
    tbsBeanMap.put("TYPE", "type");
    tbsBeanMap.put("PREFETCH_SIZE", "prefetchSize");
    tbsBeanMap.put("EXTENT_SIZE", "extentSize");
    tbsBeanMap.put("TABLESPACE_STATE", "tablespaceState");
    
    
  }
  
  public static final Map<String, String> tbsContBeanMap = new HashMap<String, String>();
  static {
    tbsContBeanMap.put("TABLESPACE_NAME", "tableSpaceName");
    tbsContBeanMap.put("CONTAINER_NAME", "containerName");
    tbsContBeanMap.put("CONTAINER_TYPE", "containerType");
    tbsContBeanMap.put("USABLE_PAGES", "usablePages");
    tbsContBeanMap.put("TOTAL_PAGES", "totalPages");
  }

  /**
   * 监测的结果信息(用于监测结果详细描述).
   */
  private StringBuffer resultDesc = new StringBuffer();

  /**
   * 当前情况(用于发送告警信息).
   */
  private StringBuffer currentVal = new StringBuffer();

  public Db2TableSpaceManager(String ip, DB2MonitorMethodOption option) {
    super(ip, option);
  }

  private List<Object> getTbsListByMap(Map<String, String> beanMap, String sqlStr, Class<?> clazz) throws Exception {
    List<Object> db2tsList = new ArrayList<Object>();
    String sql = sqlStr.replaceAll("#db_sid#", option.getDb());
    if (this.option.getOptType().equalsIgnoreCase(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD)) {
      List result = new DB2AgentExecutor(ip).execute(ip, option, CLIConstant.DB2AGENT,
          new String[] { option.getUsername(), option.getDb(), sql });
      if (result != null && result.size() > 0) {
        for (int i = 0; i < result.size(); i++) {
        	Object temp = result.get(i);
        	if (temp instanceof Map) {
            Map map = (Map) result.get(i);
            Object obj = clazz.newInstance();
            for (Object key : map.keySet()) {
              String colName = key.toString();
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
            BeanUtils.setProperty(obj, beanMap.get(colName), rs.getObject(colName));
          }

          db2tsList.add(obj);
        }

      } catch (Exception t) {
        throw t;
      } finally {
        JDBCUtil.close(connection);
      }
    }
    return db2tsList;
  }

  public List<Object> getTableSpaceList() throws Exception {
    return getTbsListByMap(tbsBeanMap, option.ifNewVersion() ? SQL_TABLESPACE_NEW : SQL_TABLESPACE, Db2TableSpace.class);
  }

  public List<Object> getTableSpaceContainerList() throws Exception {
    return getTbsListByMap(tbsContBeanMap, option.ifNewVersion() ? SQL_TABLESPACECONTAINER_NEW : SQL_TABLESPACECONTAINER, Db2TableSpaceContainer.class);
  } 

  public StringBuffer getCurrentVal() {
    return currentVal;
  }

  public StringBuffer getResultDesc() {
    return resultDesc;
  }
}
