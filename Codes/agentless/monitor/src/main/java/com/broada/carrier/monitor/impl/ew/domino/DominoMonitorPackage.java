package com.broada.carrier.monitor.impl.ew.domino;

import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.ew.domino.basic.DominoBasicConfiger;
import com.broada.carrier.monitor.impl.ew.domino.basic.DominoBasicMonitor;
import com.broada.carrier.monitor.impl.ew.domino.basic46.Domino46BasicMonitor;
import com.broada.carrier.monitor.impl.ew.domino.perf.DominoPerfMonitor;
import com.broada.carrier.monitor.method.domino.DominoMonitorMethodOption;
import com.broada.carrier.monitor.method.domino.DominoParamPanel;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class DominoMonitorPackage implements MonitorPackage {

	String[] targetTypeIds = new String[] { "Domino" };
	String[] methodTypeIds = new String[] { DominoMonitorMethodOption.TYPE_ID };

	@Override
	public MonitorType[] getTypes() {
		int index = 1;

		return new MonitorType[] {
				new MonitorType("DOMINO", "DOMINO_BASIC", "Domino常用性能和告警事件监测 [可用性]", "Domino服务器的常见性能和告警事件监测",
						DominoBasicConfiger.class.getName(), DominoBasicMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds),
				new MonitorType("DOMINO", "DOMINO46_BASIC", "Domino常用性能和告警事件监测", "Domino服务器的常见性能和告警事件监测",
						DominoBasicConfiger.class.getName(), Domino46BasicMonitor.class.getName(), index++,
						targetTypeIds, null),
				new MonitorType("DOMINO", "DOMINO_PERF", "Domino性能负载监测", "Domino服务器的各类性能和负载指标监测",
						SingleInstanceConfiger.class.getName(), DominoPerfMonitor.class.getName(), index++,
						targetTypeIds, methodTypeIds) };

	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("DOMINO_BASIC","DOMINO_BASIC-1", "内存使用", "MB", "服务器内存使用数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_BASIC","DOMINO_BASIC-2", "数据库空间占用率", "%", "指定的数据库的使用空间占用率", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_BASIC","DOMINO_BASIC-3", "代发送邮件数目", "个", "Domino邮件服务所有代发送邮件数目", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_BASIC","DOMINO_BASIC-4", "僵死邮件数目", "个", "Domino邮件服务所有僵死邮件数目", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO46_BASIC","DOMINO46_BASIC-1", "内存使用", "MB", "服务器内存使用数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO46_BASIC","DOMINO46_BASIC-2", "数据库空间占用率", "%", "指定的数据库的使用空间占用率", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO46_BASIC","DOMINO46_BASIC-3", "代发送邮件数目", "个", "Domino邮件服务所有代发送邮件数目", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO46_BASIC","DOMINO46_BASIC-4", "僵死邮件数目", "个", "Domino邮件服务所有僵死邮件数目", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF", "DOMINO_PERF-1", "版本", "", "Domino服务器的版本信息", MonitorItemType.TEXT),
				new MonitorItem("DOMINO_PERF", "DOMINO_PERF-2", "文件路径", "", "Domino服务器的数据库文件路径", MonitorItemType.TEXT),
				new MonitorItem("DOMINO_PERF", "DOMINO_PERF-3", "服务器CPU个数", "个", "Domino服务器的硬件的CPU个数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF", "DOMINO_PERF-4", "服务器CPU类型", "", "Domino服务器的硬件的CPU类型", MonitorItemType.TEXT),
				new MonitorItem("DOMINO_PERF", "DOMINO_PERF-10", "每分钟交易数", "起", "Domino服务器每分钟交易数量", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF", "DOMINO_PERF-11", "每分钟最大交易数", "起", "Domino服务器自启动以来，每分钟交易数量最大值", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF", "DOMINO_PERF-12", "交易总数", "起", "Domino服务器自启动以来，交易总数量", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-13", "当前用户数", "个", "Domino服务器当前登录用户总数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-14", "最大用户数", "个", "Domino服务器自启动以来，最大的并发用户数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-15", "当前任务数", "个", "Domino服务器当前工作的任务数量", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-20", "成功复制次数", "次", "Domino服务器之间复制数据库的成功次数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-21", "失败复制次数", "次", "Domino服务器之间复制数据库的失败次数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-22", "删除文档总数", "个", "Domino服务器自启动以来，删除文档数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-23", "增加文档总数", "个", "Domino服务器自启动以来，增加文档数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-24", "修改文档总数", "个", "Domino服务器自启动以来，修改文档数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-30", "死邮件数", "个", "Domino服务器死邮件个数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-31", "路由邮件数", "个", "Domino服务器已经路由的邮件个数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-32", "待路由邮件数", "个", "Domino服务器等待路由的邮件个数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-33", "已投递邮件数", "个", "Domino服务器已经投递的邮件个数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-34", "待投递邮件数", "个", "Domino服务器等待投递的邮件个数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-35", "平均邮件大小", "KB", "Domino服务器发送的邮件平均大小", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-36", "最大邮件大小", "KB", "Domino服务器发送的邮件最大大小", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-40", "缓冲池大小", "byte", "Domino服务器缓冲池大小", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-41", "缓冲池使用值", "byte", "Domino服务器缓冲池当前使用值", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-42", "缓冲池最大值", "byte", "Domino服务器缓冲池最大使用值", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-43", "扩展管理池使用值", "byte", "Domino服务器扩展管理池当前使用值", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-44", "扩展管理池最大值", "byte", "Domino服务器扩展管理池最大使用值", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-45", "NSF使用值", "byte", "Domino NSF数据文件缓冲池使用值", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-46", "NSF最大值", "byte", "Domino NSF数据文件缓冲池最大值", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-50", "MAIL传递速率", "秒/次", "訊息的平均递送時間", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-51", "MAIL路由速率", "次/秒", "已递送讯息的平均伺服器跳跃點数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-52", "死信率", "%", "无法递送的讯息数在「路由器」接收的讯息数里的比率", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-53", "总的处理数", "个", "伺服器启动后处理事务的总数", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-54", "空闲空间", "位元組", "磁碟机中的可用磁碟空间", MonitorItemType.NUMBER),
				new MonitorItem("DOMINO_PERF","DOMINO_PERF-55", "剩余率", "%", "磁碟机中的可用磁碟空间在总计大小中的比率", MonitorItemType.NUMBER), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(DominoMonitorMethodOption.TYPE_ID, "Domino 监测协议",
				DominoParamPanel.class), };
	}
}
