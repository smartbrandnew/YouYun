package uyun.bat.monitor.impl.dao;

import java.util.Date;
import java.util.List;

import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorCount;
import uyun.bat.monitor.api.entity.MonitorCountVO;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.entity.SimpleMonitorQuery;

public interface MonitorDao {

	List<Monitor> getMonitorsByFilter(SimpleMonitorQuery query);

	List<Monitor> getMonitorList(String tenantId);

	Monitor getMonitorById(String tenantId, String monitorId);

	int createMonitor(Monitor monitor);

	int updateMonitor(Monitor monitor);

	int deleteMonitor(String tenantId, String monitorId);

	List<MonitorCount> getCount(String tenantId);

	List<String> getIdListByTenantId(String tenantId);
	/**
	 * 以下方法监测器触发调用
	 */
	/***************************************************************/
	/**
	 * 
	 * @param tenantId 租户id
	 * @param type 监测器类型
	 * @return
	 */
	List<Monitor> getMonitors(String tenantId, MonitorType type);

	/**
	 * 更新监测器状态
	 * 
	 * @param monitor
	 * @return
	 */
	int updateMonitorStatus(Monitor monitor);

	/**
	 * 若分成多个数据库，则该方法需要改进
	 */
	List<Monitor> getCheckEventMonitors(MonitorType type, MonitorState state, String option);

	/***************************************************************/
	List<MonitorCountVO> getMonitorCountByDate(Date startTime, Date endTime);

	List<MonitorCountVO> getMonitorCount();

	int getCountByTenantId(String tenantId);
}
