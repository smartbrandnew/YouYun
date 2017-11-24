package com.broada.carrier.monitor.impl.db.db2;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.db.db2.bp.Db2BufferPoolMonitor;
import com.broada.carrier.monitor.impl.db.db2.lock.Db2LockMonitor;
import com.broada.carrier.monitor.impl.db.db2.sort.Db2SortMonitor;
import com.broada.carrier.monitor.impl.db.db2.tablespace.Db2TableSpaceMonitor;
import com.broada.carrier.monitor.impl.db.db2.tablespacecont.Db2TableSpaceContainerMonitor;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodConfig;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class DB2MonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "DB2" };
		String[] methodTypeIds = new String[] { DB2MonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("DB2","DB2", "DB2数据库 [可用性]", "监测DB2服务器的运行情况,获取当前连接数等性能参数。",
						Db2ParamConfiger.class.getName(),
						Db2Monitor.class.getName(),
						index++, targetTypeIds, methodTypeIds),

				new MonitorType("DB2","DB2-BUFFERPOOL-JDBC", "DB2缓冲池信息监测", "通过JDBC监测DB2的缓冲池信息",
						SingleInstanceConfiger.class.getName(),
						Db2BufferPoolMonitor.class.getName(),
						index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("DB2","DB2-LOCK-JDBC", "DB2锁监测", "监测DB2锁",
						SingleInstanceConfiger.class.getName(),
						Db2LockMonitor.class.getName(),
						index++, targetTypeIds, methodTypeIds),
						
//				new MonitorType("DB2-LOCKEDTABLE-JDBC", "DB2锁表信息", "监测DB2被锁定的表信息",
//						MultiInstanceConfiger.class.getName(),
//						Db2LockedTableMonitor.class.getName(),
//						index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("DB2","DB2-SORT-JDBC", "DB2排序信息监测", "通过JDBC监测DB2的排序信息",
						SingleInstanceConfiger.class.getName(),
						Db2SortMonitor.class.getName(),
						index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("DB2","DB2-TABLESPACE", "DB2表空间监测", "监测DB2表空间",
						MultiInstanceConfiger.class.getName(),
						Db2TableSpaceMonitor.class.getName(),
						index++, targetTypeIds, methodTypeIds),
						
				new MonitorType("DB2","DB2-TBSCONT", "DB2表空间容器监测", "监测DB2表空间容器",
						MultiInstanceConfiger.class.getName(),
						Db2TableSpaceContainerMonitor.class.getName(),
						index++, targetTypeIds, methodTypeIds),						
		};
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("DB2","DB2-1", "数据库状态", "", "DB2数据库实例的当前状态", MonitorItemType.TEXT),
				new MonitorItem("DB2","DB2-2", "数据库激活时间", "", "数据库级别第一次连接或激活的时间", MonitorItemType.TEXT),
				new MonitorItem("DB2","DB2-3", "当前连接数", "个", "当前连接至数据库的连接个数", MonitorItemType.NUMBER),
				new MonitorItem("DB2","DB2-4", "连接总次数", "次", "最近一次启动后到当前时间的连接次数总和", MonitorItemType.NUMBER),
				new MonitorItem("DB2","DB2-5", "最后一次备份时间", "", "最近一次备份的时间", MonitorItemType.TEXT),
				
				new MonitorItem("DB2-BUFFERPOOL-JDBC","DB2-BUFFERPOOL-JDBC-1", "缓冲池数据逻辑读次数", "次", "从缓冲池（逻辑）中对常规表空间和大型表空间请求的数据页的数目", MonitorItemType.NUMBER),
				new MonitorItem("DB2-BUFFERPOOL-JDBC","DB2-BUFFERPOOL-JDBC-2", "缓冲池索引逻辑读次数", "次", "从缓冲池（逻辑）中对常规表空间和大型表空间请求的索引页的数目", MonitorItemType.NUMBER),
				new MonitorItem("DB2-BUFFERPOOL-JDBC","DB2-BUFFERPOOL-JDBC-3", "缓冲池数据物理读次数", "次", "从表空间容器（物理）中对常规表空间和大型表空间读取的数据页的数目", MonitorItemType.NUMBER),
				new MonitorItem("DB2-BUFFERPOOL-JDBC","DB2-BUFFERPOOL-JDBC-4", "缓冲池索引物理读次数", "次", "从表空间容器（物理）中对常规表空间和大型表空间读取的索引页的数目", MonitorItemType.NUMBER),
				new MonitorItem("DB2-BUFFERPOOL-JDBC","DB2-BUFFERPOOL-JDBC-5", "数据缓冲池命中率", "%", "缓冲池中数据页的命中率", MonitorItemType.NUMBER),
				new MonitorItem("DB2-BUFFERPOOL-JDBC","DB2-BUFFERPOOL-JDBC-6", "索引缓冲池命中率", "%", "缓冲池中索引页的命中率", MonitorItemType.NUMBER),
				new MonitorItem("DB2-BUFFERPOOL-JDBC","DB2-BUFFERPOOL-JDBC-7", "直接读次数", "次", "不使用缓冲池进行的读取操作数", MonitorItemType.NUMBER),
				new MonitorItem("DB2-BUFFERPOOL-JDBC","DB2-BUFFERPOOL-JDBC-8", "直接写次数", "次", "不使用缓冲池进行的写入操作数", MonitorItemType.NUMBER),
				new MonitorItem("DB2-BUFFERPOOL-JDBC","DB2-BUFFERPOOL-JDBC-9", "目录缓冲命中率", "%", "目录高速缓存避免目录访问的效果", MonitorItemType.NUMBER),
				new MonitorItem("DB2-BUFFERPOOL-JDBC","DB2-BUFFERPOOL-JDBC-10", "包缓冲命中率", "%", "程序包高速缓存命中率显示程序包高速缓存的使用是否有效率", MonitorItemType.NUMBER),
				
				new MonitorItem("DB2-LOCK-JDBC","DB2-LOCK-JDBC-1", "死锁数", "个", "死锁数", MonitorItemType.NUMBER),
				new MonitorItem("DB2-LOCK-JDBC","DB2-LOCK-JDBC-2", "锁升级率", "%", "锁升级率", MonitorItemType.NUMBER),
				new MonitorItem("DB2-LOCK-JDBC","DB2-LOCK-JDBC-3", "锁等待率", "%", "锁等待率", MonitorItemType.NUMBER),
				
//				new MonitorItem("DB2-LOCKEDTABLE-JDBC-1", "Schema", "", "Schema", MonitorItemType.TEXT),
//				new MonitorItem("DB2-LOCKEDTABLE-JDBC-2", "表空间", "", "表空间", MonitorItemType.TEXT),
//				new MonitorItem("DB2-LOCKEDTABLE-JDBC-3", "锁模式", "", "锁模式", MonitorItemType.TEXT),
//				new MonitorItem("DB2-LOCKEDTABLE-JDBC-4", "锁状态", "", "锁状态", MonitorItemType.TEXT),
				
				new MonitorItem("DB2-SORT-JDBC","DB2-SORT-JDBC-1", "应用排序数", "个", "已执行的总排序数", MonitorItemType.NUMBER),
				new MonitorItem("DB2-SORT-JDBC","DB2-SORT-JDBC-2", "排序溢出百分比", "%", "必须溢出至磁盘的排序的百分比", MonitorItemType.NUMBER),

				new MonitorItem("DB2-TABLESPACE", "DB2-TABLESPACE-1", "表空间类型", "", "表空间类型", MonitorItemType.TEXT),
				new MonitorItem("DB2-TABLESPACE", "DB2-TABLESPACE-2", "页长", "B", "页长", MonitorItemType.NUMBER),
				new MonitorItem("DB2-TABLESPACE", "DB2-TABLESPACE-3", "总页数", "个", "总页数", MonitorItemType.NUMBER),
				new MonitorItem("DB2-TABLESPACE", "DB2-TABLESPACE-4", "已使用页", "个", "已使用页", MonitorItemType.NUMBER),
				new MonitorItem("DB2-TABLESPACE", "DB2-TABLESPACE-5", "空闲页", "个", "空闲页", MonitorItemType.NUMBER),
				new MonitorItem("DB2-TABLESPACE", "DB2-TABLESPACE-6", "空闲率", "%", "空闲率", MonitorItemType.NUMBER),
				new MonitorItem("DB2-TABLESPACE", "DB2-TABLESPACE-7", "使用率", "%", "使用率", MonitorItemType.NUMBER),
				new MonitorItem("DB2-TABLESPACE", "DB2-TABLESPACE-8", "预取大小", "KB", "预取大小", MonitorItemType.NUMBER),
				new MonitorItem("DB2-TABLESPACE", "DB2-TABLESPACE-9", "扩展数据块大小", "KB", "扩展数据块大小", MonitorItemType.NUMBER),
				new MonitorItem("DB2-TABLESPACE", "DB2-TABLESPACE-10", "表空间状态", "", "表空间状态", MonitorItemType.TEXT),
				
				new MonitorItem("DB2-TBSCONT","DB2-TBSCONT-1", "表空间名称", "", "表空间名称", MonitorItemType.TEXT),
				new MonitorItem("DB2-TBSCONT","DB2-TBSCONT-2", "容器类型", "", "容器类型", MonitorItemType.TEXT),
				new MonitorItem("DB2-TBSCONT","DB2-TBSCONT-3", "已使用页", "个", "已使用页", MonitorItemType.NUMBER),
				new MonitorItem("DB2-TBSCONT","DB2-TBSCONT-4", "总页数", "个", "总页数", MonitorItemType.NUMBER),
				new MonitorItem("DB2-TBSCONT","DB2-TBSCONT-5", "使用率", "%", "使用率", MonitorItemType.NUMBER),
		};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {
				new MonitorMethodType(DB2MonitorMethodOption.TYPE_ID, "DB2监测协议", DB2MonitorMethodConfig.class),
		};
	}

}
