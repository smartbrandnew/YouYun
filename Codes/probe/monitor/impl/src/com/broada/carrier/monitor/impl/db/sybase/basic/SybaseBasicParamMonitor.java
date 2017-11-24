package com.broada.carrier.monitor.impl.db.sybase.basic;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.sybase.SybaseMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;

import java.io.Serializable;

public class SybaseBasicParamMonitor extends BaseMonitor {
	private final String ITEM_CODE = "SYBASE-BASIC-";

	@Override public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorState.UNMONITOR);

		SybaseMonitorMethodOption option = new SybaseMonitorMethodOption(context.getMethod());
		String ip = context.getNode().getIp();
		int port = option.getPort();
		String dbname = "";
		SybaseInfo sybaseInfo = null;
		long replyTime = System.currentTimeMillis();
		try {
			sybaseInfo = SybaseUtils.getSybaseInfo(ip, port, dbname, option.getUsername(), option.getPassword());
		} catch (Exception e) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("无法取得Sybase基本信息");
			return result;
		}
		replyTime = System.currentTimeMillis() - replyTime;
		if (replyTime <= 0)
			replyTime = 1L;
		result.setResponseTime(replyTime);
		MonitorResultRow row = new MonitorResultRow();
		if (sybaseInfo != null) {
			row.setIndicator(ITEM_CODE + 1, sybaseInfo.getDbmsName());
			row.setIndicator(ITEM_CODE + 2, sybaseInfo.getDbmsVer());
			row.setIndicator(ITEM_CODE + 3, sybaseInfo.getDatabaseProductName());
			row.setIndicator(ITEM_CODE + 4, sybaseInfo.getDatabaseProductVersion());
			row.setIndicator(ITEM_CODE + 5, "" + sybaseInfo.getDatabaseMajorVersion());
			row.setIndicator(ITEM_CODE + 6, "" + sybaseInfo.getDatabaseMinorVersion());
			row.setIndicator(ITEM_CODE + 7, sybaseInfo.getVersion());
		}
		result.addRow(row);
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		return result;
	}

}
