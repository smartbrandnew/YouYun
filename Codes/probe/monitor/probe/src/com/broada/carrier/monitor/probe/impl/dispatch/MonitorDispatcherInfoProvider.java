package com.broada.carrier.monitor.probe.impl.dispatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.runcheck.RuntimeInfoEntry;
import com.broada.component.utils.runcheck.RuntimeInfoProvider;
import com.broada.component.utils.text.Unit;

public class MonitorDispatcherInfoProvider implements RuntimeInfoProvider {
	private static final Logger logger = LoggerFactory.getLogger(MonitorDispatcherInfoProvider.class);
	
	@Override
	public String getName() {
		return "监测调度";
	}

	@Override
	public RuntimeInfoEntry[] getRuntimeInfo() {	
		List<RuntimeInfoEntry> entires = new ArrayList<RuntimeInfoEntry>();
		
		try {
			entires.add(new RuntimeInfoEntry("监测次数", MonitorDispatcher.getDefault().getMonitorExecuteCount()));
			entires.add(new RuntimeInfoEntry("监测延误率（%）", MonitorDispatcher.getDefault().getMonitorDelayRate()));
			entires.add(new RuntimeInfoEntry("队列长度", MonitorDispatcher.getDefault().getQueueSize()));
			if (MonitorDispatcher.getDefault().monitorPipeline != null) {
				entires.add(new RuntimeInfoEntry("活动线程", MonitorDispatcher.getDefault().monitorPipeline.getAliveNum()));
				entires.add(new RuntimeInfoEntry("僵死线程", MonitorDispatcher.getDefault().monitorPipeline.getDeadNum()));
			}
			if (MonitorResultUploader.getDefault().getServerTimeOffset() != 0)
				entires.add(new RuntimeInfoEntry("时间差距", Unit.ms.formatPrefer(0 - MonitorResultUploader.getDefault().getServerTimeOffset())));
			
			if (MonitorDispatcher.getDefault().typeCounter != null) {
				MonitorDispatcher.TimeCounter[] counters = MonitorDispatcher.getDefault().typeCounter.values().toArray(
						new MonitorDispatcher.TimeCounter[MonitorDispatcher.getDefault().typeCounter.size()]);
				Arrays.sort(counters);
				for (int i = 0; i < 5 && i < counters.length; i++) {
					MonitorDispatcher.TimeCounter counter = counters[i]; 
					entires.add(new RuntimeInfoEntry("耗时监测类型前" + (i + 1), counter.toString()));
				}
				
				counters = MonitorDispatcher.getDefault().taskCounter.values().toArray(
						new MonitorDispatcher.TimeCounter[MonitorDispatcher.getDefault().taskCounter.size()]);
				Arrays.sort(counters);
				for (int i = 0; i < 5 && i < counters.length; i++) {
					MonitorDispatcher.TimeCounter counter = counters[i]; 
					entires.add(new RuntimeInfoEntry("耗时监测任务前" + (i + 1), counter.toString()));
				}
			}
		} catch (Throwable e) {
			ErrorUtil.warn(logger, "获取监测框架信息失败", e);
		}
						
		return entires.toArray(new RuntimeInfoEntry[entires.size()]);
	}
	
}
