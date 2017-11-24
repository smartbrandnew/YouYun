package com.broada.carrier.monitor.impl.storage.dell.equallogic.fan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.fan.bean.MemberFan;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.ipconf.IPConfInfoMgr;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.ipconf.bean.MemberIPConf;
import com.broada.carrier.monitor.method.common.MonitorCondition;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpUtil;
import com.broada.snmp.SnmpWalk;

public class FanInfoMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(FanInfoMonitor.class);
	// 性能项的个数
	private static int ITEMIDX_LENGTH = 5;

	/**
	 * 实现doMonitor 监测器采集数据 的实现方法
	 */
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		List<MemberFan> fanInfoList;
		try {
			FanInfoMgr fanmgr = new FanInfoMgr(walk);
			logger.info("初始化风扇信息类完成！");
			try {
				fanInfoList = fanmgr.generateFanInfo();
				logger.info("获取风扇信息完成！");
			} catch (Exception e) {
				logger.info("获取风扇信息发生异常："+e.getMessage());
				return null;
			}
			 
			 for (MemberFan fanInfo : fanInfoList) {		
					MonitorResultRow row = new MonitorResultRow();
					row.setInstCode(fanInfo.getFanName());
					row.setInstName(fanInfo.getFanName());
					row.setIndicator("DELLEQUALLOGIC-FAN-INFO-1", fanInfo.getFanName());
					row.setIndicator("DELLEQUALLOGIC-FAN-INFO-2", fanInfo.getFanStatus());
					row.setIndicator("DELLEQUALLOGIC-FAN-INFO-3", fanInfo.getMemberName());
					result.addRow(row);
				}
		
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			walk.close();
		}
		return result;
}
}
