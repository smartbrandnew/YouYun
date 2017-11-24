package com.broada.carrier.monitor.impl.mw.websphere;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.mw.websphere.pmi.WASPmiMonitor;
import com.broada.carrier.monitor.method.websphere.WASMonitorMethodOption;
import com.broada.carrier.monitor.method.websphere.WASParamPanel;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class WebsphereMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		int index = 1;
		String[] targetTypeWebSphere = new String[] { "WebSphere" };
		String[] methodWebSphereAgent = new String[] { WASMonitorMethodOption.TYPE_ID };
		return new MonitorType[] {

				new MonitorType("WEBSPHERE", "WAS-CACHE-PMI", "Websphere 动态高速缓存监测 [可用性]", "监测Websphere动态高速缓存性能",
						MultiInstanceConfiger.class.getName(), WASPmiMonitor.class.getName(), index++,
						targetTypeWebSphere, methodWebSphereAgent),

				new MonitorType("WEBSPHERE", "WAS-EJB-PMI", "Websphere EJB监测", "监测Websphere EJB性能状态",
						MultiInstanceConfiger.class.getName(), WASPmiMonitor.class.getName(), index++,
						targetTypeWebSphere, methodWebSphereAgent),

				new MonitorType("WEBSPHERE", "WAS-J2C-PMI", "Websphere J2C监测", "监测Websphere J2C性能状态",
						MultiInstanceConfiger.class.getName(), WASPmiMonitor.class.getName(), index++,
						targetTypeWebSphere, methodWebSphereAgent),

				new MonitorType("WEBSPHERE", "WAS-TRANS-PMI", "Websphere 事务监测", "监测Websphere 事务性能状态",
						SingleInstanceConfiger.class.getName(), WASPmiMonitor.class.getName(), index++,
						targetTypeWebSphere, methodWebSphereAgent),

				new MonitorType("WEBSPHERE", "WASJDBC", "Websphere JDBC监测", "监测Websphere JDBC的性能状态",
						MultiInstanceConfiger.class.getName(), WASPmiMonitor.class.getName(), index++,
						targetTypeWebSphere, methodWebSphereAgent),

				new MonitorType("WEBSPHERE", "WASJVM", "Websphere JVM监测", "监测Websphere JVM的性能状态",
						SingleInstanceConfiger.class.getName(), WASPmiMonitor.class.getName(), index++,
						targetTypeWebSphere, methodWebSphereAgent),

				new MonitorType("WEBSPHERE", "WASSERVLET", "Websphere Servlet会话监测", "监测Websphere Servlet会话性能状态",
						MultiInstanceConfiger.class.getName(), WASPmiMonitor.class.getName(), index++,
						targetTypeWebSphere, methodWebSphereAgent),

				new MonitorType("WEBSPHERE", "WASTHREADPOOL", "WebSphere线程池监测", "监测WebSphere线程池使用情况。",
						MultiInstanceConfiger.class.getName(), WASPmiMonitor.class.getName(), index++,
						targetTypeWebSphere, methodWebSphereAgent),

				new MonitorType("WEBSPHERE", "WASWEBAPP", "Websphere Web应用监测", "监测Websphere Web应用的性能状态",
						MultiInstanceConfiger.class.getName(), WASPmiMonitor.class.getName(), index++,
						targetTypeWebSphere, methodWebSphereAgent) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {

				new MonitorItem("WAS-CACHE-PMI","WAS-CACHE-PMI-1", "最大高速缓存数", "个", "内存中的最大高速缓存条目数", MonitorItemType.NUMBER),
				new MonitorItem("WAS-CACHE-PMI","WAS-CACHE-PMI-2", "当前高速缓存数", "个", "内存中的当前高速缓存条目数", MonitorItemType.NUMBER),
				new MonitorItem("WAS-CACHE-PMI","WAS-CACHE-PMI-3", "高速缓存使用率", "%", "当前动态高速缓存的使用率", MonitorItemType.NUMBER),
				new MonitorItem("WAS-CACHE-PMI","WAS-CACHE-PMI-4", "高速缓存不命中率", "%", "当前动态高速缓存的不命中率", MonitorItemType.NUMBER),

				new MonitorItem("WAS-EJB-PMI", "WAS-EJB-PMI-1", "Bean方法响应时间", "毫秒", "Bean方法平均响应时间	", MonitorItemType.NUMBER),
				new MonitorItem("WAS-EJB-PMI", "WAS-EJB-PMI-2", "Bean活动时间", "毫秒", "BeanActivate调用平均时间", MonitorItemType.NUMBER),
				new MonitorItem("WAS-EJB-PMI", "WAS-EJB-PMI-3", "Bean钝化时间", "毫秒", "BeanPassivate调用平均时间", MonitorItemType.NUMBER),
				new MonitorItem("WAS-EJB-PMI", "WAS-EJB-PMI-4", "Bean创建时间", "毫秒", "Bean创建平均时间", MonitorItemType.NUMBER),
				new MonitorItem("WAS-EJB-PMI", "WAS-EJB-PMI-5", "Bean持久化时间", "毫秒", "Bean持久化平均时间", MonitorItemType.NUMBER),
				new MonitorItem("WAS-EJB-PMI", "WAS-EJB-PMI-6", "Bean装载时间", "毫秒", "从持久存储装入Bean的平均时间", MonitorItemType.NUMBER),
				new MonitorItem("WAS-EJB-PMI", "WAS-EJB-PMI-7", "Bean调用时间", "毫秒", "bean条目调用平均时间", MonitorItemType.NUMBER),

				new MonitorItem("WAS-J2C-PMI", "WAS-J2C-PMI-1", "J2C使用总时间", "秒", "正在使用的连接的使用总时间", MonitorItemType.NUMBER),
				new MonitorItem("WAS-J2C-PMI", "WAS-J2C-PMI-2", "J2C使用时间增量", "秒", "从上次监测到最后一次监测时间内J2C使用的时间", MonitorItemType.NUMBER),

				new MonitorItem("WAS-TRANS-PMI", "WAS-TRANS-PMI-1", "全局事务总数", "个", "在服务器上启动的全局事务总数", MonitorItemType.NUMBER),
				new MonitorItem("WAS-TRANS-PMI", "WAS-TRANS-PMI-2", "本地事务总数", "个", "在服务器上启动的本地事务的总数", MonitorItemType.NUMBER),
				new MonitorItem("WAS-TRANS-PMI", "WAS-TRANS-PMI-3", "全局事务响应时间", "毫秒", "全局事务的平均持续时间", MonitorItemType.NUMBER),
				new MonitorItem("WAS-TRANS-PMI", "WAS-TRANS-PMI-4", "本地事务响应时间", "毫秒", "本地事务的平均持续时间", MonitorItemType.NUMBER),
				new MonitorItem("WAS-TRANS-PMI", "WAS-TRANS-PMI-5", "超时全局事务数", "个", "超时的全局事务数", MonitorItemType.NUMBER),
				new MonitorItem("WAS-TRANS-PMI", "WAS-TRANS-PMI-6", "超时本地事务数", "个", "超时的本地事务数", MonitorItemType.NUMBER),
				new MonitorItem("WAS-TRANS-PMI", "WAS-TRANS-PMI-7", "并发活动全局事务数", "个", "并发活动全局事务数", MonitorItemType.NUMBER),
				new MonitorItem("WAS-TRANS-PMI", "WAS-TRANS-PMI-8", "并发活动本地事务数", "个", "并发活动本地事务数", MonitorItemType.NUMBER),
				new MonitorItem("WAS-TRANS-PMI", "WAS-TRANS-PMI-9", "全局超时事务率", "%", "全局超时事务率", MonitorItemType.NUMBER),
				new MonitorItem("WAS-TRANS-PMI", "WAS-TRANS-PMI-10", "本地超时事务率", "%", "本地超时事务率", MonitorItemType.NUMBER),

				new MonitorItem("WASJDBC","WASJDBC-1", "创建连接数", "个", "JDBC连接池创建连接数量", MonitorItemType.NUMBER),
				new MonitorItem("WASJDBC","WASJDBC-2", "关闭连接数", "个", "JDBC连接池关闭连接数量", MonitorItemType.NUMBER),
				new MonitorItem("WASJDBC","WASJDBC-3", "分配连接数", "个", "JDBC连接池分配连接数量", MonitorItemType.NUMBER),
				new MonitorItem("WASJDBC","WASJDBC-4", "返回连接数", "个", "JDBC连接池返回连接数量", MonitorItemType.NUMBER),
				new MonitorItem("WASJDBC","WASJDBC-5", "连接池大小", "个", "JDBC连接池大小", MonitorItemType.NUMBER),
				new MonitorItem("WASJDBC","WASJDBC-6", "空闲连接池大小", "个", "JDBC空闲连接池大小", MonitorItemType.NUMBER),
				new MonitorItem("WASJDBC","WASJDBC-7", "并发等待数", "个", "JDBC连接池并发等待数量", MonitorItemType.NUMBER),
				new MonitorItem("WASJDBC","WASJDBC-8", "故障数", "个", "JDBC连接池故障数", MonitorItemType.NUMBER),
				new MonitorItem("WASJDBC","WASJDBC-9", "使用百分率", "%", "JDBC连接池使用百分率", MonitorItemType.NUMBER),
				new MonitorItem("WASJDBC","WASJDBC-10", "平均使用时间", "毫秒", "JDBC连接池平均使用时间", MonitorItemType.NUMBER),
				new MonitorItem("WASJDBC","WASJDBC-11", "平均等待时间", "毫秒", "JDBC连接池平均等待时间", MonitorItemType.NUMBER),

				new MonitorItem("WASJVM","WASJVM-1", "JVM堆栈大小", "兆字节(MB)", "JVM申请的堆栈大小", MonitorItemType.NUMBER),
				new MonitorItem("WASJVM","WASJVM-2", "JVM堆栈利用率", "%", "JVM当前堆栈利用率", MonitorItemType.NUMBER),

				new MonitorItem("WASSERVLET","WASSERVLET-1", "创建会话数", "个", "已创建的会话数", MonitorItemType.NUMBER),
				new MonitorItem("WASSERVLET","WASSERVLET-2", "无效会话数", "个", "无效的会话数", MonitorItemType.NUMBER),
				new MonitorItem("WASSERVLET","WASSERVLET-3", "超时会话数", "个", "由超时而导致无效的会话数", MonitorItemType.NUMBER),
				new MonitorItem("WASSERVLET","WASSERVLET-4", "新建会话数", "个", "当前有效的会话数", MonitorItemType.NUMBER),
				new MonitorItem("WASSERVLET","WASSERVLET-5", "活动会话数", "个", "当前正在处理使用的会话数", MonitorItemType.NUMBER),

				new MonitorItem("WASTHREADPOOL","WASTHREADPOOL-1", "线程创建数", "个", "线程创建数", MonitorItemType.NUMBER),
				new MonitorItem("WASTHREADPOOL","WASTHREADPOOL-2", "线程销毁数", "个", "线程销毁数", MonitorItemType.NUMBER),
				new MonitorItem("WASTHREADPOOL","WASTHREADPOOL-3", "活动线程数", "个", "活动线程数", MonitorItemType.NUMBER),
				new MonitorItem("WASTHREADPOOL","WASTHREADPOOL-4", "线程池大小", "个", "线程池大小", MonitorItemType.NUMBER),
				new MonitorItem("WASTHREADPOOL","WASTHREADPOOL-5", "最大百分数", "", "最大百分数", MonitorItemType.NUMBER),
				new MonitorItem("WASTHREADPOOL","WASTHREADPOOL-6", "未释放线程", "个", "未释放线程", MonitorItemType.NUMBER),
				new MonitorItem("WASTHREADPOOL","WASTHREADPOOL-7", "线程池利用率", "%", "线程池利用率", MonitorItemType.NUMBER),

				new MonitorItem("WASWEBAPP","WASWEBAPP-1", "装入servlet数", "个", "Web应用装入servlet的数量", MonitorItemType.NUMBER),
				new MonitorItem("WASWEBAPP","WASWEBAPP-2", "重新装入servlet数", "个", "Web应用重新装入的servlet数量", MonitorItemType.NUMBER),
				new MonitorItem("WASWEBAPP","WASWEBAPP-3", "servlet处理请求总数", "个", "Web应用servlet处理的请求总数", MonitorItemType.NUMBER),
				new MonitorItem("WASWEBAPP","WASWEBAPP-4", "并发处理请求数", "个", "Web应用并发处理的请求数", MonitorItemType.NUMBER),
				new MonitorItem("WASWEBAPP","WASWEBAPP-5", "完成servlet请求平均响应时间", "毫秒", "Web应用完成servlet请求的平均响应时间",
						MonitorItemType.NUMBER),
				new MonitorItem("WASWEBAPP","WASWEBAPP-6", "servlet/JSP错误总数", "个", "Web应用servlet/JSP中的错误总数", MonitorItemType.NUMBER) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(WASMonitorMethodOption.TYPE_ID, "WebSphere Agent监测协议",
				WASParamPanel.class) };
	}

}
