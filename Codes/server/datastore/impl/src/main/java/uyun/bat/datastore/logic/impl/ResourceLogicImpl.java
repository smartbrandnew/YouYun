package uyun.bat.datastore.logic.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.dao.DuplicateKeyException;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.common.tag.util.TagUtil;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.PageResource;
import uyun.bat.datastore.api.entity.PageResourceGroup;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceApp;
import uyun.bat.datastore.api.entity.ResourceCount;
import uyun.bat.datastore.api.entity.ResourceDetail;
import uyun.bat.datastore.api.entity.ResourceGroup;
import uyun.bat.datastore.api.entity.ResourceModify;
import uyun.bat.datastore.api.entity.ResourceOpenApiQuery;
import uyun.bat.datastore.api.entity.ResourceOrderBy;
import uyun.bat.datastore.api.entity.ResourceStatusCount;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.entity.SimpleResource;
import uyun.bat.datastore.api.util.UUIDUtils;
import uyun.bat.datastore.dao.ResourceDao;
import uyun.bat.datastore.dao.ResourceDetailDao;
import uyun.bat.datastore.api.util.CollatorComparator;
import uyun.bat.datastore.entity.FilterQuery;
import uyun.bat.datastore.entity.GroupQuery;
import uyun.bat.datastore.entity.MetricSpanTime;
import uyun.bat.datastore.entity.ResTagResult;
import uyun.bat.datastore.entity.SimpleResourceQuery;
import uyun.bat.datastore.exception.DbException;
import uyun.bat.datastore.logic.LogicManager;
import uyun.bat.datastore.logic.ResourceLogic;
import uyun.bat.datastore.mq.MQManager;
import uyun.bat.datastore.util.StringUtils;

import com.google.common.collect.ArrayListMultimap;

public class ResourceLogicImpl implements ResourceLogic {
	@javax.annotation.Resource
	private ResourceDao resourceDao;
	@javax.annotation.Resource
	private ResourceDetailDao resourceDetailDao;

	public boolean save(Resource resource) {
		try {
//			String id = UUIDUtils.encodeMongodbId(resource.getId());
//			resource.setId(id);
			resourceDao.deleteResAppById(resource.getId());
			resourceDao.deleteResTagById(resource.getId());
			List<ResourceTag> list = new ArrayList<ResourceTag>();
			ArrayListMultimap<String, ResourceTag> map = ArrayListMultimap.create();
			for (ResourceTag tag : resource.getTags()) {
				map.put(tag.getKey(), tag);
			}
			for (String key : map.keySet()) {
				List<ResourceTag> tags = map.get(key);
				StringBuilder sb = new StringBuilder();
				for (ResourceTag tag : tags) {
					if (StringUtils.isNotNullAndTrimBlank(tag.getValue())
							&& sb.indexOf(tag.getValue()) == -1) {
						sb.append(tag.getValue());
						sb.append(",");
					}
				}
				String val = sb.toString().isEmpty() ? "" : sb.substring(0, sb.lastIndexOf(","));
				if (key.length() > 60)
					key = key.substring(0, 60);
				if (val.length() > 60)
					val = val.substring(0, 60);
				list.add(new ResourceTag(resource.getId(), key, val, resource.getTenantId()));
			}
			resourceDao.saveRes(resource);
			List<ResourceApp> apps = resource.getAppNames();
			for (ResourceApp app : apps) {
				app.setId(resource.getId());
			}
			if (apps.size() > 0)
				resourceDao.insertResAppBatch(apps);
			// 默认给没有tag的资源增加一个空的tag，资源列表分组需要，勿删
			if (list.size() <= 0)
				list.add(new ResourceTag(resource.getId(), "", "", resource.getTenantId()));
			resourceDao.insertResTagBatch(list);
			return true;
		} catch (DuplicateKeyException e) {
			return true;
		}

	}

