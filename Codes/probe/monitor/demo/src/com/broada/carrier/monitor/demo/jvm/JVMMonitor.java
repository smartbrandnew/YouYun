package com.broada.carrier.monitor.demo.jvm;

import java.io.Serializable;

import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.common.util.Unit;
import com.broada.component.utils.runcheck.JvmInfoProvider;

/**
 * 一个JVM监测器演示类
 * 
 * @author Jiangjw
 */
public class JVMMonitor implements Monitor {
	public static final MonitorType TYPE = new MonitorType(
			JVMMonitor.class.getSimpleName(), // 监测器ID，与其它监测器不重复即可
			"JVM监测", // 监测器名称
			"演示监测探针JVM", // 监测器说明
			SingleInstanceConfiger.class.getName(), // 监测器配置界面类名，由于本监测器无监测实例也没有特殊配置要求，所以直接使用公共的SingleInstanceConfiger
			JVMMonitor.class.getName(), // 监测器实现类名
			1, // 监测器排序号，暂时无用，默认使用监测器ID排序
			new String[] { "Computer" }, // 监测器适用的监测项类型，可支持数组
			null // 监测器需要的监测方法类型，本监测器不需要
	);
	public static final MonitorItem MI_HEAP_USED = new MonitorItem("jvmHeapUsed", "JVM堆已使用大小", "MB", null,
			MonitorItemType.NUMBER);
	public static final MonitorItem MI_HOME = new MonitorItem("jvmHome", "JVM安装路径", "", null, MonitorItemType.TEXT);

	@Override
	public MonitorResult monitor(MonitorContext context) {
		// 大部份监测器的collect与monitor逻辑是一样的，所以这里直接调用即可
		return (MonitorResult) collect(new CollectContext(context));
	}

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult mr = new MonitorResult();
		mr.addPerfResult(new PerfResult(MI_HEAP_USED.getCode(), Unit.B.to(Unit.MB, JvmInfoProvider.getHeapUsed())));
		mr.addPerfResult(new PerfResult(MI_HOME.getCode(), System.getProperty("java.home")));
		return mr;
	}

}
