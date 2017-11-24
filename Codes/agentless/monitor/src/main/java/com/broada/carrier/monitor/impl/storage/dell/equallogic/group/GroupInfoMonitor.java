package com.broada.carrier.monitor.impl.storage.dell.equallogic.group;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpWalk;

public class GroupInfoMonitor extends BaseMonitor {
	private static final Log logger = LogFactory
	.getLog(GroupInfoMonitor.class);

	/*
	 * @see com.broada.srvmonitor.Monitor#doMonitor(com.broada.srvmonitor.model.
	 * MonitorService)
	 */
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		Map groupInfo;
		try {
			GroupInfoMgr groupInfoMgr = new GroupInfoMgr(walk);
			logger.info("初始化 Group组信息类完成！");
			try {
				 groupInfo = groupInfoMgr.generateHostInfo();
				logger.info("获取 Group组信息完成！");
			} catch (Exception e) {
				logger.info("获取 Group组信息发生异常："+e.getMessage());
				return null;
			}
					MonitorResultRow row = new MonitorResultRow();
					row.setInstCode(groupInfo.get("groupName").toString());
					row.setInstName(groupInfo.get("groupName").toString());
					row.setIndicator("DELLEQUALLOGIC-GROUP-INFO-1", groupInfo.get("groupName").toString());
					row.setIndicator("DELLEQUALLOGIC-GROUP-INFO-2", groupInfo.get("memberCount").toString());
					row.setIndicator("DELLEQUALLOGIC-GROUP-INFO-3", groupInfo.get("membersInUse").toString());
					result.addRow(row);
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			walk.close();
		}
		return result;
}
}
