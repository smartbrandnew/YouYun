package uyun.bat.datastore.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.rpc.RpcException;

import uyun.bat.common.config.Config;
import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.PageResource;
import uyun.bat.datastore.api.entity.PageResourceGroup;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceCount;
import uyun.bat.datastore.api.entity.ResourceDetail;
import uyun.bat.datastore.api.entity.ResourceModify;
import uyun.bat.datastore.api.entity.ResourceOpenApiQuery;
import uyun.bat.datastore.api.entity.ResourceOrderBy;
import uyun.bat.datastore.api.entity.ResourceStatusCount;
import uyun.bat.datastore.api.entity.SimpleResource;
import uyun.bat.datastore.api.service.ResourceService;
import uyun.bat.datastore.entity.ResourceIdTransform;
import uyun.bat.datastore.exception.DbException;
import uyun.bat.datastore.logic.DistributedUtil;
import uyun.bat.datastore.logic.ResourceIdTransformLogic;
import uyun.bat.datastore.logic.ResourceLogic;
import uyun.bat.datastore.logic.pacific.PacificResourceLogic;
import uyun.bat.datastore.logic.redis.ResourceRedisService;
import uyun.bat.datastore.mq.MQManager;
import uyun.bat.datastore.overview.logic.EnsureAccuracy;