	// todo
	// 删除资源、资源APP、资源Tag、资源详情(删除时增加TenantId的条件,迎合统一资源库接口)
	public boolean delete(String tenantId, String resourceId) {
		resourceId = UUIDUtils.encodeMongodbId(resourceId);
		resourceDao.deleteResTagById(resourceId);
		resourceDao.deleteResAppById(resourceId);
		resourceDao.deleteResById(resourceId);
		resourceDetailDao.delete(resourceId);
		return true;
	}

	public long insert(String tenantId, List<Resource> resources) {
		try {
			if (resources.size() <= 0)
				return 0l;
			for (Resource res : resources) {
				String id = UUIDUtils.encodeMongodbId(res.getId());
				res.setId(id);
			}
			analyzeTagAndApp(resources);
			return resourceDao.insertResBatch(resources);
		} catch (Exception e) {
			throw new DbException(String.format("batch insert resource exception:%s, message:%s", resources, e));
		}
	}

	@Override
	public long updateResBatch(String tenantId, List<Resource> resources) {
		try {
			if (resources.size() <= 0)
				return 0l;
			for (Resource res : resources) {
				String id = UUIDUtils.encodeMongodbId(res.getId());
				res.setId(id);
			}
			analyzeTagAndApp(resources);
			return resourceDao.updateResBatch(resources);
		} catch (Exception e) {
			throw new DbException(String.format("batch update resource exception:%s, message:%s", resources, e));
		}
	}

	// 解析生成app、tag到冗余表
	private void analyzeTagAndApp(List<Resource> resources) {
		List<String> resourceIds = new ArrayList<String>();
		List<ResourceApp> apps = new ArrayList<ResourceApp>();
		List<ResourceTag> tags = new ArrayList<ResourceTag>();

		for (Resource resource : resources) {
			List<ResourceApp> resApps = resource.getAppNames();
			for (ResourceApp app : resApps) {
				app.setId(resource.getId());
			}
			apps.addAll(resApps);
			List<ResourceTag> resTags = new ArrayList<ResourceTag>();
			ArrayListMultimap<String, ResourceTag> map = ArrayListMultimap.create();
			for (ResourceTag tag : resource.getTags()) {
				map.put(tag.getKey(), tag);
			}
			for (String key : map.keySet()) {
				List<ResourceTag> rtags = map.get(key);
				StringBuilder sb = new StringBuilder();
				for (ResourceTag tag : rtags) {
					if (StringUtils.isNotNullAndTrimBlank(tag.getValue())) {
						sb.append(tag.getValue());
						sb.append(",");
					}
				}
				String val = sb.toString().isEmpty() ? "" : sb.substring(0, sb.lastIndexOf(","));
				if (key.length() > 60)
					key = key.substring(0, 60);
				if (val.length() > 60)
					val = val.substring(0, 60);
				resTags.add(new ResourceTag(resource.getId(), key, val, resource.getTenantId()));
			}
			if (resTags.size() <= 0) {
				ResourceTag t = new ResourceTag(resource.getId(), "", "", resource.getTenantId());
				resource.getTags().add(t);
				tags.add(t);
			} else {
				tags.addAll(resTags);
			}
			resourceIds.add(resource.getId());

			// 拿mysql的res_tag表比较resource是否更新tag
			List<ResourceTag> resTagMysql = resourceDao.getTagsById(resource.getId());
			if(resTagMysql == null || resTagMysql.isEmpty()){
				if(resource.getTags() != null && !resource.getTags().isEmpty()){
					ResourceModify resModify = new ResourceModify(resource.getTenantId(), resource.getId(), resource.getTags(),
							ResourceModify.TYPE_UPDATE_RESOURCE_TAG);
					// 发送更新事件
					MQManager.getInstance().getMetricMQService().getResourceModifyjmsTemplate().convertAndSend(resModify);
				}
			}else{
				resTagMysql.removeAll(resource.getTags());
				if (null != resTagMysql && !resTagMysql.isEmpty()) {
					ResourceModify resModify = new ResourceModify(resource.getTenantId(), resource.getId(), resource.getTags(),
							ResourceModify.TYPE_UPDATE_RESOURCE_TAG);
					// 发送更新事件
					MQManager.getInstance().getMetricMQService().getResourceModifyjmsTemplate().convertAndSend(resModify);
				}
			}
		}
		if (resourceIds.size() > 0) {
			resourceDao.deleteResAppBatch(resourceIds);
			resourceDao.deleteResTagBatch(resourceIds);
		}
		if (apps.size() > 0)
			resourceDao.insertResAppBatch(apps);
		if (tags.size() > 0)
			resourceDao.insertResTagBatch(tags);

	}

