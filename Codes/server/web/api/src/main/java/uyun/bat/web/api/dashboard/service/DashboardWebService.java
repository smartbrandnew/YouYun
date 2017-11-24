package uyun.bat.web.api.dashboard.service;

import java.util.List;

import javax.ws.rs.CookieParam;
import javax.ws.rs.QueryParam;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.web.api.dashboard.entity.MineDashboard;
import uyun.bat.web.api.dashboard.entity.RichDashboardMetadata;

public interface DashboardWebService {

	/**
	 * 通过用户id和查询条件获取仪表盘列表
	 * 
	 * @param userId 用户ID
	 * @param tenantId 租户ID
	 * @param name 查询条件
	 * @param limit 默认返回几条数据
	 * @return RichDashboardMetadata 简化的仪表盘信息
	 */
	List<RichDashboardMetadata> searchDashboardByName(String userId, String tenantId, String name, int limit);

	/**
	 * 通过仪表盘ID获取相应的仪表盘信息
	 * 
	 * @param id 仪表盘ID
	 * @return MineDashboard
	 */
	MineDashboard getDashboardById(String userId, String id);

	/**
	 * 根据用户ID创建仪表盘
	 * 
	 * @param userId 用户ID
	 * @param tenantId 租户ID
	 * @param dashboard 仪表盘
	 * @return Dashboard 完整的仪表盘信息
	 */
	Dashboard createDashboard(String userId, String tenantId, Dashboard dashboard);

	/**
	 * 更新仪表盘
	 * 
	 * @param tenantId
	 * @param dashboard
	 * @return
	 */
	Dashboard updateDashboard(String tenantId, Dashboard dashboard);

	/**
	 * 删除仪表盘
	 * 
	 * @param userId
	 * @param tenantId
	 * @param dashboard
	 */
	void deleteDashboard(String userId, String tenantId, Dashboard dashboard);

	/**
	 * 判断用户是否有创建仪表盘
	 * @param tenantId
	 * @return
	 */
	boolean isDashboardExist(String tenantId);

	/**
	 * 仪表盘拷贝
	 * @param tenantId
	 * @return
	 */
	public Dashboard copyDashboard(String userId,String tenantId,String dashboardid,String dashboardName);

}