public class ResourceServiceImpl implements ResourceService {
	private static final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);

	@Autowired
	private ResourceLogic resourceLogic;
	@Autowired
	private ResourceRedisService resourceRedisService;
	@Autowired
	private PacificResourceLogic pacificResourceLogic;
	@Autowired
	private ResourceIdTransformLogic resourceIdTransformLogic;

	private final static int corePoolSize = 3;
	// 设置1分钟同步一次
	private final static long period = 60;
	// 资源授权数暂写死为每租户500个
	private static int authorNum = Config.getInstance().get("tenant.authority.resource.num", 500);
	private boolean startPacific = Config.getInstance().get("pacific.setStart", false);

	@SuppressWarnings("unused")
	private void init() {
		try {
			// 初始化分布式工具类
			Class.forName(DistributedUtil.class.getName());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

		initResCount();
		ScheduledExecutorService service = Executors.newScheduledThreadPool(corePoolSize);
		service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!DistributedUtil.isLeader())
					return;
				if (logger.isDebugEnabled())
				logger.debug("Batch update resource task start......");
				try {
					List<String> list = resourceRedisService.getAsyncResIds();
					List<String> delIdsList = new ArrayList<>();
					List<Resource> insertRes = new ArrayList<>();
					List<String> list1 = new ArrayList<>();
					Map<String, Resource> resMap = new HashMap<>();
					if (list.size() > 0) {
						for (String resId : list) {
							Resource res = resourceRedisService.queryResById(resId);
							if (res == null)
								continue;
							String id = null;
							if (startPacific) {
								try {
									id = pacificResourceLogic.save(res);
								} catch (Exception e) {
									logger.warn("batch update resource task/ sync reconcile resources exception", e);
								}
								// 保存映射关系
								if (StringUtils.isNotBlank(id)) {
									ResourceIdTransform transform = new ResourceIdTransform(resId, res.getTenantId());
									transform.setUnitId(id);
									resourceIdTransformLogic.insertResourceIdTransform(transform);
								}
								//同步store的内置和自定义标签到monitor
								try {
									Map<String, List<String>> map = pacificResourceLogic.queryStoreTags(res.getTenantId(), res.getId());
									if (null != map && map.size() > 0) {
										res.setUserTags(map.get("userTags"));
										res.setStoreBuiltinTags(map.get("builtinTags"));
										resourceRedisService.update(res);
										ResourceModify resModify = new ResourceModify(res.getTenantId(), res.getId(), res.getTags(),
												ResourceModify.TYPE_UPDATE_RESOURCE_TAG);
										// 发送更新事件
										MQManager.getInstance().getMetricMQService().getResourceModifyjmsTemplate().convertAndSend(resModify);
									}
								} catch (Exception e) {
									logger.warn("sync store tag to monitor failed:", e);
								}
							}

//							res.setId(id);
							resourceLogic.save(res);
							list1.add(resId);
							resMap.put(resId, res);
//							delIdsList.add(resId);
						}
						List<String> update = resourceLogic.getResIdInId(list1);
						list1.removeAll(update);
						for (String id : list1) {
							if (resMap.get(id) != null)
								insertRes.add(resMap.get(id));
						}
//						resourceRedisService.delAsyncResIds(delIdsList.toArray(new String[] {}));
						// 更新总览数据
						updateOverviewData(insertRes);
					}
				} catch (Throwable e) {
					logger.warn("Start batch update resource task/overview data exception:", e);
				}
			}
		}, 120, period, TimeUnit.SECONDS);
	}

	private void updateOverviewData(List<Resource> insertList) {
		try {
			Set<String> tenantIdSet = new HashSet<String>();
			for (Resource resource : insertList) {
				tenantIdSet.add(resource.getTenantId());
			}
			for (String tenantId : tenantIdSet) {
				EnsureAccuracy.onResourceChange(tenantId, false);
			}
		} catch (Exception e) {
			throw new DbException(String.format("batch update overview data exception:%s, message:%s", insertList, e));
		}
	}

	private void initResCount() {
		List<ResourceCount> resCounts = resourceLogic.getResCount();
		resourceRedisService.insertResCountBatch(resCounts);
	}

	public boolean insertAsync(Resource resource) {
		long count = getResCountByTenantId(resource.getTenantId());
		if (count < authorNum) {
			resourceRedisService.insert(resource, count);
			return true;
		}
		throw new RuntimeException(String.format("fail to add resource,tenant %s authentication number %d beyond the limit", resource.getTenantId(), authorNum));
	}

	public boolean updateAsync(Resource resource) {
		resourceRedisService.update(resource);
		return true;
	}

	/*
	 * 界面删除资源数据
	 */
	public boolean delete(String tenantId, String resourceId) {
		boolean sign = true;
		if (startPacific){
			ResourceIdTransform temp = resourceIdTransformLogic.getTransformIdByIds(resourceId, tenantId);
			if (temp != null && temp.getUnitId() != null && temp.getUnitId().length() > 0)
				sign = pacificResourceLogic.delete(tenantId, temp.getUnitId());
			else
				sign = true;
			if(sign)
				resourceIdTransformLogic.delete(resourceId, tenantId);
		}
		if (sign) {
			long count = getResCountByTenantId(tenantId);
			if (resourceLogic.delete(tenantId, resourceId)) {
				resourceRedisService.delete(resourceId);
				resourceRedisService.insertResCount(tenantId, Long.toString(count - 1));
				ResourceModify resModify = new ResourceModify(tenantId, resourceId, null,
						ResourceModify.TYPE_DELETE_RESOURCE);
				// 发送删除事件
				MQManager.getInstance().getMetricMQService().getResourceModifyjmsTemplate().convertAndSend(resModify);
				return true;
			}
		}
		return false;
	}

	public long insertAsync(List<Resource> resources, String tenantId) {
		int count = getResCountByTenantId(tenantId);
		if (count + resources.size() <= authorNum) {
			resourceRedisService.insert(resources, tenantId, count);
			return resources.size();
		} else {
			int t = authorNum - count;
			resources = resources.subList(0, t);
			resourceRedisService.insert(resources, tenantId, count);
			logger.warn("Tenant {} batch insert message : {} success,{} fail, reason：tenant authorization limit{} ", tenantId, t, resources.size() - t, authorNum);
			return resources.size();
		}
	}

	public List<Resource> queryAllRes(String tenantId, boolean isContainNetwork) {
		return resourceLogic.queryAllRes(tenantId, isContainNetwork);
	}

	public List<Tag> queryResTags(String tenantId) {
		return resourceLogic.queryResourceTags(tenantId);
	}

	public List<String> queryResTagNames(String tenantId) {
		return resourceLogic.queryResTagNames(tenantId);
	}

	public Resource queryResById(String id, String tenantId) {
		Resource res = resourceRedisService.queryResById(id);
		if (res == null) {
			res = resourceLogic.queryResById(id, tenantId);
		}
		return res;
	}

	public Resource queryResByAgentId(String agentId, String tenantId) {
		return resourceLogic.queryResByAgentId(agentId, tenantId);
	}

	public PageResource queryByKey(String tenantId, String key, int pageNo, int size, OnlineStatus onlineStatus) {
		return resourceLogic.queryByKey(tenantId, key, pageNo, size, onlineStatus);
	}

	public PageResourceGroup queryByFilterAndGroupByTag(String tenantId, String filter, String groupBy, int pageNo,
			int size, OnlineStatus onlineStatus) {
		return resourceLogic.queryByFilterAndGroupByTag(tenantId, filter, groupBy, pageNo, size, onlineStatus);
	}

	public PageResource queryByKeyAndSortBy(String tenantId, String filter, ResourceOrderBy orderBy, int pageNo,
			int size, OnlineStatus onlineStatus) {
		return resourceLogic.queryByKeyAndSortBy(tenantId, filter, orderBy, pageNo, size, onlineStatus);
	}

	@Override
	public PageResource queryAllRes(String tenantId, int pageNo, int pageSize, OnlineStatus onlineStatus) {
		return resourceLogic.queryAllRes(tenantId, pageNo, pageSize, onlineStatus);
	}

	@Override
	public List<SimpleResource> query(OnlineStatus onlineStatus, long lastCollectTime) {
		List<SimpleResource> simpleResources = resourceLogic.query(onlineStatus, lastCollectTime);
		// 获取redis最新数据，削减simpleResources中最近有上报数据的资源
		List<String> redisResources = resourceRedisService.queryOnlineResource(lastCollectTime);
		if (redisResources != null && !redisResources.isEmpty()) {
			for (String resourceId : redisResources) {
				if (resourceId == null || resourceId.isEmpty())
					continue;
				for (SimpleResource simpleResource : simpleResources) {
					if (simpleResource.getResourceId().equals(resourceId)) {
						simpleResources.remove(simpleResource);
						break;
					}
				}
			}
		}
		return simpleResources;
	}

	@Override
	public boolean saveResourceDetail(ResourceDetail resourceDetail) {
		return resourceLogic.saveResourceDetail(resourceDetail);
	}

	@Override
	public ResourceDetail queryByResId(String resId) {
		return resourceLogic.queryByResId(resId);
	}

	@Override
	public PageResource queryResListByCondition(ResourceOpenApiQuery query) {
		return resourceLogic.queryResListByCondition(query);
	}

	@Override
	public List<ResourceCount> getResCountByDate(Date startTime, Date endTime) {
		return resourceLogic.getResCountByDate(startTime, endTime);
	}

	@Override
	public List<ResourceCount> getResCount() {
		return resourceLogic.getResCount();
	}

	@Override
	public List<String> getResTagsByTag(String tenantId, String tags) {
		if (tags == null)
			return resourceLogic.queryAllResTags(tenantId);
		return resourceLogic.getResTagsByTag(tenantId, tags);
	}

	public boolean deleteResourceDetail(String resId) {
		return resourceLogic.deleteResourceDetail(resId);
	}

	public List<ResourceStatusCount> getResStatusCount(String tenantId) {
		return resourceLogic.getResStatusCount(tenantId);
	}

	private int getResCountByTenantId(String tenantId) {
		int count = resourceRedisService.getResCountByTenantId(tenantId);
		if (count == 0)
			count = resourceLogic.getResCountByTenantId(tenantId);
		return count;
	}

	@Override
	public List<SimpleResource> queryByTenantIdAndTags(String tenantId, List<Tag> tags) {
		return resourceLogic.queryByTenantIdAndTags(tenantId, tags);
	}

	@Override
	public List<String> queryAllResTags(String tenantId) {
		return resourceLogic.queryAllResTags(tenantId);
	}

	/*
	 * 更新资源数据
	 * 暂时只为更新用户自定义标签存在
	 */
	@Override
	public boolean saveResourceSync(Resource resource) {
		saveResourceSyncOnly(resource);
		boolean sign = true;
		if (startPacific) {
			String unitId = pacificResourceLogic.save(resource);
			sign = unitId != null;
			if (sign && unitId.length() > 0) {
				ResourceIdTransform transform = new ResourceIdTransform(resource.getId(), resource.getTenantId());
				transform.setUnitId(unitId);
				resourceIdTransformLogic.insertResourceIdTransform(transform);
			}
		}
		ResourceModify resModify = new ResourceModify(resource.getTenantId(), resource.getId(), resource.getTags(),
				ResourceModify.TYPE_UPDATE_RESOURCE_TAG);
		// 发送更新事件
		MQManager.getInstance().getMetricMQService().getResourceModifyjmsTemplate().convertAndSend(resModify);
		return sign;
	}

	/**
	 * 只更新monitor端资源信息
	 * @param resource
	 * @return
	 */
	@Override
	public boolean saveResourceSyncOnly(Resource resource) {
		if (resource == null) {
			return false;
		}
		resourceRedisService.update(resource);
		return resourceLogic.save(resource);
	}

	@Override
	public List<ResourceCount> getResCountByOnlineStatus(OnlineStatus status) {
		return resourceLogic.getResCountByOnlineStatus(status);
	}

	@Override
	public List<Map<String, String>> queryResTagsByMetrics(String tenantId, List<String> metrics) {
		return resourceLogic.getResTagsLikeMetrics(tenantId, metrics);
	}

	@Override
	public List<Resource> queryTenantResByTags(String tenantId, List<String> resTags, String sortField, String sortOrder) {
		return resourceLogic.getTenantResByTags(tenantId, resTags, sortField, sortOrder);
	}

	@Override
	public List<Resource> queryResourcesByIpaddrs(String tenantId, List<String> ipaddrs) {
		return resourceLogic.getResByIpaddrs(tenantId, ipaddrs);
	}

	@Override
	public void updateUserTagsBatch(List<Resource> list) {
		resourceLogic.updateUserTagsBatch(list);
	}

	@Override
	public String resIdTransform(String tenantId, String resId) {
		ResourceIdTransform transform = resourceIdTransformLogic.getTransformIdByIds(resId, tenantId);
		return transform != null ? transform.getUnitId() : null;
	}
}
