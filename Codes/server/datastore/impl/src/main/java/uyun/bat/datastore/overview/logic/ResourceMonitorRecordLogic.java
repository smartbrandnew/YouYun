package uyun.bat.datastore.overview.logic;

import java.util.*;

import uyun.bat.datastore.api.overview.entity.ResourceMonitorRecord;
import uyun.bat.datastore.api.overview.entity.TagResourceData;
import uyun.bat.datastore.api.util.UUIDUtils;
import uyun.bat.datastore.overview.dao.OTagResourceDao;
import uyun.bat.datastore.overview.dao.ResourceMonitorRecordDao;

public class ResourceMonitorRecordLogic {
	@javax.annotation.Resource
	private ResourceMonitorRecordDao resourceMonitorRecordDao;

	@javax.annotation.Resource
	private OTagResourceDao oTagResourceDao;

	public int save(List<ResourceMonitorRecord> resourceMonitorRecordList) {
		//防止sql太长，分批次插入
		int singleBatchCount= 100;
		
		if (resourceMonitorRecordList == null || resourceMonitorRecordList.isEmpty())
			return 0;
		for (ResourceMonitorRecord record : resourceMonitorRecordList) {
			String id = UUIDUtils.encodeMongodbId(record.getResourceId());
			record.setResourceId(id);
		}
		if (resourceMonitorRecordList.size() > singleBatchCount) {
			int count = save(resourceMonitorRecordList.subList(0, singleBatchCount));
			return count + save(resourceMonitorRecordList.subList(singleBatchCount, resourceMonitorRecordList.size()));
		} else {
			// 为了总览只显示7天的数据，故此处相同的资源监测器状态，不更新该状态时间
			List<ResourceMonitorRecord> newList = new ArrayList<ResourceMonitorRecord>();
			for (ResourceMonitorRecord temp : resourceMonitorRecordList) {
				if (!isExist(temp)) {
					newList.add(temp);
				}
			}
			return resourceMonitorRecordDao.save(newList);
		}
	}
	
	/**
	 * 监测目前要更新的数据，对应的资源监测器状态是否和之前的是一样的，一样则不进行处理。暂不考虑时间戳的更新
	 * 
	 * @param rmr
	 * @return
	 */
	private boolean isExist(ResourceMonitorRecord rmr) {
		List<ResourceMonitorRecord> rmrs = resourceMonitorRecordDao.queryResourceMonitorRecord(rmr.getTenantId(),
				rmr.getMonitorId(), rmr.getResourceId());
		if (rmrs.size() == 1) {
			ResourceMonitorRecord temp = rmrs.get(0);
			// 如果告警状态相同则不更新
			if (rmr.isOk() == temp.isOk() && rmr.isError() == temp.isError() && rmr.isWarn() == rmr.isWarn() && rmr.isInfo() == temp.isInfo())
				return true;
		}

		return false;

	}

	public int delete(String tenantId, String resourceId, String monitorId) {
		resourceId = UUIDUtils.encodeMongodbId(resourceId);
		return resourceMonitorRecordDao.delete(tenantId, resourceId, monitorId);
	}

	public int deleteDeletedMonitorData(String tenantId, List<String> monitorIdList) {
		return resourceMonitorRecordDao.deleteDeletedMonitorData(tenantId, monitorIdList);
	}
	
	private static final long INTERVAL = 7 * 24 * 60 * 60L * 1000L;

	/**
	 * 20170227决议.告警数量统计<br>
	 * 1.只统计监测器发生的告警数量<br>
	 * 2.超过7天的监测器告警不计算<br>
	 * 
	 * @return
	 */
	private long generateBegainTime() {
		return System.currentTimeMillis() - INTERVAL;
	}

