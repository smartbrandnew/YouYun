package com.broada.carrier.monitor.impl.db.xugu;

import com.broada.carrier.monitor.impl.db.xugu.cluster.ClusterNodeStatMonitor;
import com.broada.carrier.monitor.impl.db.xugu.session.SessionInfoMonitor;
import com.broada.carrier.monitor.impl.db.xugu.sga.MemoryMonitor;
import com.broada.carrier.monitor.impl.db.xugu.transaction.NodeTransactionInfoMonitor;
import com.broada.carrier.monitor.method.xugu.XuguMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class XuguMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Xugu" };
		String[] methodTypeIds = new String[] { XuguMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("XUGU", "XUGU-CLUSTER", "虚谷数据裤集群节点状态信息", "集群节点状态信息",
						null, ClusterNodeStatMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("XUGU", "XUGU-SESSION", "虚谷数据库连接信息", "虚谷数据库连接信息",
						null, SessionInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("XUGU", "XUGU-TRANSACTION", "虚谷数据库节点事务执行信息监控", "虚谷数据库节点事务执行信息监控",
						null, NodeTransactionInfoMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
//				new MonitorType("XUGU", "XUGU-INCREMENT", "虚谷数据库数据增量监控", "虚谷数据库数据增量监控",
//						null, DBIncrementMonitor.class.getName(), index++,
//						targetTypeIds, methodTypeIds),
				new MonitorType("XUGU", "XUGU-SGA", "虚谷SGA监控", "虚谷SGA监控",
						null, MemoryMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds)};
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("XUGU-CLUSTER","XUGU-CLUSTER-1", "节点状态", "", "节点状态", MonitorItemType.TEXT),
				
				// TODO 连接时长暂时用文本表示(单位)
				new MonitorItem("XUGU-SESSION","XUGU-SESSION-1", "连接时长", "", "连接时长", MonitorItemType.TEXT),
				
				new MonitorItem("XUGU-TRANSACTION", "XUGU-TRANSACTION-1", "事务号差 ", "个", "事务号差 ", MonitorItemType.NUMBER),
				
				new MonitorItem("XUGU-SGA", "XUGU-SGA-1", "物理内存总大小", "MB", "物理内存总大小", MonitorItemType.NUMBER),
				new MonitorItem("XUGU-SGA", "XUGU-SGA-2", "已使用物理内存大小", "MB", "已使用物理内存大小", MonitorItemType.NUMBER),
				new MonitorItem("XUGU-SGA", "XUGU-SGA-3", "可使用物理内存大小", "MB", "可使用物理内存大小", MonitorItemType.NUMBER),
				new MonitorItem("XUGU-SGA", "XUGU-SGA-4", "物理内存使用率", "%", "物理内存使用率", MonitorItemType.NUMBER),
				new MonitorItem("XUGU-SGA", "XUGU-SGA-5", "交换分区内存总大小", "MB", "交换分区内存总大小", MonitorItemType.NUMBER),
				new MonitorItem("XUGU-SGA", "XUGU-SGA-6", "已使用交换分区内存大小", "MB", "已使用交换分区内存大小", MonitorItemType.NUMBER),
				new MonitorItem("XUGU-SGA", "XUGU-SGA-7", "可使用交换分区内存大小", "MB", "可使用交换分区内存大小", MonitorItemType.NUMBER),
				new MonitorItem("XUGU-SGA", "XUGU-SGA-8", "交换分区内存使用率", "%", "交换分区内存使用率", MonitorItemType.NUMBER)};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(XuguMonitorMethodOption.TYPE_ID, "Xugu JDBC 监测协议",
				XuguMonitorPackage.class)};
	}

}
