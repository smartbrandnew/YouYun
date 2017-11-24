package com.broada.carrier.monitor.impl.db.db2;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.broada.carrier.monitor.impl.db.db2.bp.DbBufferPool;
import com.broada.carrier.monitor.impl.db.db2.sort.DBSort;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.utils.ListUtil;

/**
 * 提供DB2数据库扩展管理
 * @author 杨帆
 * 
 */
public class DB2ExtendManagerImpl extends Db2Manager implements DB2ExtendManager{

  public static final Log logger = LogFactory.getLog(DB2ExtendManagerImpl.class);

  public DB2ExtendManagerImpl(String ip, DB2MonitorMethodOption option) {
    super(ip, option);
  }

  private static final String SQL_CONFIG_PATH = "DB2_SQL_config.xml";

  private static final String[] SQL_LAYERS = { "category", "sql" };

  private static final Map<String,Map<String,String>> sqlsMap = new HashMap<String,Map<String,String>>();

  private static final Map<String,String> bufferPoolMap = new HashMap<String,String>();

  private static final Map<String,String> sortMap = new HashMap<String,String>();

  static {
    initCache();
  }

  private static void initCache() {
    initSQLCache();
    initMap();
  }

  private static void initSQLCache() {
    if (sqlsMap.isEmpty()) {
      SAXBuilder builder = new SAXBuilder();
      try {
        Document document = builder.build(DB2ExtendManagerImpl.class.getResourceAsStream(SQL_CONFIG_PATH));
        Element element = document.getRootElement();
        List roots = element.getChildren();
        if (ListUtil.isNullOrEmpty(roots)) {
          logger.debug("DB2的sql配置库没有父节点", new Exception("DB2的sql配置库没有父节点"));
          return;
        } else {
          orgRoots(roots);
        }

      } catch (JDOMException e) {
        logger.debug("DB2的sql配置库读取失败", e);
      } catch (IOException e) {
        logger.debug("DB2的sql配置库读取失败", e);
      }
    }
  }

  private static void orgRoots(List roots) {
    Iterator it = roots.iterator();
    while (it.hasNext()) {
      Element root = (Element) it.next();
      if (SQL_LAYERS[0].equals(root.getName())) {
        Map<String,String> sqls = new HashMap<String,String>();
        List chlids = root.getChildren();
        if (ListUtil.isNullOrEmpty(chlids)) {
          logger.debug("DB2的sql配置库没有子节点", new Exception("DB2的sql配置库没有子节点"));
          return;
        } else {
          orgSqls(chlids, sqls);
        }
        sqlsMap.put(root.getAttributeValue("name"), sqls);
      }
    }
  }

  private static void orgSqls(List chlids, Map<String,String> sqls) {
    Iterator it = chlids.iterator();
    while (it.hasNext()) {
      Element child = (Element) it.next();
      if (SQL_LAYERS[1].equals(child.getName())) {
        sqls.put(child.getAttributeValue("name"), child.getText());
      }
    }
  }

  private static void initMap() {
    initBufferMap();
    initSortMap();
  }

  private static void initBufferMap() {
    if (bufferPoolMap.isEmpty()) {
      bufferPoolMap.put("POOL_DATA_L_READS", "dataLogicReads");
      bufferPoolMap.put("POOL_INDEX_L_READS", "indexLogicReads");
      bufferPoolMap.put("POOL_DATA_P_READS", "dataPhysicsReads");
      bufferPoolMap.put("POOL_INDEX_P_READS", "indexPhysicsReads");
      bufferPoolMap.put("POOL_DATA_RATIO", "dataRatio");
      bufferPoolMap.put("POOL_INDEX_RATIO", "indexRatio");
      bufferPoolMap.put("DIRECT_READS", "directReads");
      bufferPoolMap.put("DIRECT_WRITES", "directWrites");
      bufferPoolMap.put("CAT_CACHE_RATIO", "catCacheRatio");
      bufferPoolMap.put("PKG_CACHE_RATIO", "pkgCacheRatio");
    }
  }

  private static void initSortMap() {
    if (sortMap.isEmpty()) {
      sortMap.put("TOTAL_SORTS", "totalSorts");
      sortMap.put("SORT_OVER_RATIO", "sortOverRatio");
    }
  }

