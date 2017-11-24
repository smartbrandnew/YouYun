package com.broada.carrier.monitor.impl.storage.dell.equallogic.controller;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.controller.bean.MemberController;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpWalk;

public class ControllerInfoMonitor extends BaseMonitor {
	private static final Log logger = LogFactory
			.getLog(ControllerInfoMonitor.class);
	// 性能项的个数
	private static int ITEMIDX_LENGTH = 9;

	/**
	 * 实现doMonitor 监测器采集数据 的实现方法
	 */
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		List<MemberController> controllerInfoList;
		try {
			// 获取所有成员的磁盘信息
		
			ControllerInfoMgr controllermgr = new ControllerInfoMgr(walk);
			logger.info("初始化磁盘信息类完成！");
			try {
				controllerInfoList = controllermgr.generateControllerInfo();
				logger.info("获取磁盘信息完成！");
			} catch (Exception e) {
				logger.info("获取磁盘信息发生异常："+e.getMessage());
				return null;
			}
			 
			 for (MemberController controllerInfo : controllerInfoList) {		
					MonitorResultRow row = new MonitorResultRow();
					row.setInstCode(controllerInfo.getSerialNumber());
					row.setInstName(controllerInfo.getSerialNumber());
					row.setIndicator("DELLEQUALLOGIC-CONTORLLER-INFO-1", controllerInfo.getSerialNumber());
					row.setIndicator("DELLEQUALLOGIC-CONTORLLER-INFO-2", controllerInfo.getContrRevision());
					row.setIndicator("DELLEQUALLOGIC-CONTORLLER-INFO-3", controllerInfo.getContrPrimOrSec());
					row.setIndicator("DELLEQUALLOGIC-CONTORLLER-INFO-4", controllerInfo.getContrType());
					row.setIndicator("DELLEQUALLOGIC-CONTORLLER-INFO-5", controllerInfo.getProcessorTemp());
					row.setIndicator("DELLEQUALLOGIC-CONTORLLER-INFO-6", controllerInfo.getChipsetTemp());
					row.setIndicator("DELLEQUALLOGIC-CONTORLLER-INFO-7", controllerInfo.getBatteryStatus());
					row.setIndicator("DELLEQUALLOGIC-CONTORLLER-INFO-8", controllerInfo.getMemberName());
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