	public List<Tag> queryResourceTags(String tenantId) {
		List<ResourceTag> list = resourceDao.getResTags(tenantId);
		List<Tag> tags = new ArrayList<Tag>();
		for (ResourceTag tag : list) {
			tags.add(new Tag(tag.getKey(), tag.getValue()));
		}
		return tags;
	}

	public Resource queryResById(String id, String tenantId) {
		String resId = UUIDUtils.decodeMongodbId(id);
		Resource res = resourceDao.getResById(resId);
		if (res != null) {
			res.setId(id);
			return res;
		}
		return null;
	}

	public Resource queryResByAgentId(String agentId, String tenantId) {
		Resource res = resourceDao.getResByAgentId(agentId, tenantId);
		if (res != null) {
			String id = UUIDUtils.decodeMongodbId(res.getId());
			res.setId(id);
		}
		return res;
	}

	public List<Resource> queryAllRes(String tenantId, boolean isContainNetWork) {
		List<Resource> resources = null;
		if (isContainNetWork) {
			resources = resourceDao.getAllRes(tenantId);
		} else {
			resources = resourceDao.getAllResNotContainNetwork(tenantId);
		}
		for (Resource res : resources) {
			String id = UUIDUtils.decodeMongodbId(res.getId());
			res.setId(id);
		}
		return resources;
	}

	public PageResource queryAllRes(String tenantId, int pageNo, int pageSize, OnlineStatus onlineStat) {
		int finalNo = (pageNo - 1) * pageSize;
		int total = resourceDao.getResCountByTenantId(tenantId);
		int count = 0;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tenantId", tenantId);
		map.put("pageNo", finalNo < 0 ? 0 : finalNo);
		map.put("pageSize", pageSize);
		String onlineStatus = null;
		if (onlineStat != null) {
			onlineStatus = onlineStat.getName();
		}
		map.put("onlineStatus", onlineStatus);
		List<Resource> resources = resourceDao.getAllResPage(map);
		int onlineCount = resourceDao.getOnlineCount(tenantId);
		PageResource pageResource = null;
		if (onlineStatus != null) {
			if (OnlineStatus.ONLINE.getId() == onlineStat.getId()) {
				count = onlineCount;
			} else {
				count = total - onlineCount;
			}
		} else {
			count = total;
		}
		for (Resource res : resources) {
			String id = UUIDUtils.decodeMongodbId(res.getId());
			res.setId(id);
		}
		if (count > 0) {
			pageResource = new PageResource(count, resources, onlineCount, total - onlineCount);
		} else
			pageResource = new PageResource(0, resources, 0, 0);
		return pageResource;
	}

	public List<String> queryResTagNames(String tenantId) {
		return resourceDao.getAllResTagNames(tenantId);
	}

	public PageResource queryByKey(String tenantId, String key, int pageNo, int size, OnlineStatus onlineStatus) {
		if (!StringUtils.isNotNullAndTrimBlank(key)) {
			return queryAllRes(tenantId, pageNo, size, onlineStatus);
		}
		int finalPageNo = (pageNo - 1) * size;
		boolean isOthers = false;
		List<Resource> list = new ArrayList<Resource>();
		if ("其他".equals(key)) {
			key = "";
			isOthers = true;
		}
		String[] conditions = key.split(";");
		List<String> filters = new ArrayList<String>(Arrays.asList(conditions));
		FilterQuery query = new FilterQuery(tenantId, filters, finalPageNo < 0 ? 0 : finalPageNo, size, onlineStatus);
		int total = 0;
		int onlineCount = 0;
		int count = 0;
		if (isOthers) {
			list = resourceDao.getResByEmptyTag(query);
			total = resourceDao.getCountByEmptyTag(query);
			onlineCount = resourceDao.getOnlineCountByEmptyTag(query);

		} else {
			list = resourceDao.getResByKey(query);
			total = resourceDao.getCountByKey(query);
			onlineCount = resourceDao.getOnlineCountByFilter(query);
		}

		if (onlineStatus != null) {
			if (OnlineStatus.ONLINE.getId() == onlineStatus.getId())
				count = onlineCount;
			else
				count = total - onlineCount;
		} else {
			count = total;
		}
		for (Resource res : list) {
			String id = UUIDUtils.decodeMongodbId(res.getId());
			res.setId(id);
		}
		PageResource pageResource = new PageResource(count, list, onlineCount, total - onlineCount);
		return pageResource;
	}

	private List<String> queryResIdsBykey(String tenantId, String key, OnlineStatus onlineStatus) {
		String[] conditions = key.split(";");
		List<String> filters = new ArrayList<String>(Arrays.asList(conditions));
		FilterQuery query = new FilterQuery(tenantId, filters, onlineStatus);
		List<String> list = resourceDao.getResIdByKey(query);
		return list;
	}

	public PageResourceGroup queryByFilterAndGroupByTag(String tenantId, String filter, String groupBy, int pageNo,
			int size, OnlineStatus onlineStatus) {
		List<ResourceGroup> resourceGroups = new ArrayList<ResourceGroup>();
		CollatorComparator comparator = new CollatorComparator();
		TreeMap<String, ResourceGroup> map = new TreeMap<String, ResourceGroup>(comparator);
		List<String> resourceIds = null;
		if (StringUtils.isNotNullAndTrimBlank(filter)) {
			resourceIds = queryResIdsBykey(tenantId, filter, onlineStatus);
		} else {
			resourceIds = resourceDao.getAllResId(tenantId);
		}
		if (resourceIds.size() > 0) {
			int finalPageNo = (pageNo - 1) * size;
			GroupQuery query = new GroupQuery(tenantId, groupBy, resourceIds, finalPageNo < 0 ? 0 : finalPageNo, size);
			List<String> ids = resourceDao.getResIdGroupByTag(query);
			if (ids.size() <= 0) {
				ids = resourceIds;
			}
			List<Resource> resources = resourceDao.getResInId(ids);
			for (Resource resource : resources) {
				String id = UUIDUtils.decodeMongodbId(resource.getId());
				resource.setId(id);
				boolean containGroupBy = false;
				Set<String> set = new HashSet<String>();
				for (ResourceTag tag : resource.getTags()) {
					if (tag.getKey().equals(groupBy)) {
						containGroupBy = true;
						set.add(tag.getValue());
					}
				}
				if (containGroupBy) {
					StringBuilder sb = new StringBuilder();
					for (String val : set) {
						sb.append(val);
						sb.append(",");
					}
					String value = sb.substring(0, sb.lastIndexOf(","));
					String name = groupBy + ":" + value;
					ResourceGroup group = map.get(name);
					if (group == null) {
						group = new ResourceGroup(name);
						map.put(name, group);
					}
					group.addResource(resource);
				} else {
					ResourceGroup group = map.get("座座座座座座座座座座座座座座");
					if (group == null) {
						group = new ResourceGroup("other");
						// 利用座字符确保排序时是最后的
						map.put("座座座座座座座座座座座座座座", group);
					}
					group.addResource(resource);
				}
			}
		}
		resourceGroups.addAll(map.values());
		String[] conditions = filter.split(";");
		List<String> filters = new ArrayList<String>(Arrays.asList(conditions));
		FilterQuery query = new FilterQuery(tenantId, filters, onlineStatus);
		int onlineCount = resourceDao.getOnlineCountByFilter(query);
		int total = resourceDao.getCountByKey(query);
		PageResourceGroup pageResourceGroup = new PageResourceGroup(resourceIds.size(), resourceGroups, onlineCount,
				total - onlineCount);
		return pageResourceGroup;

	}

