package com.broada.carrier.monitor.impl.db.mssql.basic;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.mssql.DataAccessException;
import com.broada.carrier.monitor.impl.db.mssql.MSSQLErrorUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class BasicInfoMonitor extends BaseMonitor {
  @Override
	public Serializable collect(CollectContext context) {
    MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);
        
    List<BasicInfo> basicInfos = null;
    try {
      long replyTime = System.currentTimeMillis();
      basicInfos = BasicInfoGetter.getBasicInfos(context.getNode().getIp(), context.getMethod());
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);           
    } catch (DataAccessException e) {
    	return MSSQLErrorUtil.process("获取数据库基本信息失败", e);    	
    }
    
    for(int index = 0; index < basicInfos.size(); index++){
      BasicInfo info = (BasicInfo) basicInfos.get(index);
      if (info.getValue() == null)
      	continue;
      String key = "MSSQL-BASIC-" + info.getSort();
      result.addPerfResult(new PerfResult(key, info.getValue()));      
    }
    return result;
  }
}
