package com.broada.carrier.monitor.demo.jvm;

import java.io.Serializable;

import com.broada.carrier.monitor.impl.common.SingleInstanceConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * 一个JVM监测器演示类
 * 
 * @author Jiangjw
 */
public class JVMMonitor2 extends JVMMonitor {
	public static final MonitorType TYPE = new MonitorType(
			JVMMonitor.class.getSimpleName(), // 监测器ID，与其它监测器不重复即可
			"JVM监测", // 监测器名称
			"演示监测探针JVM", // 监测器说明
			SingleInstanceConfiger.class.getName(), // 监测器配置界面类名，由于本监测器无监测实例也没有特殊配置要求，所以直接使用公共的SingleInstanceConfiger
			JVMMonitor2.class.getName(), // 监测器实现类名
			1, // 监测器排序号，暂时无用，默认使用监测器ID排序
			new String[] { "Computer" }, // 监测器适用的监测项类型，可支持数组
			null // 监测器需要的监测方法类型，本监测器不需要
	);
	public static final MonitorItem MI_HEAP_USED = JVMMonitor.MI_HEAP_USED;
	public static final MonitorItem MI_HOME = JVMMonitor.MI_HOME;
	public static final MonitorItem MI_OS = new MonitorItem("osName", "系统名称", "", null, MonitorItemType.TEXT);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult mr = (MonitorResult) super.collect(context);
		mr.addPerfResult(new PerfResult(MI_OS.getCode(), System.getProperty("os.name")));
		return mr;
	}

}
