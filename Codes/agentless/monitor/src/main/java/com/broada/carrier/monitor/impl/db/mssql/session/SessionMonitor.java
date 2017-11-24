package com.broada.carrier.monitor.impl.db.mssql.session;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.mssql.DataAccessException;
import com.broada.carrier.monitor.impl.db.mssql.MSSQLErrorUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.utils.XMLUtil;

public class SessionMonitor extends BaseMonitor {
  @Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setState(MonitorConstant.MONITORSTATE_NICER);

		List sessions = null;
		try {
      long replyTime = System.currentTimeMillis();
			sessions = SessionGetter.getSessions(context.getNode().getIp(), context.getMethod());
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
		} catch (DataAccessException e) {
			return MSSQLErrorUtil.process("无法获取当前连接会话列表", e);			
		}

		for (int index = 0; index < sessions.size(); index++) {
			SessionInfo info = (SessionInfo) sessions.get(index);
			// 由于doCollect方法中调用了CollectResultUtil.parseTableResultToCollectResult方法，获取的User值被XMLUtil.decode函数过滤了空格。
			// 这样做的意图不明，这里暂时也做同样的处理
			String instanceKey = XMLUtil.decode(info.getUser()) + SessionInfo.INFO_SEPARATOR + info.getId();
			MonitorResultRow row = new MonitorResultRow(instanceKey, info.getId());
			row.setIndicator("MSSQL-SESSION-1", info.getStatus());
			row.setIndicator("MSSQL-SESSION-2", info.getUser());
			row.setIndicator("MSSQL-SESSION-3", info.getHost());
			row.setIndicator("MSSQL-SESSION-4", info.getProgram());
			row.setIndicator("MSSQL-SESSION-5", info.getMemory());
			row.setIndicator("MSSQL-SESSION-6", info.getCpuTime());
			row.setIndicator("MSSQL-SESSION-7", info.getDatabase());
			row.setIndicator("MSSQL-SESSION-8", info.getCommand());
			row.setIndicator("MSSQL-SESSION-9", info.getLastBatchTime());
			row.setIndicator("MSSQL-SESSION-10", info.getLoginTime());
			result.addRow(row);
		}
		return result;
	}
}
