package com.broada.carrier.monitor.impl.db.informix;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.db.informix.basic.InformixBasicMonitor;
import com.broada.carrier.monitor.impl.db.informix.dbspace.DbSpaceMonitor;
import com.broada.carrier.monitor.impl.db.informix.sysprofile.InformixSysprofileMonitor;
import com.broada.carrier.monitor.method.informix.InformixMonitorMethodOption;
import com.broada.carrier.monitor.method.informix.InformixParamPanel;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class InformixMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Informix" };
		String[] methodTypeIds = new String[] { InformixMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("INFORMIX","INFORMIX-BASIC", "Informix基础监测 [可用性]", "监测Informix数据库的基本信息和运行状态",
						SingleInstanceConfiger.class.getName(), InformixBasicMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("INFORMIX","INFORMIX-DBSPACE", "Informix DbSpace监测", "监测Informix数据空间的使用率情况,可以根据设定的阈值进行告警.",
						MultiInstanceConfiger.class.getName(), DbSpaceMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("INFORMIX","INFORMIX-SHM-JDBC", "Informix共享内存监测", "监测Informix数据库共享内存的使用情况，可以根据设定的阈值进行告警.",
						SingleInstanceConfiger.class.getName(), InformixSysprofileMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("INFORMIX","INFORMIX-CHKPT-JDBC", "Informix检查点监测", "监测Informix数据库检查点情况，可以根据设定的阈值进行告警.",
						SingleInstanceConfiger.class.getName(), InformixSysprofileMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("INFORMIX","INFORMIX-TRANS-JDBC", "Informix事务监测", "监测Informix数据库事务的使用情况，可以根据设定的阈值进行告警.",
						SingleInstanceConfiger.class.getName(), InformixSysprofileMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("INFORMIX","INFORMIX-LOCK-JDBC", "Informix锁监测", "监测Informix数据库锁的使用情况，可以根据设定的阈值进行告警.",
						SingleInstanceConfiger.class.getName(), InformixSysprofileMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("INFORMIX","INFORMIX-DISK-JDBC", "Informix磁盘读写监测", "监测Informix数据库磁盘读写情况，可以根据设定的阈值进行告警.",
						SingleInstanceConfiger.class.getName(), InformixSysprofileMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("INFORMIX","INFORMIX-LOG-JDBC", "Informix日志读写监测", "监测Informix数据库日志读写情况，可以根据设定的阈值进行告警.",
						SingleInstanceConfiger.class.getName(), InformixSysprofileMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("INFORMIX","INFORMIX-SORT-JDBC", "Informix排序监测", "监测Informix数据库排序情况，可以根据设定的阈值进行告警.",
						SingleInstanceConfiger.class.getName(), InformixSysprofileMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("INFORMIX-BASIC","INFORMIX-BASIC-1", "数据库连接数", "个", "Informix数据库连接数", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-BASIC","INFORMIX-BASIC-2", "读缓存命中率", "%", "Informix读缓存命中率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-BASIC","INFORMIX-BASIC-3", "写缓存命中率", "%", "Informix写缓存命中率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-BASIC","INFORMIX-BASIC-4", "死锁数", "个", "Informix死锁数", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-BASIC","INFORMIX-BASIC-5", "回滚数", "个", "Informix回滚数", MonitorItemType.NUMBER),

				new MonitorItem("INFORMIX-CHKPT-JDBC","INFORMIX-CHKPT-JDBC-1", "检查点速率", "次/秒", "检查点速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-CHKPT-JDBC","INFORMIX-CHKPT-JDBC-2", "检查点等待速率", "次/秒", "检查点等待速率", MonitorItemType.NUMBER),

				new MonitorItem("INFORMIX-DBSPACE","INFORMIX-DBSPACE-1", "数据空间使用率", "%", "Informix数据空间的已用空间和总空间的百分比", MonitorItemType.NUMBER),

				new MonitorItem("INFORMIX-DISK-JDBC","INFORMIX-DISK-JDBC-1", "物理磁盘读速率", "次/分", "物理磁盘读速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-DISK-JDBC","INFORMIX-DISK-JDBC-2", "物理磁盘写速率", "次/分", "物理磁盘写速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-DISK-JDBC","INFORMIX-DISK-JDBC-3", "页读速率", "页/分", "页读速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-DISK-JDBC","INFORMIX-DISK-JDBC-4", "页写速率", "页/分", "页写速率", MonitorItemType.NUMBER),

				new MonitorItem("INFORMIX-LOCK-JDBC","INFORMIX-LOCK-JDBC-1", "锁请求速率", "个/秒", "锁请求速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-LOCK-JDBC","INFORMIX-LOCK-JDBC-2", "死锁率", "个/秒", "死锁率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-LOCK-JDBC","INFORMIX-LOCK-JDBC-3", "锁等待率", "个/秒", "锁等待率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-LOCK-JDBC","INFORMIX-LOCK-JDBC-4", "锁超时率", "个/秒", "锁超时率", MonitorItemType.NUMBER),

				new MonitorItem("INFORMIX-LOG-JDBC","INFORMIX-LOG-JDBC-1", "逻辑日志记录写速率", "次/分", "逻辑日志记录写速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-LOG-JDBC","INFORMIX-LOG-JDBC-2", "逻辑日志写速率", "次/分", "逻辑日志写速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-LOG-JDBC","INFORMIX-LOG-JDBC-3", "逻辑日志页面写速率", "次/分", "逻辑日志页面写速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-LOG-JDBC","INFORMIX-LOG-JDBC-4", "物理日志写速率", "次/分", "物理日志写速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-LOG-JDBC","INFORMIX-LOG-JDBC-5", "物理日志页面写速率", "次/分", "物理日志页面写速率", MonitorItemType.NUMBER),

				new MonitorItem("INFORMIX-SHM-JDBC", "INFORMIX-SHM-JDBC-1", "缓冲区读命中率", "%", "缓冲区读命中率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-SHM-JDBC", "INFORMIX-SHM-JDBC-2", "缓冲区写命中率", "%", "缓冲区写命中率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-SHM-JDBC", "INFORMIX-SHM-JDBC-3", "缓冲区等待速率", "次/秒", "缓冲区等待速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-SHM-JDBC", "INFORMIX-SHM-JDBC-4", "LRU写速率", "次/秒", "最近最少使用（LRU）写入速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-SHM-JDBC", "INFORMIX-SHM-JDBC-5", "顺序扫描速率", "次/秒", "缓冲区顺序扫描速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-SHM-JDBC", "INFORMIX-SHM-JDBC-6", "Latch等待速率", "次/秒", "Latch等待速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-SHM-JDBC", "INFORMIX-SHM-JDBC-7", "缓冲刷新到磁盘速率", "次/秒", "缓冲刷新到磁盘速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-SHM-JDBC", "INFORMIX-SHM-JDBC-8", "Foreground写速", "次/秒", "前台写入速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-SHM-JDBC", "INFORMIX-SHM-JDBC-9", "块写速率", "次/秒", "检查点过程中的块写入速率", MonitorItemType.NUMBER),

				new MonitorItem("INFORMIX-SORT-JDBC","INFORMIX-SORT-JDBC-1", "内存排序速率", "次/秒", "内存排序速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-SORT-JDBC","INFORMIX-SORT-JDBC-2", "磁盘排序速率", "次/秒", "磁盘排序速率", MonitorItemType.NUMBER),

				new MonitorItem("INFORMIX-TRANS-JDBC","INFORMIX-TRANS-JDBC-1", "事务提交速率", "个/秒", "事务提交速率", MonitorItemType.NUMBER),
				new MonitorItem("INFORMIX-TRANS-JDBC","INFORMIX-TRANS-JDBC-2", "事务回滚速率", "个/秒", "事务回滚速率", MonitorItemType.NUMBER), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {
				new MonitorMethodType(InformixMonitorMethodOption.TYPE_ID, "Informix JDBC 监测协议", InformixParamPanel.class), };
	}

}