	public PageResource queryByKeyAndSortBy(String tenantId, String filter, ResourceOrderBy orderBy, int pageNo,
			int size, OnlineStatus onlineStatus) {
		if (!StringUtils.isNotNullAndTrimBlank(filter)) {
			filter = "";
		}
		String[] conditions = filter.split(";");
		List<String> filters = new ArrayList<String>(Arrays.asList(conditions));
		int finalPageNo = (pageNo - 1) * size;
		FilterQuery query = new FilterQuery(tenantId, filters, finalPageNo < 0 ? 0 : finalPageNo, size,
				orderBy.getSortBy().toString(), orderBy.getOrder().toString(), onlineStatus);
		List<Resource> list = resourceDao.getResByKeyAndSort(query);
		for (Resource res : list) {
			String id = UUIDUtils.decodeMongodbId(res.getId());
			res.setId(id);
		}
		int total = resourceDao.getCountByKey(query);
		int count = 0;
		int onlineCount = resourceDao.getOnlineCountByFilter(query);
		if (onlineStatus != null) {
			if (OnlineStatus.ONLINE.getId() == onlineStatus.getId()) {
				count = onlineCount;
			} else {
				count = total - onlineCount;
			}
		} else {
			count = total;
		}
		PageResource pageResource = new PageResource(count, list, onlineCount, total - onlineCount);
		return pageResource;
	}

	@Override
	public List<SimpleResource> query(OnlineStatus onlineStatus, long lastCollectTime) {
		SimpleResourceQuery query = new SimpleResourceQuery(new ArrayList<String>(), new Date(lastCollectTime),
				onlineStatus.getName());
		List<Resource> resources = resourceDao.getSimpleResource(query);
		List<SimpleResource> simpleResourceList = new ArrayList<SimpleResource>();
		if (resources != null && !resources.isEmpty()) {
			for (Resource resource : resources) {
				String id = UUIDUtils.decodeMongodbId(resource.getId());
				SimpleResource simpleResource = new SimpleResource(id, resource.getHostname(), resource.getTenantId(),
						resource.getIpaddr(), resource.getLastCollectTime(), resource.getCreateTime(),
						resource.getResTagsAll(), resource.getOnlineStatusName());
				simpleResourceList.add(simpleResource);
			}
		}
		return simpleResourceList;
	}

	@Override
	public boolean saveResourceDetail(ResourceDetail resourceDetail) {
		ResourceDetail detail = queryByResId(resourceDetail.getResourceId());
		if (detail == null) {
			String id = UUIDUtils.encodeMongodbId(resourceDetail.getResourceId());
			resourceDetail.setResourceId(id);
			resourceDetailDao.insert(resourceDetail);
		} else
			resourceDetailDao.update(resourceDetail);
		return true;
	}

	@Override
	public ResourceDetail queryByResId(String resId) {
		String id = UUIDUtils.encodeMongodbId(resId);
		ResourceDetail resDetail = resourceDetailDao.queryByResId(id);
		if (resDetail != null) {
			resDetail.setResourceId(resId);
		}
		return resDetail;
	}

	@Override
	public boolean deleteResourceDetail(String resId) {
		String id = UUIDUtils.encodeMongodbId(resId);
		return resourceDetailDao.delete(id);
	}

