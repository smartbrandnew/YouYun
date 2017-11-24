package uyun.bat.monitor.impl.facade;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorCount;
import uyun.bat.monitor.api.entity.MonitorCountVO;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.entity.PageMonitor;
import uyun.bat.monitor.api.entity.PageNotifyRecord;
import uyun.bat.monitor.core.calculate.CalculatorManager;
import uyun.bat.monitor.impl.logic.LogicManager;

public class MonitorFacade {
	private static final Logger logger = LoggerFactory.getLogger(MonitorFacade.class);

	public PageMonitor getMonitorsByFilter(String tenantId, int currentPage, int pageSize, String filertValue,
			List<MonitorState> filterType, Boolean enable) {
		return (PageMonitor) LogicManager.getInstance().getMonitorLogic()
				.getMonitorsByFilter(tenantId, currentPage, pageSize, filertValue, filterType, enable);
	}

	public List<Monitor> getMonitorList(String tenantId) {
		return LogicManager.getInstance().getMonitorLogic().getMonitorList(tenantId);
	}

	public Monitor getMonitorById(String tenantId, String monitorId) {
		return LogicManager.getInstance().getMonitorLogic().getMonitorById(tenantId, monitorId);
	}

	public Monitor createMonitor(Monitor monitor) {
		Monitor result = LogicManager.getInstance().getMonitorLogic().createMonitor(monitor);

		LogicManager.getInstance().getMonitorLogic()._onMonitorListChange(monitor.getTenantId(), monitor.getMonitorType());

		MonitorEventCreator.onMonitorCreate(monitor);

		return result;
	}

	public Monitor updateMonitor(Monitor newData) {
		Monitor old = LogicManager.getInstance().getMonitorLogic().getMonitorById(newData.getTenantId(), newData.getId());
		if (old == null)
			return null;
		boolean triggered = false;
		// 如果所有的监测器类型自愈内容有变化
		if (old.getAutoRecoveryParams() != null && newData.getAutoRecoveryParams() != null
				&& !old.getAutoRecoveryParams().equals(newData.getAutoRecoveryParams())) {
			triggered = true;
		} else if (MonitorType.METRIC.equals(old.getMonitorType())) {
			// 如果查询条件或阈值变化，则需要删除记录
			if (null != newData.getQuery()
					&& (!old.getQuery().equals(newData.getQuery()) || !old.getOptions().getThresholds()
							.equals(newData.getOptions().getThresholds()))) {
				triggered = true;
			}
		} else if (MonitorType.EVENT.equals(old.getMonitorType())) {
			// 事件的查询条件及恢复条件不变
			if (null != newData.getQuery() && !old.getQuery().equals(newData.getQuery())) {
				triggered = true;
			}
		} else if (MonitorType.HOST.equals(old.getMonitorType())) {
			if (null != newData.getQuery()
					&& (!old.getQuery().equals(newData.getQuery()) || !old.getOptions().getThresholds()
							.equals(newData.getOptions().getThresholds()))) {
				triggered = true;
			}
		} else if (null != newData.getQuery() && MonitorType.APP.equals(old.getMonitorType())) {

			if (!old.getQuery().equals(newData.getQuery())
					|| !old.getOptions().getThresholds().equals(newData.getOptions().getThresholds())) {
				triggered = true;
			}
		}
		// 如果监测器触发条件变更，则将监测器状态还原
		// TODO 当监测器正在进行阈值判断时，修改了监测器的阈值条件，那监测器的状态估计还是会被此次阈值判断完后的逻辑覆盖
		if (triggered)
			newData.setMonitorState(MonitorState.OK);
		
		Monitor result = LogicManager.getInstance().getMonitorLogic().updateMonitor(newData);
		if (triggered) {
			LogicManager.getInstance().getMonitorLogic()._onMonitorListChange(old.getTenantId(), old.getMonitorType());

			CalculatorManager.getInstance().onMonitorChange(old.getTenantId(), old.getId(), old.getMonitorType());

			// 静默和恢复暂不发送事件，好像目前只能通过这种方式判断
			if (null != newData.getQuery()) {
				MonitorEventCreator.onTriggerUpdate(old, newData);
			}
		}

		if (newData.getEnable() != old.getEnable()) {
			LogicManager.getInstance().getMonitorLogic()._onMonitorListChange(old.getTenantId(), old.getMonitorType());

			MonitorEventCreator.onMonitorSilence(old, newData);
		}

		return result;
	}

	public void deleteMonitor(Monitor monitor) {
		Monitor old = LogicManager.getInstance().getMonitorLogic().getMonitorById(monitor.getTenantId(), monitor.getId());
		if (old == null)
			return;
		LogicManager.getInstance().getMonitorLogic().deleteMonitor(monitor);
		LogicManager.getInstance().getMonitorLogic()._onMonitorListChange(old.getTenantId(), old.getMonitorType());

		// 删除监测器后，删除监测器通知记录
		try {
			LogicManager.getInstance().getNotifyRecordLogic().deleteByMonitorId(old.getTenantId(), old.getId());
		} catch (Throwable e) {
			// 照顾单元测试找不到外来服务
			if (logger.isWarnEnabled()) {
				logger.warn(e.getMessage());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Stack:", e);
			}
		}
		CalculatorManager.getInstance().onMonitorChange(old.getTenantId(), old.getId(), old.getMonitorType());

		MonitorEventCreator.onMonitorDelete(old);
	}

	public List<MonitorCount> getCount(String tenantId) {
		return LogicManager.getInstance().getMonitorLogic().getCount(tenantId);
	}

	public PageNotifyRecord getNotifyRecordList(String tenantId, String monitorId, int currentPage, int pageSize,
			String timeRange) {
		return LogicManager.getInstance().getNotifyRecordLogic()
				.getNotifyRecordList(tenantId, monitorId, currentPage, pageSize, timeRange);
	}

	public List<MonitorCountVO> getMonitorCountByDate(Date startTime, Date endTime) {
		return LogicManager.getInstance().getMonitorLogic().getMonitorCountByDate(startTime, endTime);
	}

	public List<MonitorCountVO> getMonitorCount() {
		return LogicManager.getInstance().getMonitorLogic().getMonitorCount();
	}

	public void deleteAutoRecoverRecordByMonitorId(String tenantId, String monitorId) {
		LogicManager.getInstance().getAutoRecoverRecordLogic().deleteByMonitorId(tenantId, monitorId);
	}

	public List<String> getIdListByTenantId(String tenantId) {
		return LogicManager.getInstance().getMonitorLogic().getIdListByTenantId(tenantId);
	}
}