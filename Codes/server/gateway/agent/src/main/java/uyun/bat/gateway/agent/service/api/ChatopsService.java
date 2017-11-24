package uyun.bat.gateway.agent.service.api;

import java.io.InputStream;
import java.util.List;

import uyun.bat.datastore.api.serviceapi.entity.ServiceApiResMetrics;
import uyun.bat.gateway.agent.entity.DataValue;
import uyun.bat.gateway.agent.entity.chatopsentity.ChatOpsHostList;

public interface ChatopsService {

	/**
	 * @param tenantId 租户ID
	 * @param hostname 主机名称
	 * @param ipaddr IP地址
	 * @param tags 标签列表，多个标签用分号;分开
	 * @return
	 */
	List<ServiceApiResMetrics> getMetricNames(String tenantId, String hostname, String ipaddr, String tags);

	/**
	 * 查询最近半小时最后一个点
	 *
	 * @param tenantId
	 * @param metricName
	 * @param tags
	 * @return
	 */
	DataValue getCurrentMetric(String tenantId, String metricName, String tags);

	/**
	 * 询时间跨度范围内指标数据点图表展现
	 *
	 * @param tenantId
	 * @param metricName
	 * @param tags
	 * @param from
	 * @param to
	 * @return
	 */
	InputStream getSeriesMetricPic(String tenantId, String metricName, String tags, long from, long to);

	/**
	 * 通过ip或name过滤资源信息
	 *
	 * @param page_index
	 * @param page_size
	 * @param ip
	 * @param name
	 * @return
	 */
	ChatOpsHostList getResListByCondition(String tenantId, int page_index, int page_size, String ip, String name);

}
