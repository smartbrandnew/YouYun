package com.broada.carrier.monitor.impl.db.dm;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.db.dm.basic.DmBasicMonitor;
import com.broada.carrier.monitor.impl.db.dm.bufferPool.DmBufferMonitor;
import com.broada.carrier.monitor.impl.db.dm.cache.DmCacheMonitor;
import com.broada.carrier.monitor.impl.db.dm.checkpoint.DmCheckpointMonitor;
import com.broada.carrier.monitor.impl.db.dm.dtfile.DmDtfileMonitor;
import com.broada.carrier.monitor.impl.db.dm.links.DmLinkMonitor;
import com.broada.carrier.monitor.impl.db.dm.lock.DmLockMonitor;
import com.broada.carrier.monitor.impl.db.dm.logBuf.DmLogBufMonitor;
import com.broada.carrier.monitor.impl.db.dm.logFile.DmLogFileMonitor;
import com.broada.carrier.monitor.impl.db.dm.patchRate.DmPatchRateMonitor;
import com.broada.carrier.monitor.impl.db.dm.ramPool.DmRamPoolMonitor;
import com.broada.carrier.monitor.impl.db.dm.redolog.DmRedoLogMonitor;
import com.broada.carrier.monitor.impl.db.dm.sessions.DmSessInfoMonitor;
import com.broada.carrier.monitor.impl.db.dm.sql.DmSQLMonitor;
import com.broada.carrier.monitor.impl.db.dm.thread.DmThreadMonitor;
import com.broada.carrier.monitor.impl.db.dm.transactions.DmTrxMonitor;
import com.broada.carrier.monitor.method.dm.DmMonitorMethodOption;
import com.broada.carrier.monitor.method.dm.DmParamPanel;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class DmMonitorPackage implements MonitorPackage {
	String[] targetTypeIds = new String[] { "DM" };
	String[] methodTypeIds = new String[] { DmMonitorMethodOption.TYPE_ID };
	int index = 1;

	@Override
	public MonitorType[] getTypes() {
		return new MonitorType[] {
				new MonitorType("DM", "DM-BASIC", "DM基础监测 [可用性]", "监测DM数据库的基本信息和运行状态",
						SingleInstanceConfiger.class.getName(), DmBasicMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("DM", "DM-CHECKPOINT", "DM检查点监测",
						"监控数据库写程序（DBWR）检查点完成的次数，以及服务器请求数据库写程序检查点数。建议监测周期为一个小时。",
						SingleInstanceConfiger.class.getName(), DmCheckpointMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-DATAFILE", "DM数据文件监测", "监测DM数据文件的大小，在线状态，读写次数/修改时间等信息，可以设定告警条件。",
						MultiInstanceConfiger.class.getName(), DmDtfileMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("DM", "DM-REDOLOG", "DM-Redo日志配置", "采集DM数据库Redo日志相关配置信息,并可设定告警条件。",
						SingleInstanceConfiger.class.getName(), DmRedoLogMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-PATCHRATE", "DM碎片监测", "监测DM数据库的碎片使用情况",
						MultiInstanceConfiger.class.getName(), DmPatchRateMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-SESS", "DM会话监测", "监测DM数据库的会话信息", MultiInstanceConfiger.class.getName(),
						DmSessInfoMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-BUFFERPOOL", "DM内存缓冲池监测", "监测DM数据库的内存缓冲池信息",
						MultiInstanceConfiger.class.getName(), DmBufferMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("DM", "DM-LOGFILES", "DM日志文件监测", "监测DM数据库的日志文件信息",
						SingleInstanceConfiger.class.getName(), DmLogFileMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-CACHE", "DM缓存监测", "监测DM数据库的缓存信息", MultiInstanceConfiger.class.getName(),
						DmCacheMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-LOGBUF", "DM日志缓存监测", "监测DM数据库的日志缓存信息",
						SingleInstanceConfiger.class.getName(), DmLogBufMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-RAMPOOL", "DM内存池监测", "监测DM数据库的内存池信息", SingleInstanceConfiger.class.getName(),
						DmRamPoolMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-LOCK", "DM锁监测", "监测DM数据库的锁信息", MultiInstanceConfiger.class.getName(),
						DmLockMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-TRX", "DM事务监测", "监测DM数据库的事务信息", MultiInstanceConfiger.class.getName(),
						DmTrxMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-SQL", "DM-SQL监测", "监测DM数据库的SQL信息", MultiInstanceConfiger.class.getName(),
						DmSQLMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-THREAD", "DM-线程监测", "监测DM数据库的线程信息", MultiInstanceConfiger.class.getName(),
						DmThreadMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("DM", "DM-LINK", "DM-链接监测", "监测DM数据库的链接信息", MultiInstanceConfiger.class.getName(),
						DmLinkMonitor.class.getName(), index++, targetTypeIds, methodTypeIds), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("DM-BASIC","DM-BASIC-1", "DBMS名称", "", "数据库管理系统名称", MonitorItemType.TEXT),
				new MonitorItem("DM-BASIC","DM-BASIC-2", "产品名称", "", "数据库产品名称", MonitorItemType.TEXT),
				new MonitorItem("DM-BASIC","DM-BASIC-3", "数据库模式", "", "数据库系统模式", MonitorItemType.TEXT),
				new MonitorItem("DM-BASIC","DM-BASIC-4", "产品版本", "", "数据库产品版本", MonitorItemType.TEXT),
				new MonitorItem("DM-BASIC","DM-BASIC-5", "主机名称", "", "主机名称", MonitorItemType.TEXT),
				new MonitorItem("DM-BASIC","DM-BASIC-6", "数据库状态", "", "数据库实例状态", MonitorItemType.TEXT),

				new MonitorItem("DM-CHECKPOINT", "DM-CHECKPOINT-1", "发生检查点数", "次", "发生检查点数", MonitorItemType.NUMBER),
				new MonitorItem("DM-CHECKPOINT", "DM-CHECKPOINT-2", "重储检查发生点数", "次", "重储检查发生点数", MonitorItemType.NUMBER),
				new MonitorItem("DM-CHECKPOINT", "DM-CHECKPOINT-3", "检查耗费时间", "毫秒", "检查已耗费时间", MonitorItemType.NUMBER),

				new MonitorItem("DM-DATAFILE","DM-DATAFILE-1", "读次数", "次", "指定数据文件的读次数", MonitorItemType.NUMBER),
				new MonitorItem("DM-DATAFILE","DM-DATAFILE-2", "写次数", "次", "指定数据文件的写次数", MonitorItemType.NUMBER),
				new MonitorItem("DM-DATAFILE","DM-DATAFILE-3", "文件总大小", "MB", "指定数据文件的总大小", MonitorItemType.NUMBER),
				new MonitorItem("DM-DATAFILE","DM-DATAFILE-4", "文件剩余大小", "MB", "指定数据文件的可用大小", MonitorItemType.NUMBER),

				new MonitorItem("DM-REDOLOG","DM-REDOLOG-1", "重做日志缓冲中用户进程不能分配空间的次数", "次", "重做日志缓冲中用户进程不能分配空间的次数",
						MonitorItemType.NUMBER),
				new MonitorItem("DM-REDOLOG","DM-REDOLOG-2", "归档重做日志文件的数目", "次", "归档重做日志文件的数目", MonitorItemType.NUMBER),
				new MonitorItem("DM-REDOLOG","DM-REDOLOG-3", "重做条目的平均大小", "MB", "重做条目的平均大小", MonitorItemType.NUMBER),

				new MonitorItem("DM-PATCHRATE", "DM-PATCHRATE-1", "FSFI值", "%", "DM自由空间碎片索引比值", MonitorItemType.NUMBER),

				new MonitorItem("DM-SESS", "DM-SESS-1", "会话ID", "", "会话ID", MonitorItemType.NUMBER),
				new MonitorItem("DM-SESS", "DM-SESS-2", "当前用户", "", "当前用户", MonitorItemType.NUMBER),
				new MonitorItem("DM-SESS", "DM-SESS-3", "SQL语句", "", "SQL语句", MonitorItemType.NUMBER),
				new MonitorItem("DM-SESS", "DM-SESS-4", "会话状态", "", "会话状态", MonitorItemType.NUMBER),
				new MonitorItem("DM-SESS", "DM-SESS-5", "当前模式", "", "当前模式", MonitorItemType.NUMBER),
				new MonitorItem("DM-SESS", "DM-SESS-6", "创建时间", "", "创建时间", MonitorItemType.NUMBER),
				new MonitorItem("DM-SESS", "DM-SESS-7", "客户类型", "", "客户类型", MonitorItemType.NUMBER),
				new MonitorItem("DM-SESS", "DM-SESS-8", "是否自动提交", "", "是否自动提交", MonitorItemType.NUMBER),
				new MonitorItem("DM-SESS", "DM-SESS-9", "客户主机名", "", "客户主机名", MonitorItemType.NUMBER),

				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-1", "缓冲区名称", "", "缓冲区名称", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-2", "页大小", "页", "页大小", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-3", "页数", "页", "页数", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-4", "正在使用的页数", "页", "正在使用的页数", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-5", "空闲页数", "页", "空闲页数", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-6", "脏页数", "页", "脏页数", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-7", "非空闲页数目", "页", "非空闲页数目", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-8", "最多的页数", "页", "最多的页数", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-9", "READ命中的次数", "页", "READ命中的次数", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-10", "淘汰的页数", "页", "淘汰的页数", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-11", "READ未命中的页数", "页", "READ未命中的页数", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-12", "批量读的页数", "页", "批量读的页数", MonitorItemType.NUMBER),
				new MonitorItem("DM-BUFFERPOOL","DM-BUFFERPOOL-13", "命中率", "%", "命中率", MonitorItemType.NUMBER),

				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-1", "检查点LSN", "", "检查点LSN", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-2", "文件LSN", "", "文件LSN", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-3", "刷新LSN", "", "刷新LSN", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-4", "当前LSN", "", "当前LSN", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-5", "下一个页序列", "", "下一个页序列", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-6", "页Magic值", "", "页Magic值", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-7", "刷新页数", "", "刷新页数", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-8", "正在刷新的页", "", "正在刷新的页", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-9", "当前文件", "", "当前文件", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-10", "当前偏移量", "", "当前偏移量", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-11", "检查点文件", "", "检查点文件", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-12", "检查点文件偏移量", "", "检查点文件偏移量", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-13", "空闲大小", "MB", "空闲大小", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-14", "总大小", "MB", "总大小", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGFILES", "DM-LOGFILES-15", "利用率", "%", "利用率", MonitorItemType.NUMBER),

				new MonitorItem("DM-CACHE","DM-CACHE-1", "地址", "", "地址", MonitorItemType.NUMBER),
				new MonitorItem("DM-CACHE","DM-CACHE-2", "类型", "", "类型", MonitorItemType.NUMBER),
				new MonitorItem("DM-CACHE","DM-CACHE-3", "是否溢出", "", "是否溢出", MonitorItemType.NUMBER),
				new MonitorItem("DM-CACHE","DM-CACHE-4", "是否在内存池中", "", "是否在内存池中", MonitorItemType.NUMBER),
				new MonitorItem("DM-CACHE","DM-CACHE-5", "是否禁用", "", "是否禁用", MonitorItemType.NUMBER),
				new MonitorItem("DM-CACHE","DM-CACHE-6", "被引用的次数", "", "被引用的次数", MonitorItemType.NUMBER),
				new MonitorItem("DM-CACHE","DM-CACHE-7", "时间戳", "", "时间戳", MonitorItemType.NUMBER),

				new MonitorItem("DM-LOGBUF", "DM-LOGBUF-1", "开始LSN", "", "开始LSN", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGBUF", "DM-LOGBUF-2", "结束LSN", "", "开始LSN", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGBUF", "DM-LOGBUF-3", "总页数", "页", "总页数", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGBUF", "DM-LOGBUF-4", "使用的页数", "页", "已用页数", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOGBUF", "DM-LOGBUF-5", "利用率", "%", "利用率", MonitorItemType.NUMBER),

				new MonitorItem("DM-RAMPOOL","DM-RAMPOOL-1", "个数", "个", "个数", MonitorItemType.NUMBER),
				new MonitorItem("DM-RAMPOOL","DM-RAMPOOL-2", "总大小", "KB", "总大小", MonitorItemType.NUMBER),
				new MonitorItem("DM-RAMPOOL","DM-RAMPOOL-3", "空闲大小", "KB", "空闲大小", MonitorItemType.NUMBER),
				new MonitorItem("DM-RAMPOOL","DM-RAMPOOL-4", "已用大小", "KB", "已用大小", MonitorItemType.NUMBER),
				new MonitorItem("DM-RAMPOOL","DM-RAMPOOL-5", "使用率", "%", "使用率", MonitorItemType.NUMBER),

				new MonitorItem("DM-LOCK","DM-LOCK-1", "会话ID", "", "会话ID", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOCK","DM-LOCK-2", "事务ID", "", "事务ID", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOCK","DM-LOCK-3", "产生锁的SQL", "", "产生锁的SQL语句", MonitorItemType.NUMBER),
				new MonitorItem("DM-LOCK","DM-LOCK-4", "锁发生时间", "", "锁发生时间", MonitorItemType.NUMBER),

				new MonitorItem("DM-TRX","DM-TRX-1", "事务ID", "", "事务ID", MonitorItemType.NUMBER),
				new MonitorItem("DM-TRX","DM-TRX-2", "事务状态", "", "事务状态", MonitorItemType.NUMBER),
				new MonitorItem("DM-TRX","DM-TRX-3", "隔离级", "", "隔离级", MonitorItemType.NUMBER),
				new MonitorItem("DM-TRX","DM-TRX-4", "是否为只读事务", "", "是否为只读事务", MonitorItemType.NUMBER),
				new MonitorItem("DM-TRX","DM-TRX-5", "会话ID", "", "会话ID", MonitorItemType.NUMBER),
				new MonitorItem("DM-TRX","DM-TRX-6", "插入数目", "条", "插入数目", MonitorItemType.NUMBER),
				new MonitorItem("DM-TRX","DM-TRX-7", "删除数目", "条", "删除数目", MonitorItemType.NUMBER),
				new MonitorItem("DM-TRX","DM-TRX-8", "更新数目", "条", "更新数目", MonitorItemType.NUMBER),
				new MonitorItem("DM-TRX","DM-TRX-9", "更新插入数目", "条", "更新插入数目", MonitorItemType.NUMBER),
				new MonitorItem("DM-TRX","DM-TRX-10", "当前UNDO记录的递增系列号", "", "当前UNDO记录的递增系列号", MonitorItemType.NUMBER),
				new MonitorItem("DM-TRX","DM-TRX-11", "事务等待的锁", "个", "事务等待的锁", MonitorItemType.NUMBER),

				new MonitorItem("DM-SQL","DM-SQL-1", "序列号", "", "序列号", MonitorItemType.NUMBER),
				new MonitorItem("DM-SQL","DM-SQL-2", "会话ID", "", "会话ID", MonitorItemType.NUMBER),
				new MonitorItem("DM-SQL","DM-SQL-3", "事务ID", "", "事务ID", MonitorItemType.NUMBER),
				new MonitorItem("DM-SQL","DM-SQL-4", "第一层SQL", "", "第一层SQL", MonitorItemType.NUMBER),
				new MonitorItem("DM-SQL","DM-SQL-5", "开始时间", "ms", "开始时间", MonitorItemType.NUMBER),
				new MonitorItem("DM-SQL","DM-SQL-6", "使用时间", "ms", "使用时间", MonitorItemType.NUMBER),
				new MonitorItem("DM-SQL","DM-SQL-7", "是否结束", "", "是否结束", MonitorItemType.NUMBER),
				new MonitorItem("DM-SQL","DM-SQL-8", "用户名", "", "用户名", MonitorItemType.NUMBER),
				new MonitorItem("DM-SQL","DM-SQL-9", "IP地址", "", "IP地址", MonitorItemType.NUMBER),
				new MonitorItem("DM-SQL","DM-SQL-10", "应用名称", "", "应用名称", MonitorItemType.NUMBER),

				new MonitorItem("DM-THREAD","DM-THREAD-1", "线程ID", "", "线程ID", MonitorItemType.NUMBER),
				new MonitorItem("DM-THREAD","DM-THREAD-2", "线程名", "", "线程名", MonitorItemType.NUMBER),
				new MonitorItem("DM-THREAD","DM-THREAD-3", "线程开始时间", "", "线程开始时间", MonitorItemType.NUMBER),

				new MonitorItem("DM-LINK","DM-LINK-1", "ID", "", "ID", MonitorItemType.NUMBER),
				new MonitorItem("DM-LINK","DM-LINK-2", "名称", "", "名称", MonitorItemType.NUMBER),
				new MonitorItem("DM-LINK","DM-LINK-3", "是否为公共", "", "是否为公共", MonitorItemType.NUMBER),
				new MonitorItem("DM-LINK","DM-LINK-4", "登录名", "", "登录名", MonitorItemType.NUMBER),
				new MonitorItem("DM-LINK","DM-LINK-5", "主机名", "", "主机名", MonitorItemType.NUMBER),
				new MonitorItem("DM-LINK","DM-LINK-6", "端口号", "", "端口号", MonitorItemType.NUMBER),
				new MonitorItem("DM-LINK","DM-LINK-7", "当前是否已连接", "", "当前是否已连接", MonitorItemType.NUMBER),
				new MonitorItem("DM-LINK","DM-LINK-8", "链接类型", "", "链接类型", MonitorItemType.NUMBER),
				new MonitorItem("DM-LINK","DM-LINK-9", "通信协议", "", "通信协议", MonitorItemType.NUMBER),
				new MonitorItem("DM-LINK","DM-LINK-10", "是否使用", "", "是否使用", MonitorItemType.NUMBER),

		};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(DmMonitorMethodOption.TYPE_ID, "Dm JDBC 监测协议",
				DmParamPanel.class), };
	}

}
