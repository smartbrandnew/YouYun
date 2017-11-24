package com.broada.carrier.monitor.impl.storage.dell.equallogic.ipconf;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.ipconf.bean.MemberIPConf;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpWalk;

public class IPConfInfoMonitor extends BaseMonitor {
	private static final Log logger = LogFactory
			.getLog(IPConfInfoMonitor.class);
	// 性能项的个数
	private static int ITEMIDX_LENGTH = 6;

	/**
	 * 实现doMonitor 监测器采集数据 的实现方法
	 */
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		List<MemberIPConf> ipConfInfoList;
		try {
			IPConfInfoMgr ipConfmgr = new IPConfInfoMgr(walk);
			logger.info("初始化ip信息类完成！");
			try {
				ipConfInfoList = ipConfmgr.generateIPConfInfo();
				logger.info("获取ip信息完成！");
			} catch (Exception e) {
				logger.info("获取ip信息发生异常："+e.getMessage());
				return null;
			}
			 
			 for (MemberIPConf ipConfInfo : ipConfInfoList) {		
					MonitorResultRow row = new MonitorResultRow();
					row.setInstCode(ipConfInfo.getIpAddress());
					row.setInstName(ipConfInfo.getIpAddress());
					row.setIndicator("DELLEQUALLOGIC-IPCONF-INFO-1", ipConfInfo.getInterfaceIndex());
					row.setIndicator("DELLEQUALLOGIC-IPCONF-INFO-2", ipConfInfo.getInterfaceName());
					row.setIndicator("DELLEQUALLOGIC-IPCONF-INFO-3", ipConfInfo.getIpAddress());
					row.setIndicator("DELLEQUALLOGIC-IPCONF-INFO-4", ipConfInfo.getSubNetMask());
					row.setIndicator("DELLEQUALLOGIC-IPCONF-INFO-5", ipConfInfo.getStatus());
					row.setIndicator("DELLEQUALLOGIC-IPCONF-INFO-6", ipConfInfo.getMemberName());
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
