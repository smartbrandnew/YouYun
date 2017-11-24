package com.broada.carrier.monitor.impl.mw.iis;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.mw.iis.baseInfo.IISBaseInfoMonitor;
import com.broada.carrier.monitor.impl.mw.iis.bytes.IISBytesMonitor;
import com.broada.carrier.monitor.impl.mw.iis.conns.IISConnsMonitor;
import com.broada.carrier.monitor.impl.mw.iis.files.IISFilesMonitor;
import com.broada.carrier.monitor.impl.mw.iis.users.IISUsersMonitor;
import com.broada.carrier.monitor.impl.mw.iis.webRequest.IISWebRequestMonitor;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class IISMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIIS = new String[] { "IIS" };
		String[] methodCLI = new String[] { CLIMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("IIS", "IIS-BASEINFO", "IIS基本信息监测 [可用性]", "监测IIS服务的基本信息。",
						MultiInstanceConfiger.class.getName(), IISBaseInfoMonitor.class.getName(), index++,
						targetTypeIIS, methodCLI),

				new MonitorType("IIS", "IIS-CONNS", "IIS连接监测", "监测IIS服务的连接信息。", MultiInstanceConfiger.class.getName(),
						IISConnsMonitor.class.getName(), index++, targetTypeIIS, methodCLI),

				new MonitorType("IIS", "IIS-TRANSFERBYTES", "IIS传输字节监测", "监测IIS服务的字节发送和接收信息。",
						MultiInstanceConfiger.class.getName(), IISBytesMonitor.class.getName(), index++, targetTypeIIS,
						methodCLI),

				new MonitorType("IIS", "IIS-TRANSFERFILES", "IIS传输文件监测", "监测IIS服务的文件发送和接收信息。",
						MultiInstanceConfiger.class.getName(), IISFilesMonitor.class.getName(), index++, targetTypeIIS,
						methodCLI),

				new MonitorType("IIS", "IIS-USERS", "IIS用户监测", "监测IIS服务的用户访问信息。",
						MultiInstanceConfiger.class.getName(), IISUsersMonitor.class.getName(), index++, targetTypeIIS,
						methodCLI),

				new MonitorType("IIS", "IIS-WEBREQUEST", "IISWEB请求监测", "监测IIS服务的WEB请求信息。",
						MultiInstanceConfiger.class.getName(), IISWebRequestMonitor.class.getName(), index++,
						targetTypeIIS, methodCLI) };

	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("IIS-BASEINFO", "IIS-BASEINFO-1", "服务运行时间", "秒", "网站服务已经运行了多长时间", MonitorItemType.NUMBER),
				new MonitorItem("IIS-BASEINFO", "IIS-CONNS-1", "当前连接数", "个", "网站当前连接的用户个数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-CONNS", "IIS-CONNS-2", "最大连接数", "个", "网站能同时连接的最大连接用户个数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-CONNS", "IIS-CONNS-3", "尝试连接/秒", "次/秒", "网站每秒里尝试连接的次数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-CONNS", "IIS-CONNS-4", "尝试登陆/秒", "次/秒", "网站每秒里尝试登陆的次数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-CONNS", "IIS-CONNS-5", "服务运行时间", "秒", "网站服务已经运行了多长时间", MonitorItemType.NUMBER),

				new MonitorItem("IIS-TRANSFERBYTES", "IIS-TRANSFERBYTES-1", "发送字节/秒", "byte/s", "网站每秒里发送的字节", MonitorItemType.NUMBER),
				new MonitorItem("IIS-TRANSFERBYTES", "IIS-TRANSFERBYTES-2", "接收字节/秒", "byte/s", "网站每秒里接收的字节", MonitorItemType.NUMBER),
				new MonitorItem("IIS-TRANSFERBYTES", "IIS-TRANSFERBYTES-3", "传输字节/秒", "byte/s", "网站每秒里传输的字节", MonitorItemType.NUMBER),
				new MonitorItem("IIS-TRANSFERBYTES", "IIS-TRANSFERBYTES-4", "服务运行时间", "秒", "网站服务已经运行了多长时间", MonitorItemType.NUMBER),

				new MonitorItem("IIS-TRANSFERFILES", "IIS-TRANSFERFILES-1", "发送文件/秒", "个/秒", "网站每秒里发送文件的个数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-TRANSFERFILES", "IIS-TRANSFERFILES-2", "接收文件/秒", "个/秒", "网站每秒里接收文件的个数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-TRANSFERFILES", "IIS-TRANSFERFILES-3", "传输文件/秒", "个/秒", "网站每秒里传输文件的个数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-TRANSFERFILES", "IIS-TRANSFERFILES-4", "服务运行时间", "秒", "网站服务已经运行了多长时间", MonitorItemType.NUMBER),
				new MonitorItem("IIS-TRANSFERFILES", "IIS-TRANSFERFILES-5", "找不到文件的错误数", "个", "服务启动后总的找不到文件的错误数", MonitorItemType.NUMBER),

				new MonitorItem("IIS-USERS", "IIS-USERS-1", "当前匿名用户数", "个", "网站当前匿名登陆的用户个数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-USERS", "IIS-USERS-2", "当前非匿名用户数", "个", "网站当前非匿名登陆用户个数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-USERS", "IIS-USERS-3", "匿名用户/秒", "个/秒", "网站每秒存在匿名用户的个数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-USERS", "IIS-USERS-4", "非匿名用户/秒", "个/秒", "网站每秒存在非匿名用户的个数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-USERS", "IIS-USERS-5", "最大匿名用户数", "个", "网站能够连接的最大匿名用户个数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-USERS", "IIS-USERS-6", "最大非匿名用户数", "个", "网站能够连接的最大非匿名用户个数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-USERS", "IIS-USERS-7", "服务运行时间", "秒", "网站服务已经运行了多长时间", MonitorItemType.NUMBER),

				new MonitorItem("IIS-WEBREQUEST", "IIS-WEBREQUEST-1", "get请求/秒", "次/秒", "网站每秒通过get请求的次数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-WEBREQUEST", "IIS-WEBREQUEST-2", "post请求/秒", "次/秒", "网站每秒通过post请求的次数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-WEBREQUEST", "IIS-WEBREQUEST-3", "head请求/秒", "次/秒", "网站每秒通过head请求的次数", MonitorItemType.NUMBER),
				new MonitorItem("IIS-WEBREQUEST", "IIS-WEBREQUEST-4", "其他请求/秒", "次/秒", "网站每秒通过除get,post,head之外请求的次数",
						MonitorItemType.NUMBER),
				new MonitorItem("IIS-WEBREQUEST", "IIS-WEBREQUEST-5", "服务运行时间", "秒", "网站服务已经运行了多长时间", MonitorItemType.NUMBER) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		// TODO Auto-generated method stub
		return null;
	}

}