  public DbBufferPool getBufferPoolData() throws SQLException, InstantiationException, IllegalAccessException,
      InvocationTargetException,CLIException {
    DbBufferPool bufferPool = new DbBufferPool();
    List data_index_ratio = getDatas(bufferPoolMap, option.ifNewVersion() ? sqlsMap.get("buffer-pool").get("data-index-ratio-new") : sqlsMap.get("buffer-pool").get("data-index-ratio")
        , DbBufferPool.class);
    List cat_pkg_ratio = getDatas(bufferPoolMap, option.ifNewVersion() ? sqlsMap.get("buffer-pool").get("cat-pkg-ratio-new") : sqlsMap.get("buffer-pool").get("cat-pkg-ratio"),
        DbBufferPool.class);
    Iterator it = data_index_ratio.iterator();
    if (it.hasNext()) {
      DbBufferPool data_index = (DbBufferPool) it.next();
      bufferPool.setDataLogicReads(data_index.getDataLogicReads());
      bufferPool.setDataPhysicsReads(data_index.getDataPhysicsReads());

      bufferPool.setDataRatio(BigDecimal.valueOf(data_index.getDataRatio() * 100).setScale(2, BigDecimal.ROUND_HALF_UP)
          .doubleValue());
      bufferPool.setIndexLogicReads(data_index.getIndexLogicReads());
      bufferPool.setIndexPhysicsReads(data_index.getIndexPhysicsReads());

      bufferPool.setIndexRatio(BigDecimal.valueOf(data_index.getIndexRatio() * 100).setScale(2,
          BigDecimal.ROUND_HALF_UP).doubleValue());
      bufferPool.setDirectReads(data_index.getDirectReads());
      bufferPool.setDirectWrites(data_index.getDirectWrites());
    }
    Iterator it2 = cat_pkg_ratio.iterator();
    if (it2.hasNext()) {
      DbBufferPool cat_pkg = (DbBufferPool) it2.next();
      bufferPool.setCatCacheRatio(BigDecimal.valueOf(cat_pkg.getCatCacheRatio() * 100).setScale(2,
          BigDecimal.ROUND_HALF_UP).doubleValue());
      bufferPool.setPkgCacheRatio(BigDecimal.valueOf(cat_pkg.getPkgCacheRatio() * 100).setScale(2,
          BigDecimal.ROUND_HALF_UP).doubleValue());
    }
    return bufferPool;
  }

  public DBSort getDBSortData() throws SQLException, InstantiationException, IllegalAccessException,
      InvocationTargetException,CLIException {
    DBSort sort = null;
    List datas = getDatas(sortMap, option.ifNewVersion() ? sqlsMap.get("sort").get("base-new"): sqlsMap.get("sort").get("base"), DBSort.class);
    Iterator it = datas.iterator();
    if (it.hasNext()) {
      sort = (DBSort) it.next();
      sort.setSortOverRatio(BigDecimal.valueOf(sort.getSortOverRatio() * 100).setScale(2, BigDecimal.ROUND_HALF_UP)
          .doubleValue());
    }
    return sort;
  }

  /**
   * 通过sqlStr脚本获取数据库里的集合
   * @param beanMap
   * @param sqlStr
   * @param clazz
   * @return
   * @throws SQLException
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  private List getDatas(Map<String,String> beanMap, String sqlStr, Class clazz) throws SQLException, InstantiationException,
      IllegalAccessException, InvocationTargetException, CLIException {
    List results = new ArrayList();
    String sql = sqlStr.replaceAll("\\$DATABASE", option.getDb());
    if (this.option.getOptType().equalsIgnoreCase(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD)) {
      try {
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
	            results.add(obj);
          	} else if (temp instanceof String) {
          		DB2CmdTable table = new DB2CmdTable((String)temp);
          		
          		for (int j = 0; j < table.getRowsSize(); j++) {
								Object obj = clazz.newInstance();
								for (int c = 0; c < table.getColumnsSize(); c++) {									
									String colName = table.getColumn(c);
									BeanUtils.setProperty(obj, (String) beanMap.get(colName), table.getData(j, c));
								}
          			results.add(obj);
          		}
          	} else 
          		throw new IllegalArgumentException(String.format("无法识别的数据类型：%s", temp));            
          }
        }
      } catch (SQLException e) {
        judgeSQLExcetpion(e);
        throw e;
      }
    } else {
      Connection connection = getConnectionIgnoreRole();
      PreparedStatement ps = null;
      ResultSet rs = null;
      ResultSetMetaData metaData = null;
      try {

        ps = connection.prepareStatement(sql);
        rs = ps.executeQuery();
        metaData = ps.getMetaData();
        while (rs.next()) {
          Object ob = clazz.newInstance();
          for (int i = 1, count = metaData.getColumnCount(); i <= count; i++) {
            String columnName = metaData.getColumnName(i);
            BeanUtils.setProperty(ob, beanMap.get(columnName), rs.getObject(columnName));
          }
          results.add(ob);
        }
      } catch (SQLException e) {
        judgeSQLExcetpion(e);
        if (connection == null) {// 判断连接是否有效
          SQLException se = new SQLException("连接失败,请检查配置是否正确.");
          throw se;
        }
        throw e;
      } finally {
        if (rs != null) {
          rs.close();
        }
        if (ps != null) {
          ps.close();
        }
        if (connection != null) {
          connection.close();
        }
      }
    }
    return results;
  }
  
  private void judgeSQLExcetpion(SQLException e) throws SQLException {
    int errCode = e.getErrorCode();
    
    //指定用户无SYSADM、SYSCTRL 或 SYSMAINT 权限.
    if(e.getSQLState().equals("SQL0440N")){
      throw e;
    }
    // 根据错误号来判断错误类型
    // -4499数据库url错误
    // -99999 用户名或密码错误
    if (errCode == -4499) {
      SQLException se = new SQLException("连接失败,数据库url错误,可能指定的端口不是DB2监听端口或数据库实例名错误.");
      se.initCause(e);
      throw se;
    } else if (errCode == -99999) {
      DB2LoginException se = new DB2LoginException("连接失败,用户名或密码错误.", "", errCode);
      se.initCause(e);
      throw se;
    } else if (errCode != 0) {
      SQLException se = new SQLException("数据库没有装载或打开,错误:" + e.getMessage());
      se.initCause(e);
      throw se;
    } else if (e != null) {// 如果有未知异常则继续抛出
      SQLException se = new SQLException("连接失败,请查看实例名或相关配置是否正确.");
      se.initCause(e);
      throw se;
    }
  }
}
