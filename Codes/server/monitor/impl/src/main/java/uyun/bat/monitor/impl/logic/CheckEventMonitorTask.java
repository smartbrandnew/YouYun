package uyun.bat.monitor.impl.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.event.api.entity.Event;
import uyun.bat.monitor.api.common.util.PeriodUtil;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.core.entity.CheckContext;
import uyun.bat.monitor.core.entity.Symbol;
import uyun.bat.monitor.core.logic.CheckController;
import uyun.bat.monitor.core.logic.EventMonitor;
import uyun.bat.monitor.core.util.MonitorQueryUtil;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.common.DistributedUtil;
import uyun.bat.monitor.impl.common.ServiceManager;

public class CheckEventMonitorTask {

	private static final Logger logger = LoggerFactory.getLogger(CheckEventMonitorTask.class);

	private static int corePoolSize = 3;
	// 暂时设置一分钟执行一次
	private static long period = 60;

	public void init() {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
		service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!DistributedUtil.isLeader()){
					return;
				}
				try {
					checkEventMonitor();
				} catch (Exception e) {
					logger.warn("Monitor scheduled task execution exception：" + e.getMessage());
				}
			}

		}, 60, period, TimeUnit.SECONDS);
	}

	private void checkEventMonitor() {
		List<EventMonitor> eventMonitors = MonitorQueryUtil.getTriggeredEventMonitor();
		if (eventMonitors.isEmpty()) {
			return;
		}

		for (EventMonitor eventMonitor : eventMonitors) {
			Symbol symbol = eventMonitor.generateSymbol(null, null);
			String[] tags = symbol.generateTags();
			String state = symbol.generateState();
			Checkpoint[] checkpoints = ServiceManager.getInstance().getStateService().getCheckpoints(state, tags);
			if (null == checkpoints || checkpoints.length < 1) {
				continue;
			}
			String period = MonitorQueryUtil.getRecoversByTime(eventMonitor.getMonitor().getOptions().getEventRecover());
			boolean periodFlag = PeriodUtil.check(period);
			if (!periodFlag) {
				continue;
			}

			MonitorState monitorState = MonitorState.OK;
			PeriodUtil.Period pp = PeriodUtil.generatePeriod(period);
			List<Checkpoint> checkpointList = new ArrayList<Checkpoint>();
			for (Checkpoint checkpoint : checkpoints) {
				if (checkpoint.getValue().equals(MonitorState.ERROR)) {
					monitorState = MonitorState.ERROR;
				}
				if (pp.getStart() > checkpoint.getTimestamp()) {
					checkpoint.setValue(MonitorState.OK.getCode());
					checkpointList.add(checkpoint);
				}
			}

			checkUpdateMonitorStatus(eventMonitor.getMonitor(), monitorState);
			checkTrigger(checkpointList, eventMonitor, monitorState);
		}

	}

	private void checkUpdateMonitorStatus(Monitor monitor, MonitorState monitorState) {
		if (monitor.getMonitorState().equals(MonitorState.ERROR) && monitorState.equals(MonitorState.OK)) {
			monitor.setMonitorState(monitorState);
			LogicManager.getInstance().getMonitorLogic().updateMonitorState(monitor);
		}
	}

	private void checkTrigger(List<Checkpoint> checkpointList, EventMonitor eventMonitor, MonitorState monitorState) {
		Monitor monitor = eventMonitor.getMonitor();
		for (Checkpoint checkpoint : checkpointList) {
			String resId = "";
			if (null != checkpoint.getTags() && checkpoint.getTags().length >= 2) {
				resId = eventMonitor.getResIdByTags(checkpoint.getTags());
			}
			Resource res = ServiceManager.getInstance().getResourceService().queryResById(resId, monitor.getTenantId());
			Event event = eventMonitor.generateEvent(monitorState,res);
			CheckContext context = new CheckContext(event, resId, monitorState, eventMonitor.getEventMonitorParam());
			boolean trigger = CheckController.getInstance().trigger(monitor, context,
					eventMonitor.generateSymbol(resId, MonitorState.OK));
			if (trigger && monitor.getNotify() && monitor.getNotifyUserIdList() != null
					&& monitor.getNotifyUserIdList().size() > 0)
				CheckController.getInstance().notify(context, monitor.getNotifyUserIdList());
		}
	}

	public static void main(String[] args) {
		Startup.getInstance().startup();
		CheckEventMonitorTask task = new CheckEventMonitorTask();
		task.checkEventMonitor();
	}
}