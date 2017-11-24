package com.broada.carrier.monitor.impl.db.mssql.database;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.mssql.DataAccessException;
import com.broada.carrier.monitor.impl.db.mssql.MSSQLErrorUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class DatabaseMonitor extends BaseMonitor {
  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);
    
    List databases = null;
    try {
      long replyTime = System.currentTimeMillis();
      databases = DatabaseGetter.getAllDatabases(context.getNode().getIp(), context.getMethod());
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
    } catch (DataAccessException e) {
    	return MSSQLErrorUtil.process("获取数据库信息失败", e);
    }
    
    for(int index = 0; index < databases.size(); index++){
      DatabaseInfo info = (DatabaseInfo) databases.get(index);
      
      MonitorResultRow row = new MonitorResultRow(info.getDatabaseName());
      row.setIndicator("MSSQL-DBSIZE-1", info.getSize());
      row.setIndicator("MSSQL-DBSIZE-2", info.getDataSize());
      row.setIndicator("MSSQL-DBSIZE-3", info.getIndexSize());
      row.setIndicator("MSSQL-DBSIZE-4", info.getUnused());
      row.setIndicator("MSSQL-DBSIZE-5", info.getUnallocatedSize());
      row.setIndicator("MSSQL-DBSIZE-6", info.getReserved());
      result.addRow(row);
    }
    return result;
  }
}
