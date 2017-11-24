package com.broada.carrier.monitor.impl.db.oracle;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SpecificMultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SpecificSingleInstanceConfig;
import com.broada.carrier.monitor.impl.db.oracle.advancedQueue.OracleAdvancedQueueMonitor;
import com.broada.carrier.monitor.impl.db.oracle.asm.OracleDiskASMMonitor;
import com.broada.carrier.monitor.impl.db.oracle.asm.disk.OracleASMDiskStateMonitor;
import com.broada.carrier.monitor.impl.db.oracle.asm.diskgroup.OracleASMDiskGroupMonitor;
import com.broada.carrier.monitor.impl.db.oracle.basic.OracleBaseMonitor;
import com.broada.carrier.monitor.impl.db.oracle.checkpoint.OracleCheckpointMonitor;
import com.broada.carrier.monitor.impl.db.oracle.dtfile.OracleDtfileMonitor;
import com.broada.carrier.monitor.impl.db.oracle.fts.OracleFTSMonitor;
import com.broada.carrier.monitor.impl.db.oracle.lock.OracleLockMonitor;
import com.broada.carrier.monitor.impl.db.oracle.lock.OracleLockParamConfiger;
import com.broada.carrier.monitor.impl.db.oracle.patchRate.OraclePatchRateMonitor;
import com.broada.carrier.monitor.impl.db.oracle.pga.OraclePGAMonitor;
import com.broada.carrier.monitor.impl.db.oracle.process.OracleProcessMonitor;
import com.broada.carrier.monitor.impl.db.oracle.rac.OracleRacMonitor;
import com.broada.carrier.monitor.impl.db.oracle.ratio.OracleRatioMonitor;
import com.broada.carrier.monitor.impl.db.oracle.recursion.OracleRecursionMonitor;
import com.broada.carrier.monitor.impl.db.oracle.redolog.OracleRedoLogMonitor;
import com.broada.carrier.monitor.impl.db.oracle.session.wait.OracleSessionWaitMonitor;
import com.broada.carrier.monitor.impl.db.oracle.sga.OracleSGAMonitor;
import com.broada.carrier.monitor.impl.db.oracle.tablespace.OracleTableSpaceConfiger;
import com.broada.carrier.monitor.impl.db.oracle.tablespace.OracleTableSpaceMonitor;
import com.broada.carrier.monitor.impl.db.oracle.undostat.OracleUndostatMonitor;
import com.broada.carrier.monitor.impl.db.oracle.user.OracleUserStateMonitor;
import com.broada.carrier.monitor.impl.db.oracle.wait.OracleWaitEventMonitor;
import com.broada.carrier.monitor.impl.db.oracle.workQueue.OracleWorkQueueMonitor;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.method.oracle.OracleMethodConfiger;
import com.broada.carrier.monitor.method.oracle.OracleRACMethod;
import com.broada.carrier.monitor.method.oracle.OracleRACMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class OracleMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Oracle" };
		String[] methodTypeIds = new String[] { OracleMethod.TYPE_ID };
		String[] methodTypeIds_ = new String[] { OracleRACMethod.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("ORACLE", "ORACLE-BASE", "ORACLE基本信息采集", "采集Oracle数据库的基本配置信息和数据库的名称、版本信息等。",
						SpecificSingleInstanceConfig.class.getName(), OracleBaseMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-TABLESPACE", "Oracle表空间监测", "监测Oracle表空间使用情况",
						OracleTableSpaceConfiger.class.getName(), OracleTableSpaceMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				/*
				new MonitorType("ORACLE", "ORACLE-SECACCESS", "ORACLE安全访问监测",
						"可以设置复杂的安全访问规则，比如什么用户只能在什么时候登录数据库服务器等,如果有不符合规则的登录会话则告警。",
						OracleSecAccessParamConfiger.class.getName(), OracleSecAccessMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				*/
				new MonitorType("ORACLE", "ORACLE-LOCK", "Oracle资源锁定监测", "监测Oracle资源的锁定情况",
						OracleLockParamConfiger.class.getName(), OracleLockMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-RATIO", "Oracle命中率监测", "监测Oracle数据库命中率情况",
						SingleInstanceConfiger.class.getName(), OracleRatioMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				/*
				 * new MonitorType("ORACLE-RMAN", "OracleRman备份监测",
				 * "监测OracleRman备份情况。(这个监测器为自定义监测器，必须在特定的环境下才能配置使用。)",
				 * SingleInstanceConfiger.class.getName(),
				 * OracleRmanMonitor.class.getName(), index++, targetTypeIds,
				 * methodTypeIds),
				 */
				new MonitorType("ORACLE", "ORACLE-DATAFILE", "ORACLE数据文件监测",
						"监测Oracle数据文件的大小，在线状态，读写次数/时间等信息，可以设定告警条件。", SpecificMultiInstanceConfiger.class.getName(),
						OracleDtfileMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-SGA", "ORACLE-SGA配置", "采集Oracle数据库SGA相关配置信息,并可设定告警条件。",
						SingleInstanceConfiger.class.getName(), OracleSGAMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-PGA", "ORACLE-PGA配置", "采集Oracle数据库PGA相关配置信息,并可设定告警条件。",
						SingleInstanceConfiger.class.getName(), OraclePGAMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-PATCHRATE", "Oracle碎片监测", "监测Oracle数据库的碎片使用情况",
						MultiInstanceConfiger.class.getName(), OraclePatchRateMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-FTS", "ORACLE-全表扫描配置", "获取Oracle数据库全表扫描相关配置信息,并可设定告警条件。",
						SingleInstanceConfiger.class.getName(), OracleFTSMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-UNDOSTAT", "ORACLE撤销空间监测状态监测",
						"撤消空间,监控无空间错误和快照太旧错误。当无空间计数超过指定阈值时，或快照太旧错误计数超过指定阈值时，监控系统将产生报警事件。",
						SingleInstanceConfiger.class.getName(), OracleUndostatMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-WORKQUE", "Oracle作业队列监测", "Oracle作业队列监测中破损\\失败\\过期作业的数量。",
						SingleInstanceConfiger.class.getName(), OracleWorkQueueMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-RECURSION", "ORACLE-递归调用信息监测", "获取Oracle数据库递归调用相关信息,并可设定告警条件。",
						SingleInstanceConfiger.class.getName(), OracleRecursionMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-REDOLOG", "ORACLE-Redo日志配置", "采集Oracle数据库Redo日志相关配置信息,并可设定告警条件。",
						SingleInstanceConfiger.class.getName(), OracleRedoLogMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType(
						"ORACLE",
						"ORACLE-CHECKPOINT",
						"ORACLE检查点监测",
						"监控数据库写程序（DBWR）检查点完成的次数，以及服务器请求数据库写程序检查点数。注意:请将监测项“历史发生监测点”和“历史完成检查点”勾选上，否则后面两项监测项将不准确。建议监测周期为一个小时。",
						SingleInstanceConfiger.class.getName(), OracleCheckpointMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-ADVANCEDQUE", "Oracle高级队列监测",
						"Oracle高级队列中消息总数量,ready消息数量,消息平均访问时间,异常情况。", MultiInstanceConfiger.class.getName(),
						OracleAdvancedQueueMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("ORACLE", "ORACLE-PROCESS", "ORACLE-进程资源消耗监测",
						"Oracle进程资源消耗信息监测,并可设定告警条件。当比较符为\" - \"时不做比较。", MultiInstanceConfiger.class.getName(),
						OracleProcessMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),
				// OracleDiskASMMonitor.TYPE,
				new MonitorType("ORACLE", "ORACLE-ASM", "Oracle-ASM监测", "Oracle-ASM磁盘组使用率监测。",
						MultiInstanceConfiger.class.getName(), OracleDiskASMMonitor.class.getName(), 1,
						new String[] { "Oracle" }, new String[] { OracleMethod.TYPE_ID }),

				new MonitorType("ORACLE", "ORACLE-RAC", "ORACLE-RAC实例监测", "OracleRAC实例启动信息监测。",
						MultiInstanceConfiger.class.getName(), OracleRacMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds_),
						
				new MonitorType("ORACLE", "ORACLE-ASM-DISK", "ORACLE-ASM磁盘监测", "ORACLE-ASM磁盘监测。",
						MultiInstanceConfiger.class.getName(), OracleASMDiskStateMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds_),
				
				new MonitorType("ORACLE", "ORACLE-ASM-DISK_GROUP", "ORACLE-ASM磁盘组监测", "ORACLE-ASM磁盘组监测。",
						MultiInstanceConfiger.class.getName(), OracleASMDiskGroupMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds_),
						
				new MonitorType("ORACLE", "ORACLE-WAIT-EVENT", "ORACLE-等待事件监测", "ORACLE-等待事件监测。",
						MultiInstanceConfiger.class.getName(), OracleWaitEventMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds_),
				
				new MonitorType("ORACLE", "ORACLE-SESSION-WAIT", "ORACLE-锁等待监测", "ORACLE-锁等待监测。",
						MultiInstanceConfiger.class.getName(), OracleSessionWaitMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds_),
				
				new MonitorType("ORACLE", "ORACLE-USER-STATE", "ORACLE-用户状态监测", "ORACLE-用户状态监测。",
						MultiInstanceConfiger.class.getName(), OracleUserStateMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds_),
		/*
		 * new MonitorType("ORACLE-TABLESTATE", "Oracle表状态监测", "监测Oracle表的使用情况",
		 * OracleTableStateParamConfiger.class.getName(),
		 * OracleTableStateMonitor.class.getName(), index++, targetTypeIds,
		 * methodTypeIds),
		 * 
		 * new MonitorType("ORACLE-SQLINFO", "Oracle SQL监测", "监测Oracle SQL执行情况",
		 * MultiInstanceConfiger.class.getName(),
		 * OracleSqlInfoMonitor.class.getName(), index++, targetTypeIds,
		 * methodTypeIds),
		 */

		};
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-1", "实例状态", "", "数据库实例状态", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-2", "主机名", "", "数据库所在服务器的主机名", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-3", "DB名称", "", "数据库名称", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-4", "DB版本", "", "数据库版本", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-5", "位长", "位", "数据库字符位长度", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-6", "并行状态", "", "数据库并行状态", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-7", "例程名", "", "数据库实例名称", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-8", "例程开始时间", "", "数据库实例启动时间", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-9", "限制模式", "", "数据库是否处于限制模式", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-10", "归档模式", "", "数据库归档日志模式", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-11", "归档路径", "", "数据库归档路径", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-12", "只读模式", "", "数据库是否处于只读模式", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-13", "使用spfile启动", "", "数据库spfile启动路径", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-BASE","ORACLE-BASE-14", "当前连接数", "", "当前连接数", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-1", "已使用量", "MB", "Oracle表空间已使用大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-2", "已使用率", "%", "Oracle表空间已使用百分率", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-3", "最大Extent数量", "个", "Oracle表空间允许的最大Extent数量",
						MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-4", "读时间", "秒", "Oracle读表空间平均时间", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-5", "写时间", "秒", "Oracle写表空间平均时间", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-6", "当前Extent数量", "个", "Oracle表空间当前存在的Extent数量",
						MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-7", "下一个Extent大小", "KB", "Oracle表空间下一次新增Extent大小",
						MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-8", "未使用Extent数量", "个", "Oracle表空间当前未使用的Extent数量",
						MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-9", "Segment管理方式", "", "Oracle表空间Segment管理方式", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-10", "表空间类型", "", "Oracle表空间类型", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-11", "未使用量", "MB", "Oracle表空间未使用大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-12", "未使用百分率", "%", "Oracle表空间未使用百分率", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-13", "允许最大空间", "MB", "Oracle表空间设定的最大扩展大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-TABLESPACE", "ORACLE-TABLESPACE-14", "是否自动扩展", "", "Oracle表空间的扩展方式", MonitorItemType.TEXT),

				new MonitorItem("ORACLE-ADVANCEDQUE", "ORACLE-ADVANCEDQUE-1", "消息总数", "个", "队列中消息总数量", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-ADVANCEDQUE", "ORACLE-ADVANCEDQUE-2", "ready消息数", "个", "队列中ready消息总数", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-ADVANCEDQUE", "ORACLE-ADVANCEDQUE-3", "错误的消息数", "个", "不能传播并已经记录为错误的消息", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-ADVANCEDQUE", "ORACLE-ADVANCEDQUE-4", "消息平均访问时间", "秒", "队列中消息平均访问时间", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-CHECKPOINT","ORACLE-CHECKPOINT-1", "发生检查点数", "次", "发生检查点数", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-CHECKPOINT","ORACLE-CHECKPOINT-2", "完成检查点数", "次", "完成检查点数", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-DATAFILE","ORACLE-DATAFILE-1", "读次数", "次", "指定数据文件的读次数", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-DATAFILE","ORACLE-DATAFILE-2", "写次数", "次", "指定数据文件的写次数", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-DATAFILE","ORACLE-DATAFILE-3", "读时间", "秒", "指定数据文件的读时间", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-DATAFILE","ORACLE-DATAFILE-4", "写时间", "秒", "指定数据文件的写时间", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-DATAFILE","ORACLE-DATAFILE-5", "文件大小", "MB", "指定数据文件当前大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-DATAFILE","ORACLE-DATAFILE-6", "写文件块数", "", "指定数据文件写块数", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-DATAFILE","ORACLE-DATAFILE-7", "读文件块数", "", "指定数据文件读块数", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-DATAFILE","ORACLE-DATAFILE-8", "读写文件块数", "", "指定数据文件读写块数", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-FTS","ORACLE-FTS-1", "LTSCANRATIO值", "%", "Oracle长表全表扫描百分比值", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-FTS","ORACLE-FTS-2", "RSRATIO值", "%", "Oracle行源百分比值", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-LOCK","ORACLE-LOCK-1", "锁定时长", "秒", "该资源被锁定的时间的长度", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-PATCHRATE","ORACLE-PATCHRATE-1", "FSFI值", "%", "Oracle自由空间碎片索引比值", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-PGA","ORACLE-PGA-1", "PGA内存总大小", "MB", "PGA内存总大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-2", "当前可用于自动分配了的PGA大小", "MB", "当前可用于自动分配了的PGA大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-3", "自动模式下工作区域的最大大小", "MB", "自动模式下工作区域的最大大小（Oracle根据工作负载自动调整）",
						MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-4", "使用的PGA大小", "MB", "使用的PGA大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-5", "分配的PGA大小", "MB", "分配的PGA大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-6", "PGA的最大分配大小", "MB", "PGA的最大分配大小（历史最大值）", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-7", "空闲的PGA大小", "MB", "空闲的PGA大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-8", "释放的PGA大小", "MB", "释放的PGA大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-9", "自动工作区PGA使用大小", "MB", "自动工作区PGA使用大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-10", "自动工作区PGA最大使用量", "MB", "自动工作区PGA最大使用量（历史最大值）", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-11", "手动工作区PGA使用大小", "MB", "手动工作区PGA使用大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-12", "手动工作区PGA最大使用量", "MB", "手动工作区PGA最大使用量（历史最大值）", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-13", "实例启动后PGA分配次数", "次", "实例启动后PGA分配次数", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-14", "实例启动后处理的字节数", "MB", "实例启动后处理的字节数", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-15", "实例启动后额外处理（读/写）的字节数", "MB", "实例启动后额外处理（读/写）的字节数",
						MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PGA","ORACLE-PGA-16", "Cache命中率", "%", "Cache命中率", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-PROCESS","ORACLE-PROCESS-1", "已分配程序全局区", "M", "已分配程序全局区", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PROCESS","ORACLE-PROCESS-2", "已使用程序全局区", "M", "已使用程序全局区", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PROCESS","ORACLE-PROCESS-3", "可用程序全局区百分比", "%", "可用程序全局区百分比", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-PROCESS","ORACLE-PROCESS-4", "可用程序全局区", "M", "可用程序全局区", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-RATIO","ORACLE-RATIO-1", "高速缓存区命中率", "%", "Oracle高速缓存区命中率", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-RATIO","ORACLE-RATIO-2", "共享区库缓存区命中率", "%", "Oracle共享区库缓存区命中率", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-RATIO","ORACLE-RATIO-3", "共享区字典缓存区命中率", "%", "Oracle共享区字典缓存区命中率", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-RATIO","ORACLE-RATIO-4", "回退段等待次数与获取次数比率", "%", "Oracle回退段等待次数与获取次数比率", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-RATIO","ORACLE-RATIO-5", "磁盘排序与内存排序比率", "%", "Oracle磁盘排序与内存排序比率", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-RATIO","ORACLE-RATIO-6", "多次解析(重装)的条目比率", "%", "Oracle库高速缓存中多次解析(重装)的条目比率",
						MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-RECURSION","ORACLE-RECURSION-1", "时间间隔的递归调用百分比", "%", "当前时间间隔的递归调用百分比", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-RECURSION","ORACLE-RECURSION-2", "递归调用百分比", "%", "递归调用百分比", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-RECURSION","ORACLE-RECURSION-3", "递归调用速率", "个/秒", "每秒钟新递归调用的数目", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-RECURSION","ORACLE-RECURSION-4", "递归-用户调用比率", "比率", "递归-用户调用比率", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-RECURSION","ORACLE-RECURSION-5", "递归调用数", "个", "自实例创建起递归调用的数目", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-RECURSION","ORACLE-RECURSION-6", "用户调用数", "个", "自实例创建起用户调用的数目", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-REDOLOG","ORACLE-REDOLOG-1", "Willing-to-wait请求latch失败数", "次",
						"初始以Willing-to-wait请求类型请求一个latch不成功的总次数", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-REDOLOG","ORACLE-REDOLOG-2", "Willing-to-wait请求latch成功数", "次",
						"成功地以Willing-to-wait请求类型请求一个latch的总次数", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-REDOLOG","ORACLE-REDOLOG-3", "Immediate请求latch失败数", "次", "以Immediate请求类型请求一个latch不成功的总次数",
						MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-REDOLOG","ORACLE-REDOLOG-4", "Immediate请求latch成功数", "次", "以Immediate请求类型成功地获得一个latch的总次数",
						MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-REDOLOG","ORACLE-REDOLOG-5", "Willing-to-wait请求失败与获得的百分比", "%",
						"Willing-to-wait请求类型的丢失量占其获得数的百分比", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-REDOLOG","ORACLE-REDOLOG-6", "Immediate请求失败与获得的百分比", "%", "Immediate请求类型的丢失量占其获得数的百分比",
						MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-REDOLOG","ORACLE-REDOLOG-7", "重做日志缓冲中用户进程不能分配空间的次数", "次", "重做日志缓冲中用户进程不能分配空间的次数",
						MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-REDOLOG","ORACLE-REDOLOG-8", "归档重做日志文件的数目", "次", "归档重做日志文件的数目", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-REDOLOG","ORACLE-REDOLOG-9", "重做条目的平均大小", "MB", "重做条目的平均大小", MonitorItemType.NUMBER),
				/*
				 * new MonitorItem("ORACLE-RMAN-1", "全备份大小", "MB",
				 * "OracleRman备份全备份大小", MonitorItemType.NUMBER), new
				 * MonitorItem("ORACLE-RMAN-2", "增量备份大小", "MB",
				 * "OracleRman备份增量备份大小", MonitorItemType.NUMBER),
				 */
				new MonitorItem("ORACLE-SGA","ORACLE-SGA-1", "高速缓冲区大小", "MB", "SGA高速缓冲区大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-SGA","ORACLE-SGA-2", "重做日志缓冲区大小", "MB", "SGA重做日志缓冲区大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-SGA","ORACLE-SGA-3", "共享池大小", "MB", "SGA共享池大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-SGA","ORACLE-SGA-4", "数据字典缓存大小", "MB", "SGA数据字典缓存大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-SGA","ORACLE-SGA-5", "共享库缓存大小", "MB", "SGA共享库缓存大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-SGA","ORACLE-SGA-6", "SQL缓存大小", "MB", "SGA SQL缓存大小", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-SGA","ORACLE-SGA-7", "命中率", "%", "SGA 命中率", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-UNDOSTAT","ORACLE-UNDOSTAT-1", "无空间计数", "次", "无空间计数", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-UNDOSTAT","ORACLE-UNDOSTAT-2", "快照太旧错误计数", "次", "快照太旧错误计数", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-WORKQUE","ORACLE-WORKQUE-1", "破损作业数量", "个", "破损作业数量", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-WORKQUE","ORACLE-WORKQUE-2", "失败作业数量", "个", "失败作业数量", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-WORKQUE","ORACLE-WORKQUE-3", "过期作业数量", "个", "过期作业数量", MonitorItemType.NUMBER),
				// OracleDiskASMMonitor.ORACLE_ASM_TOTAL_SIZE,
				// OracleDiskASMMonitor.ORACLE_ASM_AVAILABLE_SIZE,
				// OracleDiskASMMonitor.ORACLE_ASM_USERATE,
				new MonitorItem("ORACLE-ASM", "oracle_asm_useRate", "空间使用率", "%", "空间使用率", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-ASM", "oracle_asm_availableSize", "可用空间", "MB", "可用空间", MonitorItemType.NUMBER),
				new MonitorItem("ORACLE-ASM","oracle_asm_totalSize", "总空间", "MB", "总空间", MonitorItemType.NUMBER),

				new MonitorItem("ORACLE-RAC", "ORACLE-RAC-1", "RAC状态", "", "RAC状态", MonitorItemType.TEXT),
				new MonitorItem("ORACLE-RAC", "ORACLE-RAC-2", "RAC主机名称", "", "RAC状态", MonitorItemType.TEXT), };

		/*
		 * ORACLE_ASM_USERATE new MonitorItem("ORACLE-SQLINFO-1", "用户", "",
		 * "用户", MonitorItemType.TEXT), new MonitorItem("ORACLE-SQLINFO-2",
		 * "SQL语句", "", "SQL语句", MonitorItemType.TEXT), new
		 * MonitorItem("ORACLE-SQLINFO-3", "执行时间", "ms", "执行时间",
		 * MonitorItemType.NUMBER), new MonitorItem("ORACLE-SQLINFO-4", "使用内存",
		 * "KB", "使用内存", MonitorItemType.NUMBER),
		 * 
		 * new MonitorItem("ORACLE-TABLESTATE-2", "数据大小", "MB", "该表的数据大小",
		 * MonitorItemType.NUMBER), new MonitorItem("ORACLE-TABLESTATE-3",
		 * "索引大小", "MB", "该表的索引大小", MonitorItemType.NUMBER), };
		 */

	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {
				new MonitorMethodType(OracleMethod.TYPE_ID, "Oracle JDBC 监测协议", OracleMethodConfiger.class),
				new MonitorMethodType(OracleRACMethod.TYPE_ID, "OracleRAC JDBC 监测协议", OracleRACMethodConfiger.class), };
	}

}