	/**
	 * 查询总览统计树
	 * 
	 * @param tenantId
	 * @return
	 */
	public List<TagResourceData> getOverviewData(String tenantId) {
		List<TagResourceData> overviewDataList = resourceMonitorRecordDao.getOverviewData(tenantId, generateBegainTime());
		// 重新组装数据，dao查到的数据由多个union all查询组成
		// 1.总览资源数统计
		// 2.总览告警统计
		// 3.标签key级资源相关数据统计
		// 4.标签key,value资源相关数据统计
		// 暂时仅将1与2合并
		TagResourceData overviewData = overviewDataList.get(0);
		TagResourceData temp = overviewDataList.get(1);
		overviewData.setErrorCount(temp.getErrorCount());
		overviewData.setWarnCount(temp.getWarnCount());
		overviewData.setInfoCount(temp.getInfoCount());
		overviewDataList.remove(1);
		return overviewDataList;
	}

	/**
	 * 总览堆图<br>
	 * 选择全部或者选择标签key，查询相关的标签分组资源数据统计<br>
	 * key为null则显示全部
	 * 
	 * @param tenantId
	 * @param key
	 * @param value
	 * @return
	 */
	public List<TagResourceData> getTagResourceDataList(String tenantId, String key) {
		return resourceMonitorRecordDao.getTagResourceDataList(tenantId, key, generateBegainTime());
	}

	/**
	 * 选择总览标签，查询相关的标签资源数据统计<br>
	 * key与value为null则显示全部<br>
	 * value为''-->获取标签key:对应的数据
	 * 
	 * @param tenantId
	 * @param key
	 * @param value
	 * @return
	 */
	public TagResourceData getTagResourceData(String tenantId, String key, String value) {
		if (key == null && value != null)
			throw new IllegalArgumentException("there is no key in the tag but value comes...");
		List<TagResourceData> dataList = resourceMonitorRecordDao.getTagResourceData(tenantId, key, value, generateBegainTime());
		if (dataList.size() == 2) {
			// 由于不确定的key和value以及相应的分组条件，故查询所得列不包含key与value，此处回写
			TagResourceData data = dataList.get(0);
			TagResourceData temp = dataList.get(1);
			data.setKey(key);
			data.setValue(value);
			data.setWarnCount(temp.getWarnCount());
			data.setErrorCount(temp.getErrorCount());
			data.setInfoCount(temp.getInfoCount());
			return data;
		}
		return null;
	}

	/**
	 * 查询资源状态记录表的租户资源id列表
	 * 
	 * @param tenantId
	 * @return
	 */
	public List<String> queryTenantResourceIdList(String tenantId) {
		List<String> idList = resourceMonitorRecordDao.queryTenantResourceIdList(tenantId);
		for (int i = 0; i < idList.size(); i++) {
			String id = UUIDUtils.decodeMongodbId(idList.get(i));
			idList.set(i, id);
		}
		return idList;
	}

	/**
	 * 返回租户的资源告警信息
	 *
	 * @param tenantId
	 * @return
	 */
	public Map<String, ResourceMonitorRecord> queryResourceMonitorRecord(String tenantId) {
		List<ResourceMonitorRecord> rmrs = resourceMonitorRecordDao.queryResourceMonitorRecord(tenantId,
				null, null);
		Map<String, ResourceMonitorRecord> map = new HashMap<>();
		for (ResourceMonitorRecord r : rmrs) {
			ResourceMonitorRecord temp = map.get(r.getResourceId());
			if (null == temp) {
				map.put(r.getResourceId(), r);
			} else {
				//如果是错误什么都不干
				if (temp.isError())
					continue;
				//如果有比目前更严重的级别则更新
				boolean isNeedUpdate = (temp.isWarn() && r.isError()) || (temp.isInfo() && (r.isError() || r.isWarn())) || (temp.isOk() && (r.isError() || r.isWarn() || r.isInfo()));
				if (isNeedUpdate)
					map.put(r.getResourceId(), r);
			}
		}
		return map;
	}

	/**
	 * 返回租户监测器错误状态的资源Id列表
	 *
	 * @param tenantId
	 * @return
	 */
	public Set<String> queryResIdByErrorRecord(String tenantId) {
		List<ResourceMonitorRecord> rmrs = resourceMonitorRecordDao.queryResourceMonitorRecord(tenantId,
				null, null);
		Set<String> set = new HashSet<>();
		for (ResourceMonitorRecord r : rmrs) {
			//只要告警状态是错误的资源id
			if (r.isError())
				set.add(r.getResourceId());
		}
		return set;
	}
}
