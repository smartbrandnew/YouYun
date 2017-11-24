package com.broada.carrier.monitor.impl.mw.weblogic;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.basic.WLSBasicMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.cluster.WLSClusterMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.ejb.WLSEJBMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.jdbc.WLSJdbcMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.jvm.WLSJvmMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.server.WLSServerMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.subsystem.WLSSubSystemMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.agent.webappstatus.WLSWebAppStatusMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.jdbc.WLSJDBCMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.jta.WLSJTAMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.jvm.WLSJVMMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.servlet.WLSServletMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.thread.WLSThreadMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.webapp.WLSWebAppMonitor;
import com.broada.carrier.monitor.impl.mw.weblogic.snmp.wlec.WlsWLECMonitor;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.method.weblogic.agent.WebLogicJMXOption;
import com.broada.carrier.monitor.method.weblogic.agent.WebLogicJMXPanel;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class WeblogicMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeWeblogic = new String[] { "Weblogic" };
		String[] methodSnmp = new String[] { SnmpMethod.TYPE_ID };
		String[] methodWebLogicAgent = new String[] { WebLogicJMXOption.TYPE_ID };
		int index = 1;
		return new MonitorType[] {

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLS-WLEC", "WebLogic 企业连接监测", "通过SNMP协议监测WebLogic的企业连接信息",
						MultiInstanceConfiger.class.getName(), WlsWLECMonitor.class.getName(), index++,
						targetTypeWeblogic, methodSnmp),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLSTHREAD", "WebLogic线程监测", "通过SNMP协议监测Weblogic线程使用情况。",
						MultiInstanceConfiger.class.getName(), WLSThreadMonitor.class.getName(), index++,
						targetTypeWeblogic, methodSnmp),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLSSERVLET", "WebLogic Servlet监测", "通过SNMP协议监测Weblogic Servlet情况。",
						MultiInstanceConfiger.class.getName(), WLSServletMonitor.class.getName(), index++,
						targetTypeWeblogic, methodSnmp),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLSJVM", "Weblogic JVM监测", "通过SNMP协议监测Weblogic JVM的性能状态。",
						SingleInstanceConfiger.class.getName(), WLSJVMMonitor.class.getName(), index++,
						targetTypeWeblogic, methodSnmp),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLSJDBC", "Weblogic JDBC监测", "通过SNMP协议监测Weblogic JDBC连接池的使用情况。",
						MultiInstanceConfiger.class.getName(), WLSJDBCMonitor.class.getName(), index++,
						targetTypeWeblogic, methodSnmp),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLSJTA", "Weblogic JTA监测", "通过SNMP监测Weblogic JTA的性能状态",
						SingleInstanceConfiger.class.getName(), WLSJTAMonitor.class.getName(), index++,
						targetTypeWeblogic, methodSnmp),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLSEJB", "WebLogic EJB监测(JMX)", "监测WebLogic EJB运行情况",
						MultiInstanceConfiger.class.getName(), WLSEJBMonitor.class.getName(), index++,
						targetTypeWeblogic, methodWebLogicAgent),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLSWEBAPP", "Weblogic Web应用监测(JMX)", "通过代理监测Weblogic Web应用的性能状态",
						MultiInstanceConfiger.class.getName(),
						com.broada.carrier.monitor.impl.mw.weblogic.agent.webapp.WLSWebAppMonitor.class.getName(),
						index++, targetTypeWeblogic, methodWebLogicAgent),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLSWEBAPP-SNMP", "Weblogic Web应用监测(SNMP)", "通过SNMP监测Weblogic Web应用的性能状态",
						MultiInstanceConfiger.class.getName(), WLSWebAppMonitor.class.getName(), index++,
						targetTypeWeblogic, methodSnmp),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLSBASIC", "WebLogic 基本信息监测(JMX) [可用性]", "监测WebLogic的基本信息",
						SingleInstanceConfiger.class.getName(), WLSBasicMonitor.class.getName(), index++,
						targetTypeWeblogic, methodWebLogicAgent),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLS-STATUS", "weblogic应用状态监测(JMX)",
						"通过JMX方式监控weblogic应用状态信息;注意:该监测只支持WebLogic Server 9及以上版本",
						MultiInstanceConfiger.class.getName(), WLSWebAppStatusMonitor.class.getName(), index++,
						targetTypeWeblogic, methodWebLogicAgent),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLS-SERVLET", "weblogic servlet监测(JMX)", "通过JMX方式监控weblogic servlet信息",
						MultiInstanceConfiger.class.getName(),
						com.broada.carrier.monitor.impl.mw.weblogic.agent.servlet.WLSServletMonitor.class.getName(),
						index++, targetTypeWeblogic, methodWebLogicAgent),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLS-JVM", "weblogic JVM监测(JMX)", "通过JMX方式监控weblogic JVM信息",
						MultiInstanceConfiger.class.getName(), WLSJvmMonitor.class.getName(), index++,
						targetTypeWeblogic, methodWebLogicAgent),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLS-JDBC", "weblogic JDBC监测(JMX)", "通过JMX方式监控weblogic JDBC信息",
						MultiInstanceConfiger.class.getName(), WLSJdbcMonitor.class.getName(), index++,
						targetTypeWeblogic, methodWebLogicAgent),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLS-CLUSTER", "weblogic集群服务监测(JMX)", "通过JMX方式监控weblogic 集群服务信息",
						MultiInstanceConfiger.class.getName(), WLSClusterMonitor.class.getName(), index++,
						targetTypeWeblogic, methodWebLogicAgent),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLS-SERVER", "weblogic server性能监测(JMX)", "通过JMX方式监控weblogic server性能信息",
						SingleInstanceConfiger.class.getName(), WLSServerMonitor.class.getName(), index++,
						targetTypeWeblogic, methodWebLogicAgent),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLS-THREAD", "WebLogic线程监测(JMX)",
						"通过JMX方式监控weblogic THREAD信息;注意:该监测只支持WebLogic Server 9及以上版本",
						MultiInstanceConfiger.class.getName(),
						com.broada.carrier.monitor.impl.mw.weblogic.agent.thread.WLSThreadMonitor.class.getName(),
						index++, targetTypeWeblogic, methodWebLogicAgent),

				new MonitorType("WEBLOGIC", "WEBLOGIC-WLS-SUBSYSTEM", "weblogic子系统监测(JMX)",
						"通过JMX方式监控weblogic 子系统信息;注意:该监测只支持WebLogic Server 9及以上版本",
						MultiInstanceConfiger.class.getName(), WLSSubSystemMonitor.class.getName(), index++,
						targetTypeWeblogic, methodWebLogicAgent) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] {
				new MonitorItem("WEBLOGIC-WLS-STATUS", "WLS-STATUS-1", "当前应用状态", "", "Web应用当前状态", MonitorItemType.TEXT),

				new MonitorItem("WEBLOGIC-WLS-SERVLET", "WLS-SERVLET-1", "总调用次数", "次", "总调用次数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-SERVLET", "WLS-SERVLET-2", "最长执行时间", "毫秒", "最长执行时间", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-SERVLET", "WLS-SERVLET-3", "平均执行时间", "毫秒", "平均执行时间", MonitorItemType.NUMBER),

				new MonitorItem("WEBLOGIC-WLS-JVM","WLS-JVM-1", "当前堆大小", "byte", "当前堆大小", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-JVM","WLS-JVM-2", "当前可用堆", "byte", "当前可用堆", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-JVM","WLS-JVM-3", "堆可用百分比", "", "堆可用百分比", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-JVM","WLS-JVM-4", "最大堆大小", "byte", "最大堆大小", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-JDBC", "WLS-JDBC-1", "状态", "", "JDBC的状态", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLS-JDBC", "WLS-JDBC-2", "活动连接平均计数", "个", "活动连接平均计数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-JDBC", "WLS-JDBC-3", "当前活动连接计数", "个", "当前活动连接计数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-JDBC", "WLS-JDBC-4", "最大活动连接计数", "个", "最大活动连接计数", MonitorItemType.NUMBER),

				new MonitorItem("WEBLOGIC-WLS-CLUSTER", "WLS-CLUSTER-1", "集群名称", "", "集群的名称", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLS-CLUSTER", "WLS-CLUSTER-2", "服务名称", "", "服务(Server)的名称", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLS-CLUSTER", "WLS-CLUSTER-3", "服务状态", "", "集群服务的状态", MonitorItemType.TEXT),

				new MonitorItem("WEBLOGIC-WLS-SERVER", "WLS-SERVER-1", "服务名字", "", "当前运行的server名字", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLS-SERVER", "WLS-SERVER-2", "状态", "", "server的状态", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLS-SERVER", "WLS-SERVER-3", "运行状态", "", "server的运行状态", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLS-SERVER", "WLS-SERVER-4", "空闲线程数", "", "server的空闲线程数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-SERVER", "WLS-SERVER-5", "请求队列大小", "", "server的请求队列大小", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-SERVER", "WLS-SERVER-6", "内存使用量", "byte", "server的内存使用量", MonitorItemType.NUMBER),

				new MonitorItem("WEBLOGIC-WLS-THREAD", "WLS-THREAD-1", "总线程数", "个", "WebLogic线程队列中线程总数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-THREAD", "WLS-THREAD-2", "空闲线程数", "个", "WebLogic线程队列中空闲数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-THREAD", "WLS-THREAD-3", "吞吐量", "个", "WebLogic线程队列吞吐量", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-THREAD", "WLS-THREAD-4", "运行状况", "", "WebLogic线程队列运行状况", MonitorItemType.TEXT),

				new MonitorItem("WEBLOGIC-WLS-SUBSYSTEM", "WLS-SUBSYSTEM-1", "运行状态", "", "子系统的运行状态", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLS-SUBSYSTEM", "WLS-SUBSYSTEM-2", "原因", "", "子系统的运行状态的原因", MonitorItemType.TEXT),

				/**
				 * new MonitorItem("WLS-JCA-1", "可用性百分比", "%", "连接池中JCA可用性百分比",
				 * MonitorItemType.NUMBER), new MonitorItem("WLS-JCA-2",
				 * "连接泄漏比例", "%", "连接池中JCA连接泄漏比例", MonitorItemType.NUMBER),
				 */

				new MonitorItem("WEBLOGIC-WLS-WLEC", "WLS-WLEC-1", "对象名称", "", "客户端对象名称", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLS-WLEC", "WLS-WLEC-2", "客户端连接类型", "", "客户端连接类型", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLS-WLEC", "WLS-WLEC-3", "客户端连接地址", "", "客户端连接地址", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLS-WLEC", "WLS-WLEC-4", "客户端连接请求数", "个", "客户端连接请求数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-WLEC", "WLS-WLEC-5", "客户端请求等待数", "个", "客户端请求等待数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLS-WLEC", "WLS-WLEC-6", "客户端请求错误数", "个", "客户端请求错误数", MonitorItemType.NUMBER),

				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-1", "WebLogic版本", "", "WebLogic版本", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-2", "运行状态", "", "运行状态", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-3", "健康状况", "", "健康状况", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-4", "服务器", "", "服务器", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-5", "服务监听端口", "", "服务监听端口", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-6", "SSL端口", "", "SSL端口", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-7", "活动socket连接数", "个", "活动socket连接数", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-8", "重启次数", "次", "重启次数", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-9", "当前目录", "", "当前目录", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-10", "堆栈大小", "	MB", "堆栈大小", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-11", "当前可用堆栈", "MB", "当前可用堆栈", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-12", "操作系统", "", "操作系统", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-13", "操作系统版本", "", "操作系统版本", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-14", "Java版本", "", "Java版本", MonitorItemType.TEXT),
				new MonitorItem("WEBLOGIC-WLSBASIC","WLSBASIC-15", "JavaVendor", "", "JavaVendor", MonitorItemType.TEXT),

				new MonitorItem("WEBLOGIC-WLSEJB","WLSEJB-1", "激活次数", "次", "激活次数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSEJB","WLSEJB-2", "钝化次数", "次", "钝化次数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSEJB","WLSEJB-3", "缓存个数", "个", "缓存个数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSEJB","WLSEJB-4", "事务提交次数", "次", "事务提交次数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSEJB","WLSEJB-5", "事务回滚次数", "次", "事务回滚次数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSEJB","WLSEJB-6", "事务超时次数", "次", "事务超时次数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSEJB","WLSEJB-7", "访问次数", "次", "访问次数", MonitorItemType.NUMBER),

				new MonitorItem("WEBLOGIC-WLSJDBC","WLSJDBC-1", "连接总数", "个", "JDBC连接总数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJDBC","WLSJDBC-2", "活动连接数", "个", "活动JDBC连接数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJDBC","WLSJDBC-3", "最大活动连接数", "个", "Weblogic启动后,活动JDBC连接数的最大值", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJDBC","WLSJDBC-4", "等待连接数", "个", "等待JDBC连接数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJDBC","WLSJDBC-5", "最大等待连接数", "个", "Weblogic启动后,等待JDBC连接数的最大值", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJDBC","WLSJDBC-6", "最长等待连接时间", "秒", "Weblogic启动后,等待JDBC连接的最长时间", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJDBC","WLSJDBC-7", "连接池容量", "个", "JDBC连接池容量,一般为一个固定值", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJDBC","WLSJDBC-8", "JDBC连接百分比", "%", "连接池中JDBC连接数的可用性百分比", MonitorItemType.NUMBER),

				new MonitorItem("WEBLOGIC-WLSJTA","WLSJTA-1", "资源错误导致的事务回滚数", "个", "因资源错误而导致的事务回滚数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJTA","WLSJTA-2", "系统错误导致的事务回滚数", "个", "因系统错误而导致的事务回滚数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJTA","WLSJTA-3", "应用程序错误导致的事务回滚数", "个", "因应用程序错误而导致的事务回滚数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJTA","WLSJTA-4", "已处理事务数", "个", "包括回滚的、提交的、启发式的事务", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJTA","WLSJTA-5", "全部回滚事务比例", "%", "全部的回滚事务(包括回滚的、提交的、启发式的事务)与已经处理的全部事务之比",
						MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJTA","WLSJTA-6", "资源错误导致回滚的事务比例", "%", "资源错误导致回滚的事务与已经处理的全部事务之比", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJTA","WLSJTA-7", "系统错误导致回滚的事务比例", "%", "系统错误导致回滚的事务与已经处理的全部事务之比", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJTA","WLSJTA-8", "应用程序错误导致回滚的事务比例", "%", "应用程序错误导致回滚的事务与已经处理的全部事务之比", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJTA","WLSJTA-9", "每秒交易执行数量", "次/秒", "服务中每秒处理事务的数量", MonitorItemType.NUMBER),

				new MonitorItem("WEBLOGIC-WLSJVM","WLSJVM-1", "JVM堆栈大小", "兆字节(MB)", "JVM申请的堆栈大小", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSJVM","WLSJVM-2", "JVM堆栈利用率", "%", "JVM当前堆栈利用率", MonitorItemType.NUMBER),

				new MonitorItem("WEBLOGIC-WLSSERVLET","WLSSERVLET-1", "平均执行时间", "毫秒", "Servlet平均执行的时间", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSSERVLET","WLSSERVLET-2", "最大执行时间", "毫秒", "Servlet执行的最长时间", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSSERVLET","WLSSERVLET-3", "调用次数", "次", "Servlet被调用的总次数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSSERVLET","WLSSERVLET-4", "servlet的请求速率", "次/秒", "servlet的每秒请求调用速率", MonitorItemType.NUMBER),

				new MonitorItem("WEBLOGIC-WLSTHREAD","WLSTHREAD-1", "总线程数", "个", "WebLogic线程队列中执行队列的服务请求总数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSTHREAD","WLSTHREAD-2", "空闲线程数", "个", "WebLogic线程队列中空闲数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSTHREAD","WLSTHREAD-3", "当前等待请求数", "个", "WebLogic线程队列中执行队列的当前等待请求数", MonitorItemType.NUMBER),

				new MonitorItem("WEBLOGIC-WLSWEBAPP","WLSWEBAPP-1", "当前session个数", "个", "Web应用当前session个数", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSWEBAPP","WLSWEBAPP-2", "session最大值", "个", "Web应用session最大值", MonitorItemType.NUMBER),
				new MonitorItem("WEBLOGIC-WLSWEBAPP","WLSWEBAPP-3", "session总数", "个", "Web应用session总数", MonitorItemType.NUMBER)

		};
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(WebLogicJMXOption.TYPE_ID, "WebLogic Agent监测协议",
				WebLogicJMXPanel.class) };
	}
}
