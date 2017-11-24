package uyun.bat.datastore.logic.redis;

public class RedisKeyRule {
	//指标redis key 缓存前缀
	private static final String metric_prefix = "bat-metric";
	//指标--资源redis key缓存前缀
	private static final String metric_resource_prefix = "bat-metric-resource";
	//资源redis key缓存前缀
	protected static final String resource_prefix = "bat-resource";
	//资源统计数redis key缓存前缀
	private static final String resource_count_prefix = "bat-res-count";
	//待异步更新到mysql的资源 ID队列 redis key缓存名称
	private static final String resource_async_name = "bat-res-async-update";
	
	private static final String metricname_async_name="bat-metricname-async-update";

	//状态指标--资源redis key缓存前缀
	private static final String state_metric_resource_prefix="bat-state-metric-resource";
	//资源ID队列待更新到状态指标资源表  redis key缓存名称
	private static final String state_metricname_async_name="bat-state-metricname-async-update";


	/**
	 * 转换resource key
	 * @param key
	 * @return
	 */
	public static String encodeResKey(String key) {
		if (key != null)
			return resource_prefix + key;
		return null;
	}

	/**
	 * 转换metric key
	 * @param key
	 * @return
	 */
	public static String encodeMetricKey(String key) {
		if (key != null)
			return metric_prefix + key;
		return null;
	}

	/**
	 * 转换metric-resource key
	 * @param key
	 * @return
	 */
	public static String encodeMetricResKey(String key) {
		if (key != null)
			return metric_resource_prefix + key;
		return null;
	}

	/**
	 * 转换resource-count key
	 * @param key
	 * @return
	 */
	public static String encodeResCountKey(String key) {
		if (key != null)
			return resource_count_prefix + key;
		return null;
	}

	public static String getResAsyncKey(){
		return resource_async_name;
	}

	public static String getMetricNamesAsyncKey(){
		return metricname_async_name;
	}

	public static String encodeStateMetricResKey(String key) {
		if (key != null)
			return state_metric_resource_prefix + key;
		return null;
	}

	public static String getStateMetricNamesAsyncKey(){
		return state_metricname_async_name;
	}
}
