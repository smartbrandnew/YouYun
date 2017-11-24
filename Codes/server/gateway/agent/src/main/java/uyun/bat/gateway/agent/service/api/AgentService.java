package uyun.bat.gateway.agent.service.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import uyun.bat.gateway.agent.entity.*;
import uyun.bat.gateway.agent.entity.newentity.*;

public interface AgentService {

	/**
	 * 插入性能指标
	 * 
	 * @param metrics
	 * @param request
	 */
	void intakePerfMetric(List<PerfMetricVO1> metrics, HttpServletRequest request);

	/**
	 * 插入事件
	 * 
	 * @param events
	 * @param request
	 */
	void intakeEvent(List<EventVO1> events, HttpServletRequest request);

	/**
	 * 插入设备
	 * 
	 * @param hosts
	 * @param request
	 */
	void intakeHost(List<HostVO> hosts, HttpServletRequest request);

	/**
	 * 通过过滤条件(事件标题、内容、tag、事件等级)分页返回事件列表
	 *
	 * @param request
	 * @param page_index
	 * @param page_size
	 * @param search_value
	 * @param severity
	 * @param begin_time
	 * @param end_time
	 * @return
	 */
	EventList1 getEvents(HttpServletRequest request, int page_index, int page_size, String search_value, String severity,
			long begin_time, long end_time);

	/**
	 * 通过设备Id返回设备的基本信息和详细信息
	 * 
	 * @param request
	 * @param id
	 * @return
	 */
	HostVO getHostById(HttpServletRequest request, String id);

	/**
	 * 通过过滤条件分页返回设备列表
	 * 
	 * @param request
	 * @param current
	 * @param pageSize
	 * @param ip
	 * @param name
	 * @param type
	 * @param tags
	 * @param apps
	 * @param min_update_time
	 * @return
	 */
	HostList1 getPageHosts(HttpServletRequest request, int current, int pageSize, String ip, String name, String type,
			List<String> tags, List<String> apps, String min_update_time);

	/**
	 * 通过过滤条件返回全部设备
	 *
	 * @param request
	 * @param ip
	 * @param name
	 * @param type
	 * @param tags
	 * @param apps
	 * @param min_update_time
	 * @return
	 */
	List<HostVO> getHosts(HttpServletRequest request, String ip, String name, String type, List<String> tags,
			List<String> apps, String min_update_time);

	/**
	 * 通过查询条件返回性能指标的值
	 * 
	 * @param param
	 * @param request
	 * @return
	 */
	List<Series> getPerfMetricList(SeriesRequestParam1 param, HttpServletRequest request);

	/**
	 * 查询指定设备的某个指标的快照
	 * 
	 * @param request
	 * @param id
	 * @param metricName
	 * @param groupBy
	 * @return
	 */
	List<MetricSnapshoot> getMetricSnapshoot(HttpServletRequest request, String id, String metricName, String groupBy);

	/**
	 * 插入状态指标
	 * 
	 * @param checkpoints
	 * @param request
	 */
	void intakeCheckPoints(List<CheckpointVO> checkpoints, HttpServletRequest request);

	/**
	 * 查询指定设备的某个状态指标的快照
	 * 
	 * @param request
	 * @param id
	 * @param state
	 * @return
	 */
	List<StateSnapshoot> getStateSnapshoot(HttpServletRequest request, String id, String state);

	/**
	 * 查询指定设备的状态指标历史数据
	 * 
	 * @param request
	 * @param state
	 * @param id
	 * @param tags
	 * @param firstTime
	 * @param lastTime
	 * @return
	 */

	List<CheckPointRecord> getStateHistory(HttpServletRequest request, String state, String id, String[] tags,
			long firstTime, long lastTime);

	/**
	 * 批量插入指标元数据
	 *
	 * @param metricMetaDatas
	 * @param request
	 * @return
	 */
	void intakeMetricMetaData(List<MetricMetaDataVO> metricMetaDatas, HttpServletRequest request);

	void updateResourceUserTag(ResourcesTags resourcesTags, HttpServletRequest request);

	DataValue getSingleValueList(SingleValueRequestParam param, HttpServletRequest request);

	/******************************************************************************
	 * 以上是按新规范修改后的接口 以下是旧接口后续会删除
	 ******************************************************************************/

	void oldIntakePerfMetric(List<PerfMetricVO> metrics, HttpServletRequest request);

	void oldIntakeEvent(List<EventVO> events, HttpServletRequest request);

	void oldIntakeHost(List<HostVO> hosts, HttpServletRequest request);

	EventList oldGetEvents(HttpServletRequest request, int current, int pageSize, String searchValue, String serverity,
			long beginTime, long endTime);

	HostVO oldGetHostById(HttpServletRequest request, String id);

	HostList oldGetHosts(HttpServletRequest request, int current, int pageSize, BatchHostRequestParam param);

	List<Series> oldGetPerfMetricList(SeriesRequestParam param, HttpServletRequest request);

	List<MetricSnapshoot> oldGetMetricSnapshoot(HttpServletRequest request, String id, String metricName, String groupBy);

	void oldIntakeCheckPoints(List<CheckpointVO> checkpoints, HttpServletRequest request);

	List<StateSnapshoot> oldGetStateSnapshoot(HttpServletRequest request, String id, String state);

	List<CheckPointRecord> oldGetStateHistory(HttpServletRequest request, String state, String id, String[] tags,
			long firstTime, long lastTime);

}
