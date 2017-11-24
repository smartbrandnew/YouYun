package com.broada.carrier.monitor.impl.ew.exchange;

import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.ew.exchange.basic.ExchangeBasicMonitor;
import com.broada.carrier.monitor.impl.ew.exchange.database.ExchangeDatabaseMonitor;
import com.broada.carrier.monitor.impl.ew.exchange.database.ExchangeTransportDatabaseMonitor;
import com.broada.carrier.monitor.impl.ew.exchange.ldap.ExchangeLdapMonitor;
import com.broada.carrier.monitor.impl.ew.exchange.mailbox.ExchangeMailBoxMonitor;
import com.broada.carrier.monitor.impl.ew.exchange.pop3.ExchangePop3Monitor;
import com.broada.carrier.monitor.impl.ew.exchange.queue.ExchangeQueueMonitor;
import com.broada.carrier.monitor.impl.ew.exchange.smtp.ExchangeSmtpReceiveMonitor;
import com.broada.carrier.monitor.impl.ew.exchange.smtp.ExchangeSmtpSendMonitor;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class ExchangeMonitorPackage implements MonitorPackage {
	@Override
	public MonitorType[] getTypes() {
		int index = 1;

		String[] targetTypeIds = new String[] { "Exchange" };
		String[] methodTypeIds = new String[] { CLIMonitorMethodOption.TYPE_ID };

		return new MonitorType[] {
				new MonitorType("EXCHANGE","EXCHANGE", "Exchange2007基本性能监测", "Exchange2007服务的常见性能和告警事件监测",
						SingleInstanceConfiger.class.getName(), ExchangeBasicMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("EXCHANGE","EXCHANGE-MAILBOX", "Exchange2007邮箱性能监测", "Exchange2007邮箱的常见性能和告警事件监测",
						SingleInstanceConfiger.class.getName(), ExchangeMailBoxMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("EXCHANGE","EXCHANGE-LDAP", "Exchange2007的LDAP性能监测", "Exchange2007的LDAP常见性能和告警事件监测",
						SingleInstanceConfiger.class.getName(), ExchangeLdapMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),
				new MonitorType("EXCHANGE","EXCHANGE-DATABASE", "Exchange2007的数据库性能监测", "Exchange2007的数据库常见性能和告警事件监测",
						SingleInstanceConfiger.class.getName(), ExchangeDatabaseMonitor.class.getName(), index++, targetTypeIds,
						methodTypeIds),

				new MonitorType("EXCHANGE","EXCHANGE2010-TRANSPORTDATABASE", "Exchange2010的数据库性能监测(EXCHANGE2013)", "Exchange2010的数据库常见性能和告警事件监测",
						SingleInstanceConfiger.class.getName(), ExchangeTransportDatabaseMonitor.class.getName(), index++,
						targetTypeIds,
								methodTypeIds),
				new MonitorType("EXCHANGE","EXCHANGE2010-POP3", "Exchange2010的POP3性能监测(EXCHANGE2013)", "Exchange2010的POP3性能和告警事件监测",
						SingleInstanceConfiger.class.getName(), ExchangePop3Monitor.class.getName(), index++, targetTypeIds,
								methodTypeIds),
				new MonitorType("EXCHANGE","EXCHANGE2010-QUEUE", "Exchange2010的邮箱性能监测(EXCHANGE2013)", "Exchange2010的邮箱性能和告警事件监测",
						SingleInstanceConfiger.class.getName(), ExchangeQueueMonitor.class.getName(), index++, targetTypeIds,
								methodTypeIds),
				new MonitorType("EXCHANGE","EXCHANGE2010-SMTPRECEIVED", "Exchange2010的SmtpReceived性能监测(EXCHANGE2013)",
						"Exchange2010的SmtpReceived性能和告警事件监测", SingleInstanceConfiger.class.getName(),
						ExchangeSmtpReceiveMonitor.class.getName(), index++, targetTypeIds,
								methodTypeIds),
				new MonitorType("EXCHANGE","EXCHANGE2010-SMTPSENT", "Exchange2010的SmtpSent性能监测(EXCHANGE2013)", "Exchange2010的SmtpSent性能和告警事件监测",
						SingleInstanceConfiger.class.getName(), ExchangeSmtpSendMonitor.class.getName(), index++, targetTypeIds,
								methodTypeIds), };

	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("EXCHANGE",ExchangeBasicMonitor.ITEM_ACTIVE_CONNECTION_COUNT, "活动连接数", "个", "当前活动连接数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE",ExchangeBasicMonitor.ITEM_ACTIVE_USER_COUNT, "活动用户数", "个", "当前活动用户数", MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE",ExchangeBasicMonitor.ITEM_CLIENT_RPCS_SUCCEEDED_PS, "RPC成功请求速率", "rps", "每秒执行的成功RPC数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE",ExchangeBasicMonitor.ITEM_CLIENT_RPCS_FAILED_PS, "RPC失败请求速率", "rps", "每秒执行的失败RPC数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE",ExchangeBasicMonitor.ITEM_MEM_CURRENT_MB_ALLOCATED, "已分配内存", "MB", "已分配的内存大小",
						MonitorItemType.NUMBER),

				new MonitorItem("EXCHANGE-MAILBOX",ExchangeMailBoxMonitor.ITEM_MESSAGES_DELIVERED_PS, "传送速率", "rps", "每秒递送的邮件数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE-MAILBOX",ExchangeMailBoxMonitor.ITEM_MESSAGES_SENT_PS, "发送速率", "rps", "每秒发送的邮件数", MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE-MAILBOX",ExchangeMailBoxMonitor.ITEM_MESSAGES_SUBMITTED_PS, "提交速率", "rps", "每秒提交的邮件数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE-MAILBOX",ExchangeMailBoxMonitor.ITEM_RECEIVE_SIZE, "接收队列长度", "个", "当前队列长度", MonitorItemType.NUMBER),

				new MonitorItem("EXCHANGE-LDAP", ExchangeLdapMonitor.ITEM_ACTIVE_RPC_THREADS, "RPC活动线程数", "个", "当前LDAP的RPC活动线程数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE-LDAP", ExchangeLdapMonitor.ITEM_LDAP_RESULTS_PS, "结果产生速率", "rps", "每秒产生的结果数", MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE-LDAP", ExchangeLdapMonitor.ITEM_LDAP_SEARCH_CALLS_PS, "查询执行速率", "rps", "每秒执行的查询请求数",
						MonitorItemType.NUMBER),

				new MonitorItem("EXCHANGE-DATABASE", ExchangeDatabaseMonitor.ITEM_DATABASE_CACHE_SIZE, "数据库缓存大小", "MB", "当前数据库缓存大小",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE-DATABASE", ExchangeDatabaseMonitor.ITEM_IO_DATABASE_READS_PS, "数据库IO读取速率", "rps", "每秒读取的数据量",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE-DATABASE", ExchangeDatabaseMonitor.ITEM_IO_DATABASE_WRITES_PS, "数据库IO写入速率", "rps", "每秒写入的数据量",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE-DATABASE", ExchangeDatabaseMonitor.ITEM_IO_LOG_READS_PS, "日志IO读取速率", "rps", "每秒读取的日志量",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE-DATABASE", ExchangeDatabaseMonitor.ITEM_IO_LOG_WRITES_PS, "日志IO写入速率", "rps", "每秒写入的日志量",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE-DATABASE", ExchangeDatabaseMonitor.ITEM_IO_BYTES_WRITES_PS, "字节数IO写入速率", "B_S", "每秒读取的字节数大小",
						MonitorItemType.NUMBER),

				new MonitorItem("EXCHANGE2010-TRANSPORTDATABASE",ExchangeTransportDatabaseMonitor.ITEM_STREAM_BYTES_READ_PS, "IO流读取字节速率", "B/S", "每秒读取的字节大小",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-TRANSPORTDATABASE",ExchangeTransportDatabaseMonitor.ITEM_STREAM_BYTES_READ_TOTAL, "IO流读取字节总大小", "MB", "读取的字节总大小",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-TRANSPORTDATABASE",ExchangeTransportDatabaseMonitor.ITEM_STREAM_BYTES_WRITTEN_PS, "IO流写入字节速率", "B/S", "每秒写入的字节大小",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-TRANSPORTDATABASE",ExchangeTransportDatabaseMonitor.ITEM_STREAM_BYTES_WRITTEN_TOTAL, "IO流写入字节的总大小", "MB",
						"写入的字节总大小", MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-TRANSPORTDATABASE",ExchangeTransportDatabaseMonitor.ITEM_STREAM_READ_PS, "IO流读取速率", "rps", "每秒读取的数量",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-TRANSPORTDATABASE",ExchangeTransportDatabaseMonitor.ITEM_STREAM_READ_TOTAL, "IO流读取总量", "个", "读取的总数量",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-TRANSPORTDATABASE",ExchangeTransportDatabaseMonitor.ITEM_STREAM_WRITES_PS, "IO流写入速率", "rps", "每秒写入数量",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-TRANSPORTDATABASE",ExchangeTransportDatabaseMonitor.ITEM_STREAM_WRITES_TOTAL, "IO流写入总量", "个", "写入的总数量",
						MonitorItemType.NUMBER),

				new MonitorItem("EXCHANGE2010-POP3",ExchangePop3Monitor.ITEM_CONNECTIONS_CURRENT, "当前连接数", "个", "当前连接数", MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-POP3",ExchangePop3Monitor.ITEM_CONNECTIONS_FAILED, "失败连接数", "个", "失败连接数", MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-POP3",ExchangePop3Monitor.ITEM_CONNECTIONS_REJECTED, "被拒绝的连接数", "个", "被拒绝的连接数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-POP3",ExchangePop3Monitor.ITEM_CONNECTIONS_TOTAL, "总连接数", "个", "总连接数", MonitorItemType.NUMBER),

				new MonitorItem("EXCHANGE2010-QUEUE", ExchangeQueueMonitor.ITEM_ACTIVE_MAILBOX_DELIVERY_QUEUE_LENGTH, "活跃的传送队列长度", "个", "活跃的传送队列长度",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-QUEUE", ExchangeQueueMonitor.ITEM_MESSAGES_QUEUED_FOR_DELIVERY_PS, "每秒待传送邮件数量", "rps", "每秒队列中待传送邮件的数量",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-QUEUE", ExchangeQueueMonitor.ITEM_MESSAGES_QUEUED_FOR_DELIVERY_TOTAL, "传送邮件队列总长度", "个", "队列中待传送邮件的总数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-QUEUE", ExchangeQueueMonitor.ITEM_MESSAGES_SUBMITTED_PS, "提交邮件速率", "rps", "每秒提交邮件数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-QUEUE", ExchangeQueueMonitor.ITEM_MESSAGES_SUBMITTED_TOTAL, "提交邮件总数", "个", "提交邮件的总数量",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-QUEUE", ExchangeQueueMonitor.ITEM_SUBMISSION_QUEUE_LENGTH, "提交邮件队列长度", "个", "提交邮件队列的长度",
						MonitorItemType.NUMBER),

				new MonitorItem("EXCHANGE2010-SMTPRECEIVED",ExchangeSmtpReceiveMonitor.ITEM_BYTES_RECEIVED_PS, "接受字节速率", "B/S", "每秒接受的字节数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-SMTPRECEIVED",ExchangeSmtpReceiveMonitor.ITEM_BYTES_RECEIVED_TOTAL, "接受字节总数", "MB", "接受的总字节数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-SMTPRECEIVED",ExchangeSmtpReceiveMonitor.ITEM_MESSAGE_BYTES_RECEIVED_PS, "接受邮件字节速率", "B/S", "每秒接受的邮件字节数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-SMTPRECEIVED",ExchangeSmtpReceiveMonitor.ITEM_MESSAGE_BYTES_RECEIVED_TOTAL, "接受邮件字节总数", "MB", "接受邮件的总字节数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-SMTPRECEIVED",ExchangeSmtpReceiveMonitor.ITEM_MESSAGES_RECEIVED_PS, "接受邮件速率", "rps", "每秒接受的邮件数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-SMTPRECEIVED", ExchangeSmtpReceiveMonitor.ITEM_MESSAGES_RECEIVED_TOTAL, "接受邮件总数", "个", "接受邮件的总数量",
						MonitorItemType.NUMBER),

				new MonitorItem("EXCHANGE2010-SMTPRECEIVED", ExchangeSmtpSendMonitor.ITEM_BYTES_SENT_PS, "发送字节速率", "rps", "每秒发送的字节数", MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-SMTPRECEIVED", ExchangeSmtpSendMonitor.ITEM_BYTES_SENT_TOTAL, "发送字节总数", "MB", "发送的总字节数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-SMTPSENT", ExchangeSmtpSendMonitor.ITEM_MESSAGE_BYTES_SENT_PS, "发送邮件字节速率", "B/S", "每秒发送的邮件字节数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-SMTPSENT", ExchangeSmtpSendMonitor.ITEM_MESSAGE_BYTES_SENT_TOTAL, "发送邮件字节总数", "MB", "发送邮件的总字节数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-SMTPSENT", ExchangeSmtpSendMonitor.ITEM_MESSAGES_SENT_PS, "发送邮件速率", "rps", "发送接受的邮件数",
						MonitorItemType.NUMBER),
				new MonitorItem("EXCHANGE2010-SMTPSENT", ExchangeSmtpSendMonitor.ITEM_MESSAGES_SENT_TOTAL, "发送邮件总数", "个", "发送邮件的总数量",
						MonitorItemType.NUMBER), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return null;
	}
}
