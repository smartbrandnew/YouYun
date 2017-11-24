package com.broada.carrier.monitor.impl.db.db2.tablespacecont;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.db2.Db2TableSpaceManager;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.utils.JDBCUtil;

public class Db2TableSpaceContainerMonitor extends BaseMonitor {
  public final static String[] CONDITION_FIELDS = new String[]{"totalPages","usedRate"};
  
  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

    StringBuffer msg = new StringBuffer(); // 监测结果信息描述
    StringBuffer currVal = new StringBuffer();// 当前情况，用于发送Trap
    
    DB2MonitorMethodOption option = new DB2MonitorMethodOption(context.getMethod());
    Db2TableSpaceManager manager = new Db2TableSpaceManager(context.getNode().getIp(), option);
    Connection testCon = null;
    long replyTime = System.currentTimeMillis();
    try {
      testCon = manager.getConnection();
    } catch (SQLException e1) {
      msg.append(e1.getMessage()+"\n");
      currVal.append(e1.getMessage());
      result.setResultDesc(msg.toString());
      if(e1.getErrorCode()==-99999){//表示用户名密码错误
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
      }else{
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
      }
      return result;      
    } catch (CLIException e) {
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("通过agent方式获取数据出错:" + e.getMessage());
      return result;
    } finally{
      JDBCUtil.close(testCon);
    }
    
    List tsList = Collections.EMPTY_LIST;
    try {
      tsList = manager.getTableSpaceContainerList();
    } catch (Exception e) {
    }
    replyTime = System.currentTimeMillis() - replyTime;
    if (replyTime <= 0)
      replyTime = 1L;
    result.setResponseTime(replyTime);
    
    for (int i = 0, size = tsList.size(); i < size; i++) {
      Db2TableSpaceContainer ts = (Db2TableSpaceContainer) tsList.get(i);      
      MonitorResultRow row = new MonitorResultRow(ts.getContainerName(), ts.getContainerName());
      row.setIndicator("DB2-TBSCONT-1", ts.getTableSpaceName());
      row.setIndicator("DB2-TBSCONT-2", ts.getContainerType());
      row.setIndicator("DB2-TBSCONT-3", ts.getUsablePages());
      row.setIndicator("DB2-TBSCONT-4", ts.getTotalPages());
      row.setIndicator("DB2-TBSCONT-5", ts.getUsableRate());      
      result.addRow(row);
    }
    return result;
  }
}
