package uyun.bat.datastore.logic;

import uyun.bat.datastore.logic.redis.MetricRedisService;
import uyun.bat.datastore.logic.redis.ResourceRedisService;
import uyun.bat.datastore.logic.redis.StateMetricRedisService;
import uyun.bat.datastore.util.MetricTrashCleaner;

public class LogicManager {
	private static LogicManager instance = new LogicManager();
	private MetricLogic metricLogic;
	private MetricRedisService metricRedisService;
	private ResourceRedisService resourceRedisService;
	private ResourceLogic resourceLogic;
	private MetricTrashCleaner metricClean;
	private StateMetricLogic stateMetricLogic;
	private StateMetricRedisService stateMetricRedisService;

	public static LogicManager getInstance() {
		return instance;
	}

	public StateMetricRedisService getStateMetricRedisService() {
		return stateMetricRedisService;
	}

	public void setStateMetricRedisService(StateMetricRedisService stateMetricRedisService) {
		this.stateMetricRedisService = stateMetricRedisService;
	}

	public StateMetricLogic getStateMetricLogic() {
		return stateMetricLogic;
	}

	public void setStateMetricLogic(StateMetricLogic stateMetricLogic) {
		this.stateMetricLogic = stateMetricLogic;
	}

	public MetricLogic getMetricLogic() {
		return metricLogic;
	}

	public void setMetricLogic(MetricLogic metricLogic) {
		this.metricLogic = metricLogic;
	}

	public MetricRedisService getMetricRedisService() {
		return metricRedisService;
	}

	public void setMetricRedisService(MetricRedisService metricRedisService) {
		this.metricRedisService = metricRedisService;
	}

	public ResourceRedisService getResourceRedisService() {
		return resourceRedisService;
	}

	public void setResourceRedisService(ResourceRedisService resourceRedisService) {
		this.resourceRedisService = resourceRedisService;
	}

	public ResourceLogic getResourceLogic() {
		return resourceLogic;
	}

	public void setResourceLogic(ResourceLogic resourceLogic) {
		this.resourceLogic = resourceLogic;
	}

	public MetricTrashCleaner getMetricClean() {
		return metricClean;
	}

	public void setMetricClean(MetricTrashCleaner metricClean) {
		this.metricClean = metricClean;
	}

}
