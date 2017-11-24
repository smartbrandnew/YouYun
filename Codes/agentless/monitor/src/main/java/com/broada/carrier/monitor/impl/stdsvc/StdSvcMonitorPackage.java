package com.broada.carrier.monitor.impl.stdsvc;

import com.broada.carrier.monitor.impl.stdsvc.dns.DNSMonitor;
import com.broada.carrier.monitor.impl.stdsvc.dns.DNSParamConfiger;
import com.broada.carrier.monitor.impl.stdsvc.ftp.FTPMonitor;
import com.broada.carrier.monitor.impl.stdsvc.ftp.FTPParamConfiger;
import com.broada.carrier.monitor.impl.stdsvc.http.HTTPMonitor;
import com.broada.carrier.monitor.impl.stdsvc.http.HTTPParamConfiger;
import com.broada.carrier.monitor.impl.stdsvc.https.HTTPSMonitor;
import com.broada.carrier.monitor.impl.stdsvc.https.HTTPSParamConfiger;
import com.broada.carrier.monitor.impl.stdsvc.pop3.POP3Monitor;
import com.broada.carrier.monitor.impl.stdsvc.pop3.POP3ParamConfiger;
import com.broada.carrier.monitor.impl.stdsvc.smtp.SMTPMonitor;
import com.broada.carrier.monitor.impl.stdsvc.smtp.SMTPParamConfiger;
import com.broada.carrier.monitor.impl.stdsvc.tcp.TCPMonitor;
import com.broada.carrier.monitor.impl.stdsvc.tcp.TCPParamConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class StdSvcMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		int index = 1;

		return new MonitorType[] {
				// 暂用
				new MonitorType("SERVICE_AVALIABLE", "DNS", "DNS服务 [可用性]", "监测DNS服务运行是否正常。",
						DNSParamConfiger.class.getName(), DNSMonitor.class.getName(), index++,
						new String[] { "DNSService" }, null),

				new MonitorType("SERVICE_AVALIABLE", "FTP", "FTP服务 [可用性]", "使用FTP协议监测FTP服务器状态是否正常。",
						FTPParamConfiger.class.getName(), FTPMonitor.class.getName(), index++,
						new String[] { "FTPService" }, null),

				new MonitorType("SERVICE_AVALIABLE", "HTTP", "HTTP服务 [可用性]", "使用HTTP协议监测指定的URL来判断Web服务器运行状态是否正常。",
						HTTPParamConfiger.class.getName(), HTTPMonitor.class.getName(), index++,
						new String[] { "HTTPService" }, null),

				new MonitorType("SERVICE_AVALIABLE", "HTTPS", "HTTPS服务 [可用性]", "使用HTTPS协议监测指定的URL来判断Web服务器运行状态是否正常。",
						HTTPSParamConfiger.class.getName(), HTTPSMonitor.class.getName(), index++,
						new String[] { "HTTPService" }, null),

				new MonitorType("SERVICE_AVALIABLE", "POP3", "POP3服务 [可用性]", "使用POP3协议监测POP3服务器的工作状态是否正常。",
						POP3ParamConfiger.class.getName(), POP3Monitor.class.getName(), index++,
						new String[] { "MailService" }, null),

				new MonitorType("SERVICE_AVALIABLE", "SMTP", "SMTP服务", "使用SMTP协议监测邮件发送服务器的工作状态是否正常。",
						SMTPParamConfiger.class.getName(), SMTPMonitor.class.getName(), index++,
						new String[] { "MailService" }, null),

				new MonitorType("SERVICE_AVALIABLE", "TCP", "TCP端口监听", "监测某个TCP端口是否在监听。",
						TCPParamConfiger.class.getName(), TCPMonitor.class.getName(), index++, new String[] { "OS",
								"NetDev", "SecDev" }, null), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] { new MonitorItem("DNS","DNS-1", "响应时间", "毫秒", "DNS服务响应时间", MonitorItemType.NUMBER),

		new MonitorItem( "FTP","FTP-1", "响应时间", "毫秒", "FTP服务响应时间", MonitorItemType.NUMBER),

		new MonitorItem("HTTP","HTTP-1", "响应时间", "秒", "HTTP服务响应时间", MonitorItemType.NUMBER),

		new MonitorItem("HTTPS","HTTPS-1", "响应时间", "秒", "HTTPS服务响应时间", MonitorItemType.NUMBER),

		new MonitorItem("POP3","POP3-1", "响应时间", "秒", "POP3服务响应时间", MonitorItemType.NUMBER),

		new MonitorItem("SMTP", "SMTP-1", "响应时间", "秒", "SMTP服务响应时间", MonitorItemType.NUMBER),

		new MonitorItem("TCP","TCP-1", "端口状态", "", "TCP端口的开启情况", MonitorItemType.NUMBER),
				new MonitorItem("TCP","TCP-2", "响应时间", "毫秒", "与端口建立连接所耗费的时间", MonitorItemType.NUMBER), };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return null;
	}
}
