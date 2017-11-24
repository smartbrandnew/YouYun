package uyun.bat.monitor.api.service;

import uyun.bat.monitor.api.entity.*;

import java.util.Date;
import java.util.List;

public interface MonitorService {

	/**
	 * 通过过滤条件获取监测器列表
	 */
	PageMonitor getMonitorsByFilter(String tenantId, int currentPage, int pageSize, String name, List<MonitorState> state,
			Boolean mute);

	/**
	 * 获取租户所有监测器列表
	 */
	List<Monitor> getMonitorList(String tenantId);

	Monitor getMonitorById(String tenantId, String monitorId);

	/**
	 * 需要完整的监测器数据,带用户，带租户
	 */
	Monitor createMonitor(Monitor monitor);

	/**
	 * 需要带租户
	 */
	Monitor updateMonitor(Monitor monitor);

	/**
	 * 需要带租户
	 */
	void deleteMonitor(Monitor monitor);

	List<MonitorCount> getCount(String tenantId);

	PageNotifyRecord getNotifyRecordList(String tenantId, String monitorId, int currentPage, int pageSize,
			String timeRange);

	void deleteAutoRecoverRecordByMonitorId(String tenantId, String monitorId);

	/********************************* 以下接口for门户 *******************************************/

	/**
	 * 查询某段时间内创建监测器的数量
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	List<MonitorCountVO> getMonitorCountByDate(Date startTime, Date endTime);

	/**
	 * 查询全部监测器数量
	 * 
	 * @return
	 */
	List<MonitorCountVO> getMonitorCount();

	/**
	 * 查询租户监测器id列表
	 *
	 * @param tenantId
	 * @return
	 */
	List<String> getIdListByTenantId(String tenantId);

	/**
	 * 对接 Alert 时，根据监测器类型生成对应的告警名称
	 *
	 * @param type
	 * @param monitor
	 * @return
	 */
	String getAlertNameByMonitorType(String type, Monitor monitor);
}
