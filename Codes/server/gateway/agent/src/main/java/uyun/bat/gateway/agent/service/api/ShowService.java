package uyun.bat.gateway.agent.service.api;

import uyun.bat.gateway.agent.entity.showentity.HostHealth;
import uyun.bat.gateway.agent.entity.showentity.ShowList;

import javax.servlet.http.HttpServletRequest;

/**
 * 给show团队的openapi接口
 */
public interface ShowService {

	/**
	 * 返回租户监网络设备和计算机设备的健康状态
	 *
	 * @param request
	 * @return
	 */
	HostHealth getHostsHealth(HttpServletRequest request);
	/**
	 * 返回设备类型的总数和告警资源数
	 *
	 * @param request
	 * @return
	 */
	ShowList getHostsAppsCount(HttpServletRequest request);

	/**
	 * 返回某个节点的存储使用情况 默认app可选ibm_ds和ibm_svc，如果没有则采用模拟数据
	 *
	 * @param request
	 * @param ip
	 * @param app
	 * @return
	 */
	HostHealth queryStoreUsage(HttpServletRequest request, String ip, String app);

	/**
	 * 返回网络设备延时排名
	 *
	 * @param request
	 * @param metric
	 * @param group
	 * @return
	 */
	ShowList queryTopN(HttpServletRequest request, String metric, String group);

	/**
	 * 给show返回节点存储7天的使用率 默认app可选ibm_ds和ibm_svc，如果没有则采用模拟数据
	 *
	 * @param request
	 * @param ip
	 * @param app
	 * @return
	 */
	ShowList queryHostStoreUsage(HttpServletRequest request, String ip, String app);
}
