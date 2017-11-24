package com.broada.carrier.monitor.impl.storage.dell.equallogic.disk;


import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.storage.dell.equallogic.disk.bean.MemberDisk;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpWalk;

public class DiskInfoMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(DiskInfoMonitor.class);
	// 性能项的个数
	private static int ITEMIDX_LENGTH = 8;

	/**
	 * 实现doMonitor 监测器采集数据 的实现方法
	 */
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		List<MemberDisk> diskInfoList;
		try {
			DiskInfoMgr diskmgr = new DiskInfoMgr(walk);
			logger.info("初始化磁盘工具类完成！");
			try {
				diskInfoList = diskmgr.generateDiskInfo();
				logger.info("获取磁盘信息完成！");
			} catch (Exception e) {
				logger.info("获取磁盘信息发生异常："+e.getMessage());
				return null;
			}
			 
			 for (MemberDisk diskInfo : diskInfoList) {		
					MonitorResultRow row = new MonitorResultRow();
					row.setInstCode(diskInfo.getDiskId());
					row.setInstName(diskInfo.getDiskId());
					row.setIndicator("DELLEQUALLOGIC-DISKDRIVER-INFO-1", diskInfo.getDiskId());
					row.setIndicator("DELLEQUALLOGIC-DISKDRIVER-INFO-5", diskInfo.getDiskStatus());
					row.setIndicator("DELLEQUALLOGIC-DISKDRIVER-INFO-8", diskInfo.getMemberName());
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
