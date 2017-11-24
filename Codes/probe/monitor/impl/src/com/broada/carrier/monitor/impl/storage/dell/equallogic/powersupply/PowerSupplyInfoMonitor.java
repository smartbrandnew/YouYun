package com.broada.carrier.monitor.impl.storage.dell.equallogic.powersupply;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.powersupply.bean.MemberPowerSupply;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpWalk;

public class PowerSupplyInfoMonitor extends BaseMonitor {
	private static final Log logger = LogFactory
			.getLog(PowerSupplyInfoMonitor.class);
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
		List<MemberPowerSupply> powerSupplyInfos;
		try {
			PowerSupplyInfoMgr powerSupplymgr = new PowerSupplyInfoMgr(walk);
			logger.info("初始化电源工具类完成！");
			try {
				powerSupplyInfos = powerSupplymgr.generatePowerSupplyInfo();
				logger.info("获取电源信息完成！");
			} catch (Exception e) {
				logger.info("获取电源信息发生异常："+e.getMessage());
				return null;
			}
			 
			 for (MemberPowerSupply powerSupplyInfo : powerSupplyInfos) {		
					MonitorResultRow row = new MonitorResultRow();
					row.setInstCode(powerSupplyInfo.getMemberName());
					row.setInstName(powerSupplyInfo.getMemberName());
					row.setIndicator("DELLEQUALLOGIC-POWERSUPPLY-INFO-1", powerSupplyInfo.getPowerSupplyName());
					row.setIndicator("DELLEQUALLOGIC-POWERSUPPLY-INFO-2", powerSupplyInfo.getPowerSupplyStatus());
					row.setIndicator("DELLEQUALLOGIC-POWERSUPPLY-INFO-3", powerSupplyInfo.getMemberName());
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
