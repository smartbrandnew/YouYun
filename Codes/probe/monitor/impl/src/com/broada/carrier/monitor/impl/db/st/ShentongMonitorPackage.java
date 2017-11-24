package com.broada.carrier.monitor.impl.db.st;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.db.st.basic.ShentongBaseMonitor;
import com.broada.carrier.monitor.impl.db.st.buffer.ShentongBufferMonitor;
import com.broada.carrier.monitor.impl.db.st.checkpoint.ShentongCheckpointMonitor;
import com.broada.carrier.monitor.impl.db.st.fts.ShentongFTSMonitor;
import com.broada.carrier.monitor.impl.db.st.patchRate.ShentongPatchRateMonitor;
import com.broada.carrier.monitor.impl.db.st.process.ShentongProcessMonitor;
import com.broada.carrier.monitor.impl.db.st.ratio.ShentongRatioMonitor;
import com.broada.carrier.monitor.impl.db.st.workQueue.ShentongWorkQueueMonitor;
import com.broada.carrier.monitor.method.st.ShentongMethod;
import com.broada.carrier.monitor.method.st.ShentongParamPanel;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class ShentongMonitorPackage implements MonitorPackage {
	String[] targetTypeIds = new String[] { "Shentong" };
	String[] methodTypeIds = new String[] { ShentongMethod.TYPE_ID };
	int index = 1;

	@Override
	public MonitorType[] getTypes() {
		return new MonitorType[] {
				new MonitorType("SHENTONG", "SHENTONG-BASE", "SHENTONG基本信息采集", "采集Shentong数据库的基本配置信息和数据库的名称、版本信息等。",
						SingleInstanceConfiger.class.getName(), ShentongBaseMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType(
						"SHENTONG",
						"SHENTONG-CHECKPOINT",
						"SHENTONG检查点监测",
						"监控数据库写程序（DBWR）检查点完成的次数，以及服务器请求数据库写程序检查点数。注意:请将监测项“历史发生监测点”和“历史完成检查点”勾选上，否则后面两项监测项将不准确。建议监测周期为一个小时。",
						SingleInstanceConfiger.class.getName(), ShentongCheckpointMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				// new MonitorType("SHENTONG-DATAFILE", "SHENTONG数据文件监测",
				// "监测SHENTONG数据文件的大小，在线状态，读写次数/时间等信息，可以设定告警条件。",
				// MultiInstanceConfiger.class.getName(),
				// ShentongDtfileMonitor.class.getName(), index++,
				// targetTypeIds, methodTypeIds),

				new MonitorType("SHENTONG", "SHENTONG-FTS", "SHENTONG全表扫描配置", "获取SHENTONG数据库全表扫描相关配置信息,并可设定告警条件。",
						SingleInstanceConfiger.class.getName(), ShentongFTSMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("SHENTONG", "SHENTONG-PATCHRATE", "SHENTONG碎片监测", "监测SHENTONG数据库的碎片使用情况",
						MultiInstanceConfiger.class.getName(), ShentongPatchRateMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("SHENTONG", "SHENTONG-PROCESS", "SHENTONG进程资源消耗监测",
						"SHENTONG进程资源消耗信息监测,并可设定告警条件。当比较符为\" - \"时不做比较。", MultiInstanceConfiger.class.getName(),
						ShentongProcessMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("SHENTONG", "SHENTONG-RATIO", "SHENTONG命中率监测", "监测SHENTONG数据库命中率情况",
						SingleInstanceConfiger.class.getName(), ShentongRatioMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("SHENTONG", "SHENTONG-WORKQUE", "SHENTONG作业队列监测", "SHENTONG作业队列监测中破损\\失败\\过期作业的数量。",
						SingleInstanceConfiger.class.getName(), ShentongWorkQueueMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

				new MonitorType("SHENTONG", "SHENTONG-BUFFER", "SHENTONG缓存区监测", "SHENTONG作缓存区监测。",
						MultiInstanceConfiger.class.getName(), ShentongBufferMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),

		// new MonitorType("SHENTONG-SESS", "SHENTONG会话监测", "SHENTONG会话监测。",
		// MultiInstanceConfiger.class.getName(),
		// ShentongSessInfoMonitor.class.getName(), index++, targetTypeIds,
		// methodTypeIds),

		};

	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("SHENTONG-BASE","SHENTONG-BASE-1", "实例状态", "", "数据库实例状态", MonitorItemType.TEXT),
				new MonitorItem("SHENTONG-BASE","SHENTONG-BASE-2", "主机名", "", "数据库所在服务器的主机名", MonitorItemType.TEXT),
				new MonitorItem("SHENTONG-BASE","SHENTONG-BASE-3", "DB名称", "", "数据库名称", MonitorItemType.TEXT),
				new MonitorItem("SHENTONG-BASE","SHENTONG-BASE-4", "DB版本", "", "数据库版本", MonitorItemType.TEXT),
				new MonitorItem("SHENTONG-BASE","SHENTONG-BASE-5", "例程名", "", "数据库实例名称", MonitorItemType.TEXT),
				new MonitorItem("SHENTONG-BASE","SHENTONG-BASE-6", "例程开始时间", "", "数据库实例启动时间", MonitorItemType.TEXT),
				new MonitorItem("SHENTONG-BASE","SHENTONG-BASE-7", "归档模式", "", "数据库归档日志模式", MonitorItemType.TEXT),
				new MonitorItem("SHENTONG-BASE","SHENTONG-BASE-8", "归档路径", "", "数据库归档路径", MonitorItemType.TEXT),
				new MonitorItem("SHENTONG-BASE","SHENTONG-BASE-9", "当前连接数", "", "当前连接数", MonitorItemType.NUMBER),

				new MonitorItem("SHENTONG-CHECKPOINT","SHENTONG-CHECKPOINT-1", "发生检查点数", "次", "发生检查点数", MonitorItemType.NUMBER),
				new MonitorItem("SHENTONG-CHECKPOINT","SHENTONG-CHECKPOINT-2", "完成检查点数", "次", "完成检查点数", MonitorItemType.NUMBER),

				// new MonitorItem("SHENTONG-DATAFILE-1", "读次数", "次",
				// "指定数据文件的读次数", MonitorItemType.NUMBER),
				// new MonitorItem("SHENTONG-DATAFILE-2", "写次数", "次",
				// "指定数据文件的写次数", MonitorItemType.NUMBER),
				// new MonitorItem("SHENTONG-DATAFILE-3", "读时间", "秒",
				// "指定数据文件的读时间", MonitorItemType.NUMBER),
				// new MonitorItem("SHENTONG-DATAFILE-4", "写时间", "秒",
				// "指定数据文件的写时间", MonitorItemType.NUMBER),
				// new MonitorItem("SHENTONG-DATAFILE-5", "文件大小", "MB",
				// "指定数据文件当前大小", MonitorItemType.NUMBER),
				// new MonitorItem("SHENTONG-DATAFILE-6", "写文件块数", "",
				// "指定数据文件写块数", MonitorItemType.NUMBER),
				// new MonitorItem("SHENTONG-DATAFILE-7", "读文件块数", "",
				// "指定数据文件读块数", MonitorItemType.NUMBER),
				// new MonitorItem("SHENTONG-DATAFILE-8", "读写文件块数", "",
				// "指定数据文件读写块数", MonitorItemType.NUMBER),

				new MonitorItem("SHENTONG-FTS","SHENTONG-FTS-1", "RSRATIO值", "%", "Shentong行源百分比值", MonitorItemType.NUMBER),

				new MonitorItem("SHENTONG-PATCHRATE","SHENTONG-PATCHRATE-1", "FSFI值", "%", "SHENTONG自由空间碎片索引比值", MonitorItemType.NUMBER),

				new MonitorItem("SHENTONG-PROCESS","SHENTONG-PROCESS-1", "已分配PGA", "M", "已分配程序全局区", MonitorItemType.NUMBER),
				new MonitorItem("SHENTONG-PROCESS","SHENTONG-PROCESS-2", "已使用PGA", "M", "已使用程序全局区", MonitorItemType.NUMBER),
				new MonitorItem("SHENTONG-PROCESS","SHENTONG-PROCESS-3", "可用PGA百分比", "%", "可用程序全局区百分比", MonitorItemType.NUMBER),
				new MonitorItem("SHENTONG-PROCESS","SHENTONG-PROCESS-4", "可用PGA", "M", "可用程序全局区", MonitorItemType.NUMBER),

				new MonitorItem("SHENTONG-RATIO","SHENTONG-RATIO-1", "高速缓存区命中率", "%", "SHENTONG高速缓存区命中率", MonitorItemType.NUMBER),
				new MonitorItem("SHENTONG-RATIO","SHENTONG-RATIO-2", "磁盘排序与内存排序比率", "%", "SHENTONG磁盘排序与内存排序比率", MonitorItemType.NUMBER),

				new MonitorItem("SHENTONG-WORKQUE","SHENTONG-WORKQUE-1", "破损作业数量", "个", "破损作业数量", MonitorItemType.NUMBER),
				new MonitorItem("SHENTONG-WORKQUE","SHENTONG-WORKQUE-2", "失败作业数量", "个", "失败作业数量", MonitorItemType.NUMBER),
				new MonitorItem("SHENTONG-WORKQUE","SHENTONG-WORKQUE-3", "过期作业数量", "个", "过期作业数量", MonitorItemType.NUMBER),

				new MonitorItem("SHENTONG-BUFFER","SHENTONG-BUFFER-1", "页大小", "KB", "页大小", MonitorItemType.NUMBER),
				new MonitorItem("SHENTONG-BUFFER","SHENTONG-BUFFER-2", "空闲页数", "页", "空闲页数", MonitorItemType.NUMBER),
				new MonitorItem("SHENTONG-BUFFER","SHENTONG-BUFFER-3", "脏页数", "页", "脏页数", MonitorItemType.NUMBER),
				new MonitorItem("SHENTONG-BUFFER","SHENTONG-BUFFER-4", "缓冲区读块", "页", "缓冲区读块", MonitorItemType.NUMBER),
				new MonitorItem("SHENTONG-BUFFER","SHENTONG-BUFFER-5", "缓冲区写块", "块", "缓冲区写块", MonitorItemType.NUMBER),

		// new MonitorItem("SHENTONG-SESS-1", "会话ID", "", "会话ID",
		// MonitorItemType.NUMBER),
		// new MonitorItem("SHENTONG-SESS-2", "用户名", "", "用户名",
		// MonitorItemType.NUMBER),
		// new MonitorItem("SHENTONG-SESS-3", "会话排序次数", "次", "会话排序次数",
		// MonitorItemType.NUMBER),
		// new MonitorItem("SHENTONG-SESS-4", "扫描表次数", "次", "扫描表次数",
		// MonitorItemType.NUMBER),
		// new MonitorItem("SHENTONG-SESS-5", "读次数", "次", "读次数",
		// MonitorItemType.NUMBER),
		// new MonitorItem("SHENTONG-SESS-6", "写次数", "次", "写次数",
		// MonitorItemType.NUMBER),
		// new MonitorItem("SHENTONG-SESS-7", "提交次数", "次", "提交次数",
		// MonitorItemType.NUMBER),
		};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(ShentongMethod.TYPE_ID, "Shentong JDBC 监测协议",
				ShentongParamPanel.class), };
	}

}
