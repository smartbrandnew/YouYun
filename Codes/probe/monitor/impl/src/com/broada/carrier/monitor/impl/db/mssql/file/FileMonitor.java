package com.broada.carrier.monitor.impl.db.mssql.file;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.mssql.DataAccessException;
import com.broada.carrier.monitor.impl.db.mssql.MSSQLErrorUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class FileMonitor extends BaseMonitor {
  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);

    List files = null;
    try {
      long replyTime = System.currentTimeMillis();
      files = FileGetter.getFiles(context.getNode().getIp(), context.getMethod());
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
    } catch (DataAccessException e) {
    	return MSSQLErrorUtil.process("获取数据库文件信息失败", e);
    }

    for (int index = 0; index < files.size(); index++) {
      FileInfo info = (FileInfo) files.get(index);
      
      String key = info.getDatabaseName() + FileInfo.FILE_SEPARATOR + info.getName();
      MonitorResultRow row = new MonitorResultRow(key);
      
      row.setIndicator("MSSQL-FILE-1", info.getDatabaseName());
      row.setIndicator("MSSQL-FILE-2", info.getGroupName());
      row.setIndicator("MSSQL-FILE-3", info.getSize());
      row.setIndicator("MSSQL-FILE-4", info.getMaxCapability());
      row.setIndicator("MSSQL-FILE-5", info.getGrowth());
      row.setIndicator("MSSQL-FILE-6", info.getFileName());
      
      result.addRow(row);
    }
    return result;
  }

}
