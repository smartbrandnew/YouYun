package uyun.bat.datastore.overview.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import uyun.bat.common.config.Config;
import uyun.bat.datastore.api.entity.Checkperiod;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.overview.entity.OTag;
import uyun.bat.datastore.api.overview.entity.ResourceMonitorRecord;
import uyun.bat.datastore.logic.DistributedUtil;
import uyun.bat.datastore.logic.LogicManager;
import uyun.bat.datastore.overview.entity.OResourceTag;
import uyun.bat.datastore.overview.entity.OTagResource;
import uyun.bat.datastore.overview.service.OverviewServiceManager;
import uyun.bat.datastore.service.ServiceManager;
import uyun.bat.monitor.api.common.util.StateUtil;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

/**
 * 保证准确性<br>
 * 1.标签树 <br>
 * 2.标签与资源的关系 <br>
 * 3.资源监测器触发状态
 */
public class EnsureAccuracy implements ApplicationListener<ContextRefreshedEvent> {
	private static final Logger logger = LoggerFactory.getLogger(EnsureAccuracy.class);

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		logger.info("overview ensure that the accuracy thread start......");
		service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (!DistributedUtil.isLeader())
					return;
				runEnsureAccuracy();
			}
		}, 120, 3600, java.util.concurrent.TimeUnit.SECONDS);
	}

	private void runEnsureAccuracy() {
		if (logger.isDebugEnabled())
			logger.debug("overview ensure that the accuracy thread runs......");
		// 根据资源表查询所有的租户列表，挨个处理
		long startTime = System.currentTimeMillis();
		List<String> tenantList = OverviewLogicManager.getInstance().getoTagResourceLogic().queryTenantList();
		for (String tenantId : tenantList) {
			try {
				if (Config.getInstance().get("overview.start", false))
					ensureTenantTagResourceAccuracy(tenantId);
			} catch (Throwable e) {
				logger.warn("Exception occur when maintain the relation between tags and resource at tenant:[" + tenantId + "] ." + e.getMessage());
				if (logger.isDebugEnabled()) {
					logger.debug("Stack：", e);
				}
			}
			try {
				ensureTenantResourceMonitorStateAccuracy(tenantId);
			} catch (Throwable e) {
				logger.warn("Maintain resource monitor trigger state at tenant:[" + tenantId + "]." + e.getMessage());
				if (logger.isDebugEnabled()) {
					logger.debug("Stack：", e);
				}
			}
		}
		if (logger.isDebugEnabled())
			logger.debug("finish" + tenantList.size() + "tenants' data veracity update，Time-consuming:" + (System.currentTimeMillis() - startTime));
	}

	/**
	 * 1.标签树 <br>
	 * 2.标签与资源的关系
	 */
	private static synchronized void ensureTenantTagResourceAccuracy(String tenantId) {
		// 前提，每个租户的资源数目不多，目前默认500个
		// 1.查询标签资源关系与资源表，若资源已删除，则删除该关系
		List<String> deletedResourceIdList = OverviewLogicManager.getInstance().getoTagResourceLogic()
				.queryTenantResourceIdList(tenantId);
		List<String> tenantResIdList = LogicManager.getInstance().getResourceLogic().getAllResId(tenantId);
		// 总览这边的资源id默认是大写，而datastore是小写
		for (String tenantResId : tenantResIdList) {
			for (int i = 0; i < deletedResourceIdList.size(); i++) {
				if (tenantResId.equalsIgnoreCase(deletedResourceIdList.get(i))) {
					deletedResourceIdList.remove(i);
					break;
				}
			}
		}
		tenantResIdList = null;
		for (String resourceId : deletedResourceIdList) {
			OverviewLogicManager.getInstance().getoTagResourceLogic().delete(new OTagResource(tenantId, null, resourceId));
		}
		// 2.查询该租户的资源列表，以及总览的标签列表
		List<Resource> resourceList = ServiceManager.getInstance().getResourceService().queryAllRes(tenantId, true);
		Map<String, List<OResourceTag>> tagMap = OverviewLogicManager.getInstance().getoTagLogic()
				.getTenantResourceTag(tenantId, null);
		// 所有标签
		Map<String, List<OTag>> allTags = new HashMap<String, List<OTag>>();
		for (Entry<String, List<OResourceTag>> entry : tagMap.entrySet()) {
			for (OResourceTag tag : entry.getValue()) {
				List<OTag> existTagList = allTags.get(tag.getKey());
				if (existTagList == null) {
					existTagList = new ArrayList<OTag>();
					allTags.put(tag.getKey(), existTagList);
				}
				OTag t = new OTag(tenantId, tag.getKey(), tag.getValue());
				t.setId(tag.getTagId());
				existTagList.add(t);
			}
		}
		//保存当前的标签列表以防后续重复插入
		List<OTag> tempTagList = new ArrayList<OTag>();
		for (Entry<String, List<OTag>> entry : allTags.entrySet()) {
			tempTagList.addAll(entry.getValue());
		}

		// 3.更新资源与标签的关系
		// 3.1 插入新标签，
		List<OTagResource> oTagResourceCreateList = new ArrayList<OTagResource>();
		List<OTagResource> oTagResourceDeleteList = new ArrayList<OTagResource>();
		List<String> newNoTagResourceList = new ArrayList<String>();
		for (Resource res : resourceList) {
			List<OResourceTag> tags = tagMap.get(res.getId());
			List<ResourceTag> resTags = res.getTags();
			if (resTags.isEmpty()) {
				// 处理无标签资源数据
				newNoTagResourceList.add(res.getId());
				if (tags != null) {
					if (tags.size() == 1 && tags.get(0).getKey().length() == 0 && tags.get(0).getKey().length() == 0) {
						continue;
					}
					for (OResourceTag ort : tags) {
						oTagResourceDeleteList.add(new OTagResource(tenantId, ort.getTagId(), ort.getResourceId()));
					}
				}
				continue;
			}
			
			// 需要增加哪些标签以及标签资源关系
			for (ResourceTag rt : resTags) {
				boolean isContain = false;
				if (tags != null) {
					for (OResourceTag ort : tags) {
						if (ort.getKey().equals(rt.getKey()) && ort.getValue().equals(rt.getValue() != null ? rt.getValue() : "")) {
							isContain = true;
							break;
						}
					}
				}
				if (!isContain) {
					// 防止重复创建
					OTag oTag = null;
					List<OTag> existTagList = allTags.get(rt.getKey());
					if (existTagList != null) {
						for (OTag ot : existTagList) {
							if (ot.getValue() != null && ot.getValue().length() > 0) {
								if (ot.getValue().equals(rt.getValue())) {
									oTag = ot;
									break;
								}
							} else {
								if (rt.getValue() == null || rt.getValue().length() == 0) {
									oTag = ot;
									break;
								}
							}
						}
					} else {
						existTagList = new ArrayList<OTag>();
						allTags.put(rt.getKey(), existTagList);
					}
					if (oTag == null) {
						oTag = new OTag(tenantId, rt.getKey(), rt.getValue() != null ? rt.getValue() : "");
						oTag.setId(UUIDTypeHandler.createUUID());
						existTagList.add(oTag);
					}
					oTagResourceCreateList.add(new OTagResource(tenantId, oTag.getId(), res.getId()));
				}
			}
			// 哪些标签资源关系需要删除
			if (tags != null) {
				for (OResourceTag ort : tags) {
					boolean shouldDelete = true;
					for (ResourceTag rt : resTags) {
						if (ort.getKey().equals(rt.getKey()) && ort.getValue().equals(rt.getValue() != null ? rt.getValue() : "")) {
							shouldDelete = false;
							break;
						}
					}
					if (shouldDelete) {
						oTagResourceDeleteList.add(new OTagResource(tenantId, ort.getTagId(), res.getId()));
					}
				}
			}
			res.setApps(Collections.EMPTY_LIST);
		}
		
		// 3.2 删除从无标签标签变为有标签的关系
		List<String> noTagResourceList = new ArrayList<String>();
		for (Map.Entry<String, List<OResourceTag>> resourceTagListEntry : tagMap.entrySet()) {
			List<OResourceTag> resourceTagList = resourceTagListEntry.getValue();
			if (resourceTagList.size() == 1 && resourceTagList.get(0).getKey().length() == 0
					&& resourceTagList.get(0).getValue().length() == 0) {
				noTagResourceList.add(resourceTagListEntry.getKey());
			}
		}
		noTagResourceList.removeAll(newNoTagResourceList);
		if (!noTagResourceList.isEmpty()) {
			for (String resourceId : noTagResourceList) {
				OverviewLogicManager.getInstance().getoTagResourceLogic().delete(new OTagResource(tenantId, null, resourceId));
			}
		}
		
		List<OTag> oTagCreateList = new ArrayList<OTag>();
		for (Entry<String, List<OTag>> entry : allTags.entrySet()) {
			oTagCreateList.addAll(entry.getValue());
		}
		//移除已创建的
		oTagCreateList.removeAll(tempTagList);
		OverviewLogicManager.getInstance().getoTagLogic().createOTag(oTagCreateList);
		oTagCreateList.clear();
		// 3.3建立资源与新标签的关系
		OverviewLogicManager.getInstance().getoTagResourceLogic().create(oTagResourceCreateList);
		oTagResourceCreateList.clear();
		// 3.4 删除资源与旧标签的关系
		for (OTagResource oTagResource : oTagResourceDeleteList) {
			OverviewLogicManager.getInstance().getoTagResourceLogic().delete(oTagResource);
		}
		oTagResourceDeleteList = null;
		// 3.5 创建无标签的资源关系
		if (newNoTagResourceList.size() > 0) {
			String noTagId = null;
			List<OTag> list = allTags.get("");

			if (list == null || list.isEmpty()) {
				noTagId = UUIDTypeHandler.createUUID();
				OTag oTag = new OTag(tenantId, null, null);
				oTag.setId(noTagId);
				oTagCreateList.add(oTag);
				OverviewLogicManager.getInstance().getoTagLogic().createOTag(oTagCreateList);
			} else {
				noTagId = list.get(0).getId();
			}

			for (String res : newNoTagResourceList) {
				oTagResourceCreateList.add(new OTagResource(tenantId, noTagId, res));
			}
			OverviewLogicManager.getInstance().getoTagResourceLogic().create(oTagResourceCreateList);
		}

		// 4.查询该租户的标签资源统计，若某标签对应的资源统计数是0，则清除该标签
		OverviewLogicManager.getInstance().getoTagResourceLogic().deleteNoResourceTagId(tenantId);
		// 5.清除关联不到标签的关系数据
		OverviewLogicManager.getInstance().getoTagResourceLogic().deleteNoTagResource(tenantId);
	}

	private static final String RESOURCE_PREFIX = StateUtil.RESOURCE_ID + ":";

	/**
	 * 3.资源监测器触发状态
	 */
	private static synchronized void ensureTenantResourceMonitorStateAccuracy(String tenantId) {
		// 1.获取“目前”租户已创建的所有监测器列表
		List<Monitor> monitorList = OverviewServiceManager.getInstance().getMonitorService().getMonitorList(tenantId);

		// 2.维护资源监测器状态数据的准确性
		// 2.1 根据监测器id删除未关联数据[在监测器数据较少时可以这么干]
		if (monitorList.isEmpty()) {
			// 监测器没啦~~
			OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().delete(tenantId, null, null);
			return;
		}

		List<String> monitorIdList = new ArrayList<String>();
		for (Monitor monitor : monitorList) {
			monitorIdList.add(monitor.getId());
		}
		OverviewLogicManager.getInstance().getResourceMonitorRecordLogic()
				.deleteDeletedMonitorData(tenantId, monitorIdList);
		// 2.2 根据监测器id获取“目前”触发的资源状态,插入数据
		for (Monitor monitor : monitorList) {
			String[] tags = new String[] { StateUtil.TENANT_ID + ":" + tenantId, StateUtil.MONITOR_ID + ":" + monitor.getId() };
			String[] objectIds = ServiceManager.getInstance().getTagService().queryObjectIds(tags);
			if (null == objectIds || objectIds.length < 1) {
				continue;
			}

			String state = StateUtil.generateState(monitor.getMonitorType());
			List<ResourceMonitorRecord> resourceMonitorRecordList = new ArrayList<ResourceMonitorRecord>();
			for (String objectId : objectIds) {
				Checkperiod checkperiod = ServiceManager.getInstance().getStateService()
						.getLastCheckperiod(tenantId, state, objectId);
				if (checkperiod == null || checkperiod.getTags() == null)
					continue;
				String resourceId = null;
				for (String tag : checkperiod.getTags()) {
					if (tag.startsWith(RESOURCE_PREFIX)) {
						resourceId = tag.substring(11);
						break;
					}
				}
				// 对应不到资源id
				if (resourceId == null)
					continue;
				MonitorState monitorState = MonitorState.checkByCode(checkperiod.getValue());
				if (monitorState == null) {
					logger.info("monitor[" + monitor.getId() + "]，resource[" + resourceId + "]state convert exception。State：" + checkperiod.getValue() + "");
					continue;
				}
				ResourceMonitorRecord resourceMonitorRecord = new ResourceMonitorRecord(tenantId, resourceId, monitor.getId(),
						checkperiod.getLastTime());
				if (monitorState == MonitorState.OK)
					resourceMonitorRecord.setOk(true);
				else if (monitorState == MonitorState.WARNING)
					resourceMonitorRecord.setWarn(true);
				else if (monitorState == MonitorState.ERROR)
					resourceMonitorRecord.setError(true);
				else if (monitorState == MonitorState.INFO)
					resourceMonitorRecord.setInfo(true);
				resourceMonitorRecordList.add(resourceMonitorRecord);
			}

			OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().save(resourceMonitorRecordList);

		}
		// 2.3 查询资源监测器状态与资源表，若资源已删除，则删除该状态记录
		List<String> deletedResourceIdList = OverviewLogicManager.getInstance().getResourceMonitorRecordLogic()
				.queryTenantResourceIdList(tenantId);
		List<String> tenantResIdList = LogicManager.getInstance().getResourceLogic().getAllResId(tenantId);
		// 总览这边的资源id默认是大写，而datastore是小写
		for (String tenantResId : tenantResIdList) {
			for (int i = 0; i < deletedResourceIdList.size(); i++) {
				if (tenantResId.equalsIgnoreCase(deletedResourceIdList.get(i))) {
					deletedResourceIdList.remove(i);
					break;
				}
			}
		}
		tenantResIdList = null;
		for (String resourceId : deletedResourceIdList) {
			OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().delete(tenantId, resourceId, null);
		}
	}

	/**
	 * 资源新增删除或者变更标签
	 */
	public static void onResourceChange(String tenantId, boolean isDelete) {
		long time = System.currentTimeMillis();
		if (logger.isDebugEnabled())
			logger.debug("Start clean up tenant's resource change overview data...tenantId:" + tenantId);
		try {
			if (Config.getInstance().get("overview.start", true))
				ensureTenantTagResourceAccuracy(tenantId);
		} catch (Throwable e) {
			logger.warn("Exception occur when maintain the relation between tags and resource at tenant:[" + tenantId + "]." + e.getMessage());
			if (logger.isDebugEnabled()) {
				logger.debug("Stack：", e);
			}
		}
		if (isDelete) {
			try {
				ensureTenantResourceMonitorStateAccuracy(tenantId);
			} catch (Throwable e) {
				logger.warn("Maintain resource monitor trigger state at tenant:[" + tenantId + "]." + e.getMessage());
				if (logger.isDebugEnabled()) {
					logger.debug("Stack：", e);
				}
			}
		}
		if (logger.isDebugEnabled())
			logger.debug("finish tenants' data veracity update，Time-consuming:" + (System.currentTimeMillis() - time));
	}

}
