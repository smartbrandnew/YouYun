package uyun.bat.web.api.event.service;

import uyun.bat.web.api.event.envity.EventGraphDataVO;
import uyun.bat.web.api.event.envity.MineEvent;

public interface EventWebService {

	/**
	 * 根据条件查询事件分页数据
	 *
	 * @param tenantId
	 * @param current
	 * @param pageSize
	 * @param searchValue
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	MineEvent searchEvent(String tenantId, int current, int pageSize, String searchValue, String serverity,
						  long beginTime, long endTime, int granularity);

	/**
	 * 根据条件查询柱状图数据
	 *
	 * @param tenantId
	 * @param searchValue
	 * @param beginTime
	 * @param endTime
	 * @param granularity
	 * @return
	 */
	EventGraphDataVO searchEventGraphData(String tenantId, String searchValue, long beginTime, long endTime,
										  int granularity);

	/**
	 * 根据故障获取事件
	 * @param tenantId
	 * @param eventId
	 * @param faultId
	 * @param current
	 * @param pageSize
     * @return
     */
	MineEvent getEventsByFaultId(String tenantId, String eventId, String faultId, int current, int pageSize);

	/**
	 * 获取最新的事件数
	 * @param tenantId
	 * @param beginTime
     * @return
     */
	int getEventCount(String tenantId,long beginTime);
}
