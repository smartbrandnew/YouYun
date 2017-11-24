package uyun.bat.datastore.mq.macro;

import java.util.Collections;
import java.util.List;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.common.tag.util.TagUtil;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.exception.DataAccessException;
import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.api.mq.ResourceInfo;
import uyun.bat.datastore.api.util.UUIDUtils;
import uyun.bat.datastore.logic.LogicManager;
import uyun.bat.datastore.mq.MQManager;
import uyun.bat.datastore.service.ServiceManager;
import uyun.bat.event.api.entity.EventSourceType;

public abstract class AbstractMetricMacro {
	/**
	 * 未知ip
	 */
	public static final String UNKNOWN = "unknown";

	/**
	 * 指标宏code
	 */
	public abstract int getCode();

	public abstract void exec(ComplexMetricData complexMetricData);

	protected void addMetricTag(PerfMetric metric, String tagK, String tagV) {
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

	/**
	 * 根据租户id，以及租户自定义的资源id，生成服务端的唯一uuid
	 * 
	 * @param tenantId
	 * @param resourceId
	 * @return
	 */
	private String generateResourceId(String tenantId, String resourceId) {
		return UUIDUtils.generateResId(tenantId, resourceId);
	}

	/**
	 * @param tenantId 租户id
	 * @param agentId 每个租户自己的资源id。多个租户间的资源id可重复
	 * @return
	 */
	protected Resource queryResource(String tenantId, String agentId) {
		String resId = generateResourceId(tenantId, agentId);
		// TODO 查询资源
		return ServiceManager.getInstance().getResourceService().queryResById(resId,tenantId);
	}

	protected boolean updateResource(Resource resource, boolean isOffline2Online) {
		//如果资源下线转上线则同步处理否则异步
		if (isOffline2Online)
			return ServiceManager.getInstance().getResourceService().saveResourceSyncOnly(resource);
		else
			return ServiceManager.getInstance().getResourceService().updateAsync(resource);
	}

	protected boolean instertResource(Resource resource) throws DataAccessException {
		// TODO 资源id的生成由datastore内部维护
		String resId = generateResourceId(resource.getTenantId(), resource.getAgentId());
		resource.setId(resId);
		return ServiceManager.getInstance().getResourceService().insertAsync(resource);
	}

	// 若资源从下线转为上线，则发送资源到MQ
	protected void resourceOnline(Resource resource, EventSourceType eventSourceType) {
		ResourceInfo info = new ResourceInfo(resource.getId(), resource.getTenantId(), resource.getHostname(),
				resource.getLastCollectTime(), eventSourceType.getKey(), OnlineStatus.ONLINE, resource.getIpaddr(),
				resource.getResTagsAll());
		MQManager.getInstance().getMetricMQService().resourceSaved(info);

	}

	protected void insertPerf(List<PerfMetric> metrics) {
		LogicManager.getInstance().getMetricLogic().insertPerf(metrics);
	}

	/**
	 * 增加用户自定义的tag
	 */
	protected PerfMetric generateUserTags(List<String> tags, PerfMetric metric) {
		if (tags != null && !tags.isEmpty()) {
			for (String t : tags) {
				Tag tag = TagUtil.string2Tag(t);
				// userTag存在空字符串的历史数据
				if (tag == null) {
					continue;
				}
				addMetricTag(metric, tag.getKey(), tag.getValue());
			}
		}
		return metric;
	}
}
