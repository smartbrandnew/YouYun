package uyun.bat.web.api.monitor.service;

import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.web.api.monitor.entity.MineMonitor;
import uyun.bat.web.api.monitor.entity.MineMonitorHost;
import uyun.bat.web.api.monitor.entity.MineNotifyRecord;
import uyun.bat.web.api.monitor.entity.MonitorParam;
import uyun.bat.web.api.monitor.entity.PaginationUser;
import uyun.bat.web.api.monitor.entity.SingleMonitor;

import java.util.List;

public interface MonitorWebService {

	/**
	 * 通过过滤条件分页查询监测器列表
	 * 
	 * @param tenantId
	 * @param pageSize
	 * @param currentPage
	 * @param filertValue
	 * @param fileterType
	 * @return
	 */
	MineMonitor getMonitorsByFilter(String tenantId, String pageSize, String currentPage, String filertValue,
			String fileterType);

	/**
	 * 通过监测器ID获取监测器
	 * 
	 * @param id
	 * @return
	 */
	SingleMonitor getMonitorById(String tenantId, String id);

	/**
	 * 创建监测器
	 * 
	 * @param tenantId
	 * @param userId
	 * @param mParam
	 */
	void createMonitor(String tenantId, String userId, MonitorParam mParam);

	/**
	 * 更新监测器
	 * 
	 * @param mParam
	 * @return
	 */
	void updateMonitor(String tenantId, MonitorParam mParam);

	/**
	 * 删除监测器
	 * 
	 * @param id
	 * @return
	 */
	void deleteMonitor(String tenantId, Monitor monitor);

	/**
	 * 启动/停止监测器
	 * 
	 * @param id
	 */
	void runStatus(String tenantId, Monitor monitor);

	List<Integer> getCount(String tenantId);

	/**
	 * 分页模糊查询租户用户信息
	 * @param tenantId
	 * @param realname
	 * @param userIdList 用户已经选择的id列表
	 * @return
	 */
	PaginationUser getUser(String tenantId, String realname, Integer currentPage, Integer pageSize, List<String> userIdList);

	MineNotifyRecord getNotifyRecordList(String tenantId, String monitorId, int currentPage, int pageSize,
			String timeRange);

	boolean isMonitorExist(String tenantId);

	MineMonitorHost getMonitorHostsByFilter(String tenantId, String monitorId, String tagName, String timeRange, int currentPage, int pageSize);
}