	@Override
	public PageResource queryResListByCondition(ResourceOpenApiQuery query) {
		int finalPageNo = (query.getPageNo() - 1) * query.getPageSize();
		query.setPageNo(finalPageNo);
		List<Resource> resources = resourceDao.queryResListByCondition(query);
		int count = resourceDao.queryResCountByCondition(query);
		for (Resource res : resources) {
			String id = UUIDUtils.decodeMongodbId(res.getId());
			res.setId(id);
		}
		return new PageResource(count, resources);
	}

	@Override
	public List<ResourceCount> getResCountByDate(Date startTime, Date endTime) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		return resourceDao.getResCountByDate(map);
	}

	@Override
	public List<ResourceCount> getResCount() {
		return resourceDao.getResCount();
	}

	public List<String> getResFiltersByKey(String tenantId, String key) {
		List<String> results = new ArrayList<String>();
		if (StringUtils.isNotNullAndTrimBlank(key)) {
			String[] keys = key.split(";");
			Map<String, Object> map = new HashMap<String, Object>();
			List<String> list = new ArrayList<String>(Arrays.asList(keys));
			map.put("tenantId", tenantId);
			map.put("filters", list);
			List<ResTagResult> tagResults = resourceDao.getTagByFilter(map);
			Set<String> set = new HashSet<String>();
			for (ResTagResult tagResult : tagResults) {
				if (tagResult != null)
					set.addAll(tagResult.getTags());
			}
			for (String k : keys) {
				set.remove(k);
			}
			results.addAll(set);
		} else {
			List<ResourceTag> list = resourceDao.getResTags(tenantId);
			for (ResourceTag tag : list) {
				results.add(tag.changeToString());
			}
		}
		return results;
	}

	@Override
	public List<String> getResTagsByTag(String tenantId, String tags) {
		List<String> results = new ArrayList<String>();
		Set<String> set = new HashSet<String>();
		String[] keys = tags.split(";");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tenantId", tenantId);
		map.put("tags", "".equals(tags) ? new ArrayList<>() : Arrays.asList(keys));
		List<ResTagResult> list = resourceDao.getResTagsByTag(map);
		for (ResTagResult tagResult : list) {
			if (null != tagResult)
				set.addAll(tagResult.getTags());
		}

		for (String key : keys) {
			set.remove(key);
		}
		results.addAll(set);
		return results;
	}

	public List<ResourceStatusCount> getResStatusCount(String tenantId) {
		return resourceDao.getResStatusCount(tenantId);

	}

	public List<String> getAllApps() {
		return resourceDao.getAllApps();
	}

	@Override
	public List<String> getResIdInId(List<String> list) {
		if (list.size() <= 0)
			return new ArrayList<>();
//		for (int i = 0; i < list.size(); i++) {
//			String id = UUIDUtils.encodeMongodbId(list.get(i));
//			list.set(i, id);
//		}
		List<String> idList = resourceDao.getResIdInId(list);
//		for (int i = 0; i < idList.size(); i++) {
//			String id = UUIDUtils.decodeMongodbId(idList.get(i));
//			idList.set(i, id);
//		}
		return idList;
	}

	@Override
	public long deleteAuthorizationRes(String tenantId, List<String> ids) {
		long count = 0l;
		if (ids.size() > 0) {
			for (int i = 0; i < ids.size(); i++) {
				String id = UUIDUtils.encodeMongodbId(ids.get(i));
				ids.set(i, id);
			}
			resourceDao.deleteResAppBatch(ids);
			resourceDao.deleteResTagBatch(ids);
			count = resourceDao.deleteResBatch(ids);
			resourceDetailDao.deleteResDetailBatch(ids);
			LogicManager.getInstance().getMetricRedisService().deleteMetricNamesBatch(ids);
			LogicManager.getInstance().getMetricLogic().deleteMetricNamesBatch(ids);
		}
		return count;
	}

	@Override
	public List<String> getAuthorizationResIds(String tenantId, int ttl) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tenantId", tenantId);
		map.put("ttl", ttl);
		List<String> idList=resourceDao.getAuthorizationResIds(map);
		for(int i=0;i<idList.size();i++){
			String id=UUIDUtils.decodeMongodbId(idList.get(i));
			idList.set(i, id);
		}
		return idList;
	}

	@Override
	public List<String> getAllTenantId() {
		return resourceDao.getAllTenantId();
	}

	@Override
	public long insertResCountBatch(List<ResourceCount> list) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getResCountByTenantId(String tenantId) {
		return resourceDao.getResCountByTenantId(tenantId);
	}

	@Override
	public List<MetricSpanTime> getMetricSpanTime() {
		return resourceDao.getMetricSpanTime();
	}

	@Override
	public List<SimpleResource> queryByTenantIdAndTags(String tenantId, List<Tag> tags) {
		List<String> list = new ArrayList<String>();
		List<String> hosts = new ArrayList<>();
		if (null != tags && !tags.isEmpty()) {
			for (Tag tag : tags) {
				if (null == tag.getKey()) {
					continue;
				}
				// 标签如果包含host需要单独查询hostname字段
				if ("host".equals(tag.getKey())) {
					if (null != tag.getValue())
						hosts.add(tag.getValue());
				} else {
					String str = tag.toString();
					if (str != null)
						list.add(str);
				}
			}
		}
		SimpleResourceQuery query = new SimpleResourceQuery(list, tenantId, hosts);
		List<SimpleResource> resources=resourceDao.getSimpleResByTenantIdAndTags(query);
		for(SimpleResource sr:resources){
			String id=UUIDUtils.decodeMongodbId(sr.getResourceId());
			sr.setResourceId(id);
		}
		return resources;
	}

	public List<String> queryAllResTags(String tenantId) {
		List<String> tags = new ArrayList<>();
		List<Resource> resources = resourceDao.getAllResHostAndTags(tenantId);
		if (null == resources || resources.isEmpty()) {
			return tags;
		}
		for (Resource resource : resources) {
			List<String> resTags = resource.getResTagsAll();
			// 包括host的标签
			resTags.add("host:" + resource.getHostname());
			tags.addAll(resTags);
		}
		return TagUtil.rmDuplicateTag(tags);
	}

	@Override
	public List<String> getAllResId(String tenantId) {
		List<String> idList=resourceDao.getAllResId(tenantId);
		for(int i=0;i<idList.size();i++){
			String id=UUIDUtils.decodeMongodbId(idList.get(i));
			idList.set(i, id);
		}
		return idList;
	}

	@Override
	public List<ResourceCount> getResCountByOnlineStatus(OnlineStatus status) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("onlineStatus", status.getName());
		return resourceDao.getResCountByOnlineStatus(map);
	}

	@Override
	public List<Map<String, String>> getResTagsLikeMetrics(String tenantId, List<String> metrics) {
		if (metrics == null || metrics.size() == 0) {
			return new ArrayList<>();
		}
		Map<String, Object> params = new HashMap<>();
		params.put("tenantId", tenantId);
		params.put("metrics", metrics);
		return resourceDao.getResTagsByMetricName(params);
	}

	@Override
	public List<Resource> getTenantResByTags(String tenantId, List<String> resTags, String sortField, String sortOrder) {
		Map<String, Object> params = new HashMap<>();
		params.put("resTags", resTags);
		params.put("tenantId", tenantId);
		params.put("sortField", sortField);
		params.put("sortOrder", sortOrder);
		return resourceDao.getResByCondition(params);
	}

	@Override
	public List<Resource> getResByIpaddrs(String tenantId, List<String> ipaddrs) {
		Map<String, Object> params = new HashMap<>();
		params.put("ipaddrs", ipaddrs);
		params.put("tenantId" , tenantId);
		return resourceDao.getResByCondition(params);
	}

	@Override
	public int updateUserTagsBatch(List<Resource> list) {
		return resourceDao.updateUserTagBatchByIpaddr(list);
	}
}
