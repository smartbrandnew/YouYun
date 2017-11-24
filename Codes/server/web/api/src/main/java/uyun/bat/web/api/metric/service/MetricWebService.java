package uyun.bat.web.api.metric.service;

import java.util.List;

import uyun.bat.web.api.metric.entity.*;
import uyun.bat.web.api.metric.request.BatchRequestParams;
import uyun.bat.web.api.metric.request.SingleValueRequestParams;

public interface MetricWebService {

	/**
	 * @param requestParams
	 * @return
	 */
	BatchSeries[] getSeries(String tenantId, BatchRequestParams requestParams);

	/**
	 * @param requestParams
	 * @return
	 */
	Top[] getTop(String tenantId, BatchRequestParams requestParams);

	/**
	 * @param singleValueRequestParams
	 * @return
	 */
	Value getValue(String tenantId, SingleValueRequestParams singleValueRequestParams);

	List<MetricMetaVO> getMetricNames(String tenantId, String metricName, String ranged);

	List<String> getTagsByMetricName(String tenantId, String metricName, String[] q);

	boolean deleteTrashData(MetricTrashCleanQuery query);

	MetaData getMetricMetaData(String metricName);

	/**
	 * 判断用户是否上传过指标
	 * 
	 * @param tenantId
	 * @return
	 */
	boolean isMetricExist(String tenantId);

	List<MetricDataVO> getMetricMetaDataByKey(String key);
}
