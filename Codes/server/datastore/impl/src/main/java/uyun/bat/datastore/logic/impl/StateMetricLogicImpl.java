package uyun.bat.datastore.logic.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.entity.StateMetric;
import uyun.bat.datastore.api.mq.StateMetricData;
import uyun.bat.datastore.api.mq.StateMetricInfo;
import uyun.bat.datastore.api.util.UUIDUtils;
import uyun.bat.datastore.dao.StateMetricResDao;
import uyun.bat.datastore.entity.StateMetricResource;
import uyun.bat.datastore.logic.DistributedUtil;
import uyun.bat.datastore.logic.LogicManager;
import uyun.bat.datastore.logic.StateMetricLogic;
import uyun.bat.datastore.mq.MQManager;
import uyun.bat.datastore.service.ServiceManager;

public class StateMetricLogicImpl implements StateMetricLogic {

	/**
	 * 未知ip
	 */
	public static final String UNKNOWN = "unknown";

	private static int corePoolSize = 3;
	// 设置1分钟同步一次
	private static long period = 60;

	private static final Logger logger = LoggerFactory.getLogger(StateMetricLogicImpl.class);

	@Autowired
	private StateMetricResDao stateMetricResDao;

	private void init() {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
		logger.info("batch update state_metric_resource thread start......");
		service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!DistributedUtil.isLeader())
					return;
				if (logger.isDebugEnabled())
					logger.debug("batch update state_metric_resource task start......");
				try {
					List<StateMetricResource> insert = new ArrayList<>();
					List<StateMetricResource> update = new ArrayList<>();
					Map<String, StateMetricResource> map = new HashMap<>();
					List<String> list = LogicManager.getInstance().getStateMetricRedisService().getStateAsyncMetricNames();
					List<String> list1 = new ArrayList<String>(list);
					if (list.size() > 0) {
						List<String> updateIds = stateMetricResDao.getResIdInId(list);
						list.removeAll(updateIds);
						for (String id : updateIds) {
							StateMetricResource stateMetricResource = LogicManager.getInstance().getStateMetricRedisService()
									.getResourceMetric(id);
							if (stateMetricResource != null)
								update.add(stateMetricResource);
						}
						for (String id : list) {
							StateMetricResource stateMetricResource = LogicManager.getInstance().getStateMetricRedisService()
									.getResourceMetric(id);
							if (stateMetricResource != null)
								map.put(id, stateMetricResource);
						}
						insert.addAll(map.values());
						if (update.size() > 0)
							stateMetricResDao.batchUpdate(update);
						if (insert.size() > 0)
							stateMetricResDao.batchInsert(insert);
						LogicManager.getInstance().getStateMetricRedisService().deleteMetricResIds(list1.toArray(new String[] {}));
					}

				} catch (Throwable e) {
					logger.warn("batch update state_metric_resource error:{} ", e);
				}
			}
		}, 120, period, java.util.concurrent.TimeUnit.SECONDS);
	}

	@Override
	public void insertStateMetric(StateMetricData stateMetricData) {
		List<StateMetric> stateMetrics = stateMetricData.getStateMetrics();
		Resource resource = stateMetricData.getResource();
		if (stateMetrics != null && !stateMetrics.isEmpty()) {
			// 如果ip有效，则添加ip tag
			String resId = generateResourceId(resource.getTenantId(), resource.getAgentId());
			Resource res = ServiceManager.getInstance().getResourceService().queryResById(resId, resource.getTenantId());
			// 暂时不做处理
			if (null == res) {
				return;
			}

			boolean isIPAvailable = res.getIpaddr() != null && res.getIpaddr().length() > 0
					&& !UNKNOWN.equalsIgnoreCase(res.getIpaddr());
			for (StateMetric stateMetric : stateMetrics) {
				stateMetric.addResourceId(res.getId());
				if (isIPAvailable) {
					addMetricTag(stateMetric, "ip", res.getIpaddr());
				}
				if (!res.getTags().isEmpty()) {
					for (ResourceTag tag : res.getTags()) {
						addMetricTag(stateMetric, tag.getKey(), tag.getValue());
					}
				}
			}
		}
		List<StateMetricInfo> stateMetricInfoList = new ArrayList<StateMetricInfo>();
		for (StateMetric metric : stateMetrics) {
			saveCheckpoint(metric);
			updateStateMetricRes(metric);
			stateMetricInfoList.add(generateStateMetricInfo(metric));
		}

		MQManager.getInstance().getMetricMQService().stateMetricSaved(stateMetricInfoList);
	}

	private StateMetricInfo generateStateMetricInfo(StateMetric metric) {
		List<Tag> tags = new ArrayList<>();
		for (Entry<String, List<String>> entry : metric.getTags().entrySet()) {
			StringBuilder builder = new StringBuilder();
			for (String value : entry.getValue()) {
				builder.append(value);
				builder.append(",");
			}
			tags.add(new Tag(entry.getKey(), builder.substring(0, builder.length() - 1)));
		}
		return new StateMetricInfo(metric.getName(), metric.getTenantId(), tags);
	}

	private void saveCheckpoint(StateMetric stateMetric) {
		ServiceManager.getInstance().getStateService().saveCheckpoint(buildCheckpoints(stateMetric));
	}

	private Checkpoint buildCheckpoints(StateMetric stateMetric) {
		List<String> tags = new ArrayList<>();
		for (String key : stateMetric.getTags().keySet()) {
			StringBuilder builder = new StringBuilder();
			List<String> values = stateMetric.getTags().get(key);
			for (String value : values) {
				builder.append(value);
				builder.append(",");
			}
			String val = builder.substring(0, builder.lastIndexOf(","));
			tags.add(key + ":" + val);
		}
		return new Checkpoint(stateMetric.getName(), stateMetric.getTimestamp(), stateMetric.getValue(),
				tags.toArray(new String[tags.size()]));
	}

	protected void addMetricTag(StateMetric metric, String tagK, String tagV) {
		// tagv不能为null
		if (tagV == null)
			tagV = "";
		List<String> ts = metric.getTags().get(tagK);
		if (ts == null) {
			metric.addTag(tagK, tagV);
		} else {
			if (!ts.contains(tagV)) {
				ts.add(tagV);
				// 防止因为排序问题，导致出现指标查不到
				Collections.sort(ts);
			}
		}
	}

	private String generateResourceId(String tenantId, String resourceId) {
		return UUIDUtils.generateResId(tenantId, resourceId);
	}

	// 更新状态指标--资源对应关系
	private void updateStateMetricRes(StateMetric metric) {
		List<String> metrics = LogicManager.getInstance().getStateMetricRedisService().getByResId(metric.getResourceId());
		if (metrics.contains(metric.getName())) {
			return;
		}
		metrics.add(metric.getName());
		LogicManager.getInstance().getStateMetricRedisService()
				.addMetricNames(metrics.toArray(new String[metrics.size()]), metric.getResourceId(), metric.getTenantId());
	}

	public List<String> getStateMetrics(String tenantId) {
		List<String> list = new ArrayList<>();
		List<StateMetricResource> metricResources = stateMetricResDao.getStateMetrics(tenantId);
		if (null == metricResources || metricResources.isEmpty()) {
			return list;
		}
		Set<String> stateMetrics = new HashSet<>();
		for (StateMetricResource metricResource : metricResources) {
			stateMetrics.addAll(metricResource.getMetricNames());
		}
		list.addAll(stateMetrics);
		return list;
	}

	public List<String> getStateMetricsByResId(String tenantId, String resId) {
		List<String> list = new ArrayList<>();
		List<StateMetricResource> metricResources = stateMetricResDao.getStateMetricsByResId(tenantId, resId);
		if (null == metricResources || metricResources.isEmpty()) {
			return list;
		}
		Set<String> stateMetrics = new HashSet<>();
		for (StateMetricResource metricResource : metricResources) {
			stateMetrics.addAll(metricResource.getMetricNames());
		}
		list.addAll(stateMetrics);
		return list;
	}

	@Override
	public void deleteByResId(String tenantId, String resourceId) {
		LogicManager.getInstance().getStateMetricRedisService().deleteByResId(resourceId);
		stateMetricResDao.delete(tenantId, resourceId);
	}

	@Override
	public void insert(StateMetricResource stateMetricResource) {
		List<StateMetricResource> resources = new ArrayList<>();
		resources.add(stateMetricResource);
		stateMetricResDao.batchInsert(resources);
	}
}
