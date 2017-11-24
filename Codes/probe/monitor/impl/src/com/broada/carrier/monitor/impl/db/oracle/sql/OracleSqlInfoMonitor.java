package com.broada.carrier.monitor.impl.db.oracle.sql;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * Oracle SQL监测实现
 * 
 * @author Wangx (wangx@broada.com)
 * Create By 2008-6-11 下午04:02:34
 */
public class OracleSqlInfoMonitor extends BaseMonitor {
  public static final String[] COLUMNS = { "execTime", "runtimeMem"};

  private static final int ITEMIDX_LENGTH = 4;
  
  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);    
    OracleMethod option = new OracleMethod(context.getMethod());
    OracleManager om = new OracleManager(context.getNode().getIp(), option);
    long respTime = System.currentTimeMillis();
    try {
      om.initConnection();
    } catch (Exception lde) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc(lde.getMessage());
      respTime = System.currentTimeMillis() - respTime;
      if (respTime <= 0) {
        respTime = 1;
      }
      result.setResponseTime(respTime);
      if(om!=null){
        om.close();
      }
      return result;
    } 

    List<OracleSqlInfo> dataList=null;
    try {
      dataList = om.getAllSqlInfo();
    } catch (SQLException e) {
      result.setResultDesc("无法获取当前连接SQL信息列表.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } finally {
      if(om!=null){
        om.close();
      }
    }
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0) {
      respTime = 1;
    }
    result.setResponseTime(respTime);

    //以userName+'执行时间'或'使用内存' 作为key
    Map totalSqlCnt = new HashMap();
    for (int i = 0, perfIndex = 0; i < dataList.size(); perfIndex = perfIndex + ITEMIDX_LENGTH, i++) {
      OracleSqlInfo info = (OracleSqlInfo) dataList.get(i);
      
      if(totalSqlCnt.containsKey(info.getUserName())){
        Integer sqlCnt = (Integer)totalSqlCnt.get(info.getUserName());
        totalSqlCnt.put(info.getUserName(), new Integer(sqlCnt.intValue()+1));
      }else{
        totalSqlCnt.put(info.getUserName(), new Integer(1));
      }
      
      //创建实例
      MonitorResultRow row = new MonitorResultRow(info.getSid());
      for (int j = 0; j < ITEMIDX_LENGTH; j++) {
      	Object value = getInfoValue(info, j);
      	if (value != null && value instanceof Double && ((Double)value).doubleValue() < 0)
      		value = null;
      	if (value == null)
      		continue;
      	row.setIndicator("ORACLE-SQLINFO-" + (j + 1), value);
      }
    }

    return result;
  }

  /**
   * 根据索引取得属性值
   * @param info
   * @param colIndex
   * @return
   */
  private Object getInfoValue(OracleSqlInfo info, int colIndex) {
    switch (colIndex) {
    case 0:
      return info.getUserName();
    case 1:
      return info.getSqlText();
    case 2:
      return info.getExecTime();
    case 3:
      return info.getRuntimeMem();
    default:
      return null;
    }
  }
}