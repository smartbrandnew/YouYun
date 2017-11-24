package com.broada.carrier.monitor.impl.mw.webspherend;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.method.websphere.WASMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class IbmNDMonitorPackage implements MonitorPackage {
	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeIds = new String[] { "Server" };
		String[] methodTypeIds = new String[] { WASMonitorMethodOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {
				new MonitorType("WEBSPHERE","WSND-CACHE-PMI", "Websphere集群 动态高速缓存监测 [可用性]", "监测Websphere集群 动态高速缓存性能",
						MultiInstanceConfiger.class.getName(), WSNDPmiMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("WEBSPHERE","WSND-TRANS-PMI", "Websphere集群 事务监测", "监测Websphere集群 事务性能状态",
						MultiInstanceConfiger.class.getName(), WSNDPmiMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("WEBSPHERE","WSND-JDBC", "Websphere集群 JDBC监测", "监测Websphere集群 JDBC的性能状态", MultiInstanceConfiger.class.getName(),
						WSNDPmiMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("WEBSPHERE","WSND-JVM", "Websphere集群 JVM监测 [基本信息]", "监测Websphere集群 JVM的性能状态", MultiInstanceConfiger.class.getName(),
						WSNDPmiMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("WEBSPHERE","WSND-SERVLET", "Websphere集群 Servlet会话监测", "监测Websphere集群 Servlet会话性能状态",
						MultiInstanceConfiger.class.getName(), WSNDPmiMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("WEBSPHERE","WSND-THREADPOOL", "WebSphere集群 线程池监测", "监测WebSphere集群 线程池使用情况。",
						MultiInstanceConfiger.class.getName(), WSNDPmiMonitor.class.getName(), index++, targetTypeIds, methodTypeIds),

				new MonitorType("WEBSPHERE","WSND-WEBAPP", "Websphere集群 Web应用监测", "监测Websphere集群 Web应用的性能状态",
						MultiInstanceConfiger.class.getName(), WSNDPmiMonitor.class.getName(), index++, targetTypeIds, methodTypeIds), };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] { 
				new MonitorItem("WEBSPHERE","WSND-CACHE-PMI-1", "最大高速缓存数", "个", "内存中的最大高速缓存条目数", MonitorItemType.NUMBER),
				new MonitorItem("WEBSPHERE","WSND-CACHE-PMI-2", "当前高速缓存数", "个", "内存中的当前高速缓存条目数", MonitorItemType.NUMBER),
				new MonitorItem("WEBSPHERE","WSND-CACHE-PMI-3", "高速缓存使用率", "%", "当前动态高速缓存的使用率", MonitorItemType.NUMBER),
				new MonitorItem("WEBSPHERE","WSND-CACHE-PMI-4", "高速缓存不命中率", "%", "当前动态高速缓存的不命中率", MonitorItemType.NUMBER),

				new MonitorItem("WSND-TRANS-PMI","WSND-TRANS-PMI-1", "全局事务总数", "个", "在服务器上启动的全局事务总数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-TRANS-PMI","WSND-TRANS-PMI-2", "本地事务总数", "个", "在服务器上启动的本地事务的总数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-TRANS-PMI","WSND-TRANS-PMI-3", "全局事务响应时间", "毫秒", "全局事务的平均持续时间", MonitorItemType.NUMBER),
				new MonitorItem("WSND-TRANS-PMI","WSND-TRANS-PMI-4", "本地事务响应时间", "毫秒", "本地事务的平均持续时间", MonitorItemType.NUMBER),
				new MonitorItem("WSND-TRANS-PMI","WSND-TRANS-PMI-5", "超时全局事务数", "个", "超时的全局事务数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-TRANS-PMI","WSND-TRANS-PMI-6", "超时本地事务数", "个", "超时的本地事务数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-TRANS-PMI","WSND-TRANS-PMI-7", "并发活动全局事务数", "个", "并发活动全局事务数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-TRANS-PMI","WSND-TRANS-PMI-8", "并发活动本地事务数", "个", "并发活动本地事务数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-TRANS-PMI","WSND-TRANS-PMI-9", "全局超时事务率", "%", "全局超时事务率", MonitorItemType.NUMBER),
				new MonitorItem("WSND-TRANS-PMI","WSND-TRANS-PMI-10", "本地超时事务率", "%", "本地超时事务率", MonitorItemType.NUMBER),

				new MonitorItem("WSND-JDBC","WSND-JDBC-1", "创建连接数", "个", "JDBC连接池创建连接数量", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JDBC","WSND-JDBC-2", "关闭连接数", "个", "JDBC连接池关闭连接数量", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JDBC","WSND-JDBC-3", "分配连接数", "个", "JDBC连接池分配连接数量", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JDBC","WSND-JDBC-4", "返回连接数", "个", "JDBC连接池返回连接数量", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JDBC","WSND-JDBC-5", "连接池大小", "个", "JDBC连接池大小", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JDBC","WSND-JDBC-6", "空闲连接池大小", "个", "JDBC空闲连接池大小", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JDBC","WSND-JDBC-7", "并发等待数", "个", "JDBC连接池并发等待数量", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JDBC","WSND-JDBC-8", "故障数", "个", "JDBC连接池故障数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JDBC","WSND-JDBC-9", "使用百分率", "%", "JDBC连接池使用百分率", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JDBC","WSND-JDBC-10", "平均使用时间", "毫秒", "JDBC连接池平均使用时间", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JDBC","WSND-JDBC-11", "平均等待时间", "毫秒", "JDBC连接池平均等待时间", MonitorItemType.NUMBER),

				new MonitorItem("WSND-JVM", "WSND-JVM-1", "JVM堆栈大小", "兆字节(MB)", "JVM申请的堆栈大小", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JVM", "WSND-JVM-2", "JVM堆栈利用率", "%", "JVM当前堆栈利用率", MonitorItemType.NUMBER),
				new MonitorItem("WSND-JVM", "WSND-JVM-3", "CPU占用", "%", "JVM的CPU使用百分比", MonitorItemType.NUMBER),

				new MonitorItem("WSND-SERVLET", "WSND-SERVLET-1", "创建会话数", "个", "已创建的会话数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-SERVLET", "WSND-SERVLET-2", "无效会话数", "个", "无效的会话数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-SERVLET", "WSND-SERVLET-3", "超时会话数", "个", "由超时而导致无效的会话数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-SERVLET", "WSND-SERVLET-4", "新建会话数", "个", "当前有效的会话数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-SERVLET", "WSND-SERVLET-5", "活动会话数", "个", "当前正在处理使用的会话数", MonitorItemType.NUMBER),

				new MonitorItem("WSND-THREADPOOL", "WSND-THREADPOOL-1", "线程创建数", "个", "线程创建数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-THREADPOOL", "WSND-THREADPOOL-2", "线程销毁数", "个", "线程销毁数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-THREADPOOL", "WSND-THREADPOOL-3", "活动线程数", "个", "活动线程数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-THREADPOOL", "WSND-THREADPOOL-4", "线程池大小", "个", "线程池大小", MonitorItemType.NUMBER),
				new MonitorItem("WSND-THREADPOOL", "WSND-THREADPOOL-5", "最大百分数", "", "最大百分数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-THREADPOOL", "WSND-THREADPOOL-6", "未释放线程", "个", "未释放线程", MonitorItemType.NUMBER),
				new MonitorItem("WSND-THREADPOOL", "WSND-THREADPOOL-7", "线程池利用率", "%", "线程池利用率", MonitorItemType.NUMBER),

				new MonitorItem("WSND-WEBAPP", "WSND-WEBAPP-1", "装入servlet数", "个", "Web应用装入servlet的数量", MonitorItemType.NUMBER),
				new MonitorItem("WSND-WEBAPP", "WSND-WEBAPP-2", "重新装入servlet数", "个", "Web应用重新装入的servlet数量", MonitorItemType.NUMBER),
				new MonitorItem("WSND-WEBAPP", "WSND-WEBAPP-3", "servlet处理请求总数", "个", "Web应用servlet处理的请求总数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-WEBAPP", "WSND-WEBAPP-4", "并发处理请求数", "个", "Web应用并发处理的请求数", MonitorItemType.NUMBER),
				new MonitorItem("WSND-WEBAPP", "WSND-WEBAPP-5", "servlet平均响应时间", "毫秒", "Web应用完成servlet请求的平均响应时间", MonitorItemType.NUMBER),
				new MonitorItem("WSND-WEBAPP", "WSND-WEBAPP-6", "servlet/JSP错误总数", "个", "Web应用servlet/JSP中的错误总数", MonitorItemType.NUMBER),
		};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] {};
	}
}
