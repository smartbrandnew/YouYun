package uyun.bat.monitor.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import uyun.bat.monitor.api.entity.*;
import uyun.bat.monitor.api.service.MonitorService;
import uyun.bat.monitor.core.util.AlertNameStrategy;
import uyun.bat.monitor.impl.facade.FacadeManager;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service(protocol = "dubbo", timeout = 2000)
public class MonitorServiceImpl implements MonitorService {
	private static Pattern Q_PATTERN = Pattern.compile("^([^:]+:[^:]+\\{.+\\})|([^:]+:[^:]+\\{.+\\} by \\{.+\\})$");

	public PageMonitor getMonitorsByFilter(String tenantId, int currentPage, int pageSize, String filertValue,
			List<MonitorState> state, Boolean enable) {
		return FacadeManager.getInstance().getMonitorFacade()
				.getMonitorsByFilter(tenantId, currentPage, pageSize, filertValue, state, enable);
	}

	public List<Monitor> getMonitorList(String tenantId) {
		return FacadeManager.getInstance().getMonitorFacade().getMonitorList(tenantId);
	}

	public Monitor getMonitorById(String tenantId, String monitorId) {
		return FacadeManager.getInstance().getMonitorFacade().getMonitorById(tenantId, monitorId);
	}

	public Monitor createMonitor(Monitor monitor) {
		checkMonitor(monitor);
		if (monitor.getCreatorId() == null || monitor.getCreatorId().length() == 0)
			throw new IllegalArgumentException("Illegal monitor parameters: user who create the monitor id cannot be null");
		if (monitor.getModified() == null)
			throw new IllegalArgumentException("Illegal monitor parameter: the monitor modification date cannot be null");
		if (monitor.getQuery() == null || monitor.getQuery().length() == 0)
			throw new IllegalArgumentException("Illegal monitor parameter: the monitor query expression is empty:" + monitor.getQuery());
		// 指标监测器额外的校验
		if (MonitorType.METRIC.equals(monitor.getMonitorType())) {
			if (!Q_PATTERN.matcher(monitor.getQuery()).matches())
				throw new IllegalArgumentException("Illegal monitor parameter: the monitor query expression is illegal:" + monitor.getQuery());

			if (monitor.getOptions() == null || monitor.getOptions().getThresholds() == null
					|| monitor.getOptions().getThresholds().isEmpty())
				throw new IllegalArgumentException("Illegal monitor parameter: the metirc monitor threshold parameter is empty");
		}
		return FacadeManager.getInstance().getMonitorFacade().createMonitor(monitor);
	}

	private void checkMonitor(Monitor monitor) {
		if (monitor == null)
			throw new IllegalArgumentException("Illegal monitor parameter: the monitor cannot be Null");
		if (monitor.getId() == null || monitor.getId().length() == 0)
			throw new IllegalArgumentException("Illegal monitor parameter: the monitor id cannot be null");
		if (monitor.getTenantId() == null || monitor.getTenantId().length() == 0)
			throw new IllegalArgumentException("Illegal monitor parameters: the monitor tenant id cannot be null");
	}

	public Monitor updateMonitor(Monitor monitor) {
		checkMonitor(monitor);
		if (monitor.getModified() == null)
			throw new IllegalArgumentException("Illegal monitor parameter: the monitor modification date cannot be null");
		if (monitor.getQuery() != null) {
			if (MonitorType.METRIC.equals(monitor.getMonitorType()) && !Q_PATTERN.matcher(monitor.getQuery()).matches())
				throw new IllegalArgumentException("Illegal monitor parameter: the monitor query expression is illegal:" + monitor.getQuery());
		}
		return FacadeManager.getInstance().getMonitorFacade().updateMonitor(monitor);
	}

	public void deleteMonitor(Monitor monitor) {
		checkMonitor(monitor);
		FacadeManager.getInstance().getMonitorFacade().deleteMonitor(monitor);
	}

	public List<MonitorCount> getCount(String tenantId) {
		return FacadeManager.getInstance().getMonitorFacade().getCount(tenantId);
	}

	public PageNotifyRecord getNotifyRecordList(String tenantId, String monitorId, int currentPage, int pageSize,
			String timeRange) {
		return FacadeManager.getInstance().getMonitorFacade()
				.getNotifyRecordList(tenantId, monitorId, currentPage, pageSize, timeRange);
	}

	@Override
	public void deleteAutoRecoverRecordByMonitorId(String tenantId, String monitorId) {
		FacadeManager.getInstance().getMonitorFacade().deleteAutoRecoverRecordByMonitorId(tenantId, monitorId);
	}

	public List<MonitorCountVO> getMonitorCountByDate(Date startTime, Date endTime) {
		return FacadeManager.getInstance().getMonitorFacade().getMonitorCountByDate(startTime, endTime);
	}

	public List<MonitorCountVO> getMonitorCount() {
		return FacadeManager.getInstance().getMonitorFacade().getMonitorCount();
	}

	public List<String> getIdListByTenantId(String tenantId) {
		return FacadeManager.getInstance().getMonitorFacade().getIdListByTenantId(tenantId);
	}

	@Override
	public String getAlertNameByMonitorType(String type, Monitor monitor) {
		MonitorType monitorType = MonitorType.checkByCode(type);
		return AlertNameStrategy.getInstance().getFuncByMonitorType(monitorType).apply(monitor);
	}


}
