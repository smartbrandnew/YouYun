package com.broada.carrier.monitor.impl.mw.jboss;

import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.impl.mw.jboss.basic.JbossBasicMonitor;
import com.broada.carrier.monitor.impl.mw.jboss.jdbc.Jboss7JdbcMonitor;
import com.broada.carrier.monitor.impl.mw.jboss.jdbc.JbossJdbcMonitor;
import com.broada.carrier.monitor.impl.mw.jboss.jvm.Jboss7PmiMonitor;
import com.broada.carrier.monitor.impl.mw.jboss.pmi.JbossPmiMonitor;
import com.broada.carrier.monitor.impl.mw.jboss.servlet.JbossServletMonitor;
import com.broada.carrier.monitor.impl.mw.jboss.thread.Jboss6ThreadMonitor;
import com.broada.carrier.monitor.impl.mw.jboss.threadpool.JbossThreadMonitor;
import com.broada.carrier.monitor.impl.mw.jboss.webapp.JbossWebMonitor;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;
import com.broada.carrier.monitor.method.jboss.JbossJMXPanel;
import com.broada.carrier.monitor.method.weblogic.agent.WebLogicJMXOption;
import com.broada.carrier.monitor.method.weblogic.agent.WebLogicJMXPanel;
import com.broada.carrier.monitor.method.websphere.WASMonitorMethodOption;
import com.broada.carrier.monitor.method.websphere.WASParamPanel;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethodType;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorPackage;

public class JbossMonitorPackage implements MonitorPackage {

	@Override
	public MonitorType[] getTypes() {
		String[] targetTypeJBoss = new String[] { "JBoss" };
		String[] methodJBoss = new String[] { JbossJMXOption.TYPE_ID };
		int index = 1;

		return new MonitorType[] {

				new MonitorType("JBOSS", "JBOSS-BASIC", "JBOSS基本信息监测(4.x，5.x，6.x，7.x)", "监测JBOSS基本信息",
						SingleInstanceConfiger.class.getName(), JbossBasicMonitor.class.getName(), index++,
						targetTypeJBoss, methodJBoss),
				new MonitorType("JBOSS", "JBOSS-JDBC", "JBOSS数据库连接池监测(4.x，5.x，6.x)", "监测JBOSS数据库连接池信息",
						MultiInstanceConfiger.class.getName(), JbossJdbcMonitor.class.getName(), index++,
						targetTypeJBoss, methodJBoss),
				new MonitorType("JBOSS", "JBOSS7-JDBC", "JBOSS 7.x数据库连接池监测(7.x)", "监测JBOSS7.x数据库连接池信息",
						MultiInstanceConfiger.class.getName(), Jboss7JdbcMonitor.class.getName(), index++,
						targetTypeJBoss, methodJBoss),
				new MonitorType("JBOSS", "JBOSS7-JVM", "JBOSS 7.x的JVM监测(7.x)", "监测JBOSS7的JVM信息",
						SingleInstanceConfiger.class.getName(), Jboss7PmiMonitor.class.getName(), index++,
						targetTypeJBoss, methodJBoss),
				new MonitorType("JBOSS", "JBOSS-PMI", "JBOSS性能监测(4.x，5.x，6.x)", "监测JBOSS性能信息",
						SingleInstanceConfiger.class.getName(), JbossPmiMonitor.class.getName(), index++,
						targetTypeJBoss, methodJBoss),
				new MonitorType("JBOSS", "JBOSS-SERVLET", "JBOSS的Servlet会话监测(4.x，5.x，6.x)", "监测JBOSS的Servlet会话信息",
						MultiInstanceConfiger.class.getName(), JbossServletMonitor.class.getName(), index++,
						targetTypeJBoss, methodJBoss),
				new MonitorType("JBOSS", "JBOSS6-THREAD", "JBOSS 6.x的线程监测", "监测JBOSS6.x的线程信息",
						MultiInstanceConfiger.class.getName(), Jboss6ThreadMonitor.class.getName(), index++,
						targetTypeJBoss, methodJBoss),
				new MonitorType("JBOSS", "JBOSS-THREAD", "JBOSS 线程池监测(4.x，5.x)", "监测JBOSS线程池信息",
						MultiInstanceConfiger.class.getName(), JbossThreadMonitor.class.getName(), index++,
						targetTypeJBoss, methodJBoss),
				new MonitorType("JBOSS", "JBOSS-WEB", "JBOSS WEB信息监测(4.x，5.x，6.x)", "监测JBOSS的WEB信息",
						MultiInstanceConfiger.class.getName(), JbossWebMonitor.class.getName(), index++,
						targetTypeJBoss, methodJBoss) };
	}

