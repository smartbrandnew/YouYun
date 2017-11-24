package uyun.bat.datastore.mq;


public abstract class MQManager {
	private static MQManager instance = new MQManager() {
	};

	public static MQManager getInstance() {
		return instance;
	}
	
	private MetricMQService metricMQService;

	public MetricMQService getMetricMQService() {
		return metricMQService;
	}

	public void setMetricMQService(MetricMQService metricMQService) {
		this.metricMQService = metricMQService;
	}
	
}
