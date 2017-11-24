package com.broada.carrier.monitor.impl.db.dm.basic;

import java.io.Serializable;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.dm.DmManager;
import com.broada.carrier.monitor.method.dm.DmMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class DmBasicMonitor extends BaseMonitor {
	private final String ITEM_CODE = "DM-BASIC-";

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorState.UNMONITOR);

		DmMonitorMethodOption option = new DmMonitorMethodOption(context.getMethod());
		String ip = context.getNode().getIp();
		int port = option.getPort();
		String sid = option.getSid();
		DmBaseInfo dmBaseinfo = null;
		long replyTime = System.currentTimeMillis();
		try {
			DmManager dm = new DmManager(sid, ip, port, option.getUsername(), option.getPassword());
			dm.initConnection();
			dmBaseinfo = dm.getDmBaseInfo(sid);
		} catch (Exception e) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("无法取得DM基本信息");
			return result;
		}
		replyTime = System.currentTimeMillis() - replyTime;
		if (replyTime <= 0)
			replyTime = 1L;
		result.setResponseTime(replyTime);
		MonitorResultRow row = new MonitorResultRow();
		if (dmBaseinfo != null) {
			row.setIndicator(ITEM_CODE + 1, dmBaseinfo.getDbName());
			row.setIndicator(ITEM_CODE + 2, dmBaseinfo.getProductName());
			row.setIndicator(ITEM_CODE + 3, dmBaseinfo.getMode());
			row.setIndicator(ITEM_CODE + 4, dmBaseinfo.getVersion());
			row.setIndicator(ITEM_CODE + 5, dmBaseinfo.getHostName());
			row.setIndicator(ITEM_CODE + 6, dmBaseinfo.getStatus());
		}
		result.addRow(row);
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		return result;
	}

}