	@Override
	public MonitorItem[] getItems() {
		return new MonitorItem[] { new MonitorItem( "JBOSS-BASIC", "JBOSS-BASIC-1", "jdk供应商", "", "jdk供应商信息", MonitorItemType.TEXT),
				new MonitorItem( "JBOSS-BASIC", "JBOSS-BASIC-2", "jdk版本", "", "jdk版本信息", MonitorItemType.TEXT),
				new MonitorItem( "JBOSS-BASIC", "JBOSS-BASIC-3", "操作系统", "", "操作系统信息", MonitorItemType.TEXT),
				new MonitorItem( "JBOSS-BASIC", "JBOSS-BASIC-4", "Jboss版本", "", "Jboss版本信息", MonitorItemType.TEXT),
				new MonitorItem( "JBOSS-BASIC", "JBOSS-BASIC-5", "运行状态", "", "运行状态", MonitorItemType.TEXT),

				new MonitorItem("JBOSS-JDBC", "JBOSS-JDBC-1", "可用连接数", "个", "可用连接数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-JDBC", "JBOSS-JDBC-2", "在使用的连接数", "个", "正在使用的连接数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-JDBC", "JBOSS-JDBC-3", "创建的连接数", "个", "创建的连接数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-JDBC", "JBOSS-JDBC-4", "销毁的连接数", "个", "销毁的连接数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-JDBC", "JBOSS-JDBC-5", "最大使用的连接数", "个", "最大使用的连接数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-JDBC", "JBOSS-JDBC-6", "总连接数", "个", "总连接数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-JDBC", "JBOSS-JDBC-7", "连接阻塞超时时间", "秒", "获取连接最大等待时间", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-JDBC", "JBOSS-JDBC-8", "连接池最大容量", "个", "连接池最大容量", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-JDBC", "JBOSS-JDBC-9", "连接池最小容量", "个", "连接池最小容量", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-JDBC", "JBOSS-JDBC-10", "连接闲置时间", "分钟", "当前连接的最大空闲时间", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-JDBC", "JBOSS-JDBC-11", "使用百分比", "%", "连接使用的百分比", MonitorItemType.NUMBER),

				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-1", "获取连接最大等待时间", "秒", "获取连接最大等待时间", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-2", "连接池最大容量", "个", "连接池最大容量", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-3", "连接池最小容量", "个", "连接池最小容量", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-4", "当前连接的最大空闲时间", "分钟", "当前连接的最大空闲时间", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-5", "新创建的SQL数", "个", "新创建的SQL数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-6", "连接池大小", "个", "连接池大小", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-7", "活跃连接总数", "个", "活跃连接总数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-8", "可用连接数", "个", "可用连接数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-9", "当前连接数", "个", "当前连接数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-10", "平均阻塞时间", "个", "平均阻塞时间", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-11", "创建的连接数", "个", "创建的连接数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JDBC", "JBOSS7-JDBC-12", "销毁的连接数", "个", "销毁的连接数", MonitorItemType.NUMBER),

				new MonitorItem("JBOSS7-JVM", "JBOSS7-JVM-1", "最大堆内存", "MB", "最大堆内存", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JVM", "JBOSS7-JVM-2", "使用的堆内存", "MB", "使用的堆内存", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JVM", "JBOSS7-JVM-3", "提交的堆内存", "MB", "提交的堆内存", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JVM", "JBOSS7-JVM-4", "初始化的堆内存", "MB", "初始化的堆内存", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JVM", "JBOSS7-JVM-5", "最大非堆内存", "MB", "最大非堆内存", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JVM", "JBOSS7-JVM-6", "使用的非堆内存", "MB", "使用的非堆内存", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JVM", "JBOSS7-JVM-7", "提交的非堆内存", "MB", "提交的非堆内存", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JVM", "JBOSS7-JVM-8", "初始化的非堆内存", "MB", "初始化的非堆内存", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JVM", "JBOSS7-JVM-9", "当前线程数", "个", "当前线程数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS7-JVM", "JBOSS7-JVM-10", "守护线程数", "个", "守护线程数", MonitorItemType.NUMBER),

				new MonitorItem("JBOSS-PMI", "JBOSS-PMI-1", "已使用内存", "MB", "已使用内存", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-PMI", "JBOSS-PMI-2", "空闲内存", "MB", "空闲内存", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-PMI", "JBOSS-PMI-3", "总内存", "MB", "总内存", MonitorItemType.NUMBER),

				new MonitorItem("JBOSS-SERVLET", "JBOSS-SERVLET-1", "最大处理时间", "ms", "最大处理时间", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-SERVLET", "JBOSS-SERVLET-2", "平均处理时间", "ms", "平均处理时间", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-SERVLET", "JBOSS-SERVLET-3", "请求总数", "个", "请求总数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-SERVLET", "JBOSS-SERVLET-4", "错误的请求数", "个", "错误的请求数", MonitorItemType.NUMBER),

				new MonitorItem("JBOSS6-THREAD", "JBOSS6-THREAD-1", "核心线程数", "个", "核心线程数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS6-THREAD", "JBOSS6-THREAD-2", "最大线程数", "个", "最大线程数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS6-THREAD", "JBOSS6-THREAD-3", "拒绝线程数量", "个", "拒绝线程数量", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS6-THREAD", "JBOSS6-THREAD-4", "活动线程数", "个", "活动线程数", MonitorItemType.NUMBER),

				new MonitorItem("JBOSS-THREAD", "JBOSS-THREAD-1", "当前等待线程数", "个", "当前线程数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-THREAD", "JBOSS-THREAD-2", "最大等待线程数", "个", "最大线程数", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-THREAD", "JBOSS-THREAD-3", "线程池最小容量", "个", "线程池最小容量", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-THREAD", "JBOSS-THREAD-4", "线程池最大容量", "个", "线程池最大容量", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-THREAD", "JBOSS-THREAD-5", "线程池大小", "个", "线程池大小", MonitorItemType.NUMBER),
				new MonitorItem("JBOSS-THREAD", "JBOSS-THREAD-6", "活动线程数", "个", "活动线程数", MonitorItemType.NUMBER),

				new MonitorItem( "JBOSS-WEB", "JBOSS-WEB-1", "会话最大生存时间", "ms", "会话最大生存时间", MonitorItemType.NUMBER),
				new MonitorItem( "JBOSS-WEB", "JBOSS-WEB-2", "当前的会话数", "个", "活跃的会话数", MonitorItemType.NUMBER),
				new MonitorItem( "JBOSS-WEB", "JBOSS-WEB-3", "会话平均生存时间", "ms", "会话平均生存时间", MonitorItemType.NUMBER),
				new MonitorItem( "JBOSS-WEB", "JBOSS-WEB-4", "最大活跃会话数", "个", "线程池最大容量", MonitorItemType.NUMBER),
				new MonitorItem( "JBOSS-WEB", "JBOSS-WEB-5", "无效的会话数", "个", "当前最大活跃线程数", MonitorItemType.NUMBER),
				new MonitorItem( "JBOSS-WEB", "JBOSS-WEB-6", "被拒绝的会话数", "个", "拒绝会话数", MonitorItemType.NUMBER),
				new MonitorItem( "JBOSS-WEB", "JBOSS-WEB-7", "总会话数", "个", "总会话数", MonitorItemType.NUMBER) };
	}

	@Override
	public MonitorMethodType[] getMethodTypes() {
		return new MonitorMethodType[] { new MonitorMethodType(JbossJMXOption.TYPE_ID, "Jboss监测方法", JbossJMXPanel.class) };
	}

}
