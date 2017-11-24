package uyun.bat.web.impl.service.rest.overview;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import uyun.bat.common.config.Config;
import uyun.bat.datastore.api.overview.entity.TagResourceData;
import uyun.bat.event.api.entity.PageUnrecoveredEvent;
import uyun.bat.web.api.overview.entity.Statistic;
import uyun.bat.web.api.overview.entity.TagKey;
import uyun.bat.web.api.overview.entity.TagLeaf;
import uyun.bat.web.api.overview.entity.TagNode;
import uyun.bat.web.api.overview.service.OverviewWebService;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;
 

import com.alibaba.dubbo.config.annotation.Service;

@Service(protocol = "rest")
@Path("v2/overview")
public class OverviewRESTService implements OverviewWebService {
	private static boolean isZH = Config.getInstance().isChinese();
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("alerts/statistics")
	public List<TagNode> getAllStatistics(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		 

		List<TagResourceData> datas = ServiceManager.getInstance().getOverviewService().getOverviewData(tenantId);
		List<TagNode> nodes = new ArrayList<TagNode>();
		TagResourceData data = datas.remove(0);
		// 全部
		if(isZH)
			nodes.add(new TagNode("全部", null, data.getResourceCount(), data.getWarnCount(), data.getErrorCount(), data.getInfoCount()));
		else
			nodes.add(new TagNode("All", null, data.getResourceCount(), data.getWarnCount(), data.getErrorCount(), data.getInfoCount()));
		TagResourceData otherData = null;
		if (datas.size() > 0) {
			// 枝干节点数据，叶子节点数据以及"其他"数据
			Map<String, List<TagResourceData>> dataMap = new HashMap<String, List<TagResourceData>>();
			// 同枝干数据分组
			List<TagResourceData> sameKeyDataList = null;
			for (TagResourceData temp : datas) {
				sameKeyDataList = dataMap.get(temp.getKey());
				if (sameKeyDataList == null) {
					sameKeyDataList = new ArrayList<TagResourceData>();
					dataMap.put(temp.getKey(), sameKeyDataList);
				}
				sameKeyDataList.add(temp);
			}

			// "其他"枝干数据
			sameKeyDataList = dataMap.remove("");
			if (sameKeyDataList != null)
				otherData = sameKeyDataList.get(0);

			// 枝干节点排序
			Comparator<Object> collator = Collator.getInstance(Locale.CHINA);
			Object[] tagArray = dataMap.keySet().toArray();
			Arrays.sort(tagArray, collator);

			// 组装节点
			for (Object t : tagArray) {
				// "其他"节点
				TagLeaf otherTagLeaf = null;
				// 根节点
				TagNode tagNode = null;
				// 枝干叶子节点Map
				Map<String, TagResourceData> leavesMap = new HashMap<String, TagResourceData>();
				sameKeyDataList = dataMap.get(t);
				for (int i = 0; i < sameKeyDataList.size(); i++) {
					TagResourceData tempData = sameKeyDataList.get(i);
					if (tempData.getValue() == null) {
						// 枝干节点
						tagNode = new TagNode(tempData.getKey(), tempData.getKey(), tempData.getResourceCount(),
								tempData.getWarnCount(), tempData.getErrorCount(), tempData.getInfoCount());
						sameKeyDataList.remove(i--);
						continue;
					} else if (tempData.getValue().length() == 0) {
						// "其他"叶子节点
						if(isZH)
							otherTagLeaf = new TagLeaf("其他", tempData.getKey() + ":", tempData.getResourceCount(),
								tempData.getWarnCount(), tempData.getErrorCount());
						else
							otherTagLeaf = new TagLeaf("Other", tempData.getKey() + ":", tempData.getResourceCount(),
									tempData.getWarnCount(), tempData.getErrorCount());
						sameKeyDataList.remove(i--);
						continue;
					} else {
						leavesMap.put(tempData.getValue(), tempData);
					}
				}

				// 枝干叶子节点排序
				Object[] leavesArray = leavesMap.keySet().toArray();
				Arrays.sort(leavesArray, collator);
				for (Object leaf : leavesArray) {
					TagResourceData tempData = leavesMap.get(leaf);
					tagNode.addChild(new TagLeaf(tempData.getValue(), tempData.getKey() + ":" + tempData.getValue(), tempData
							.getResourceCount(), tempData.getWarnCount(), tempData.getErrorCount()));
				}
				// 枝干增加"其他"叶子节点
				if (otherTagLeaf != null)
					tagNode.addChild(otherTagLeaf);

				nodes.add(tagNode);
			}
		}

		if (otherData != null) {
			// 添加"其他"枝干节点
			if(isZH)
				nodes.add(new TagNode("其他", ":", otherData.getResourceCount(), otherData.getWarnCount(), otherData
					.getErrorCount(), otherData.getInfoCount()));
			else
				nodes.add(new TagNode("Other", ":", otherData.getResourceCount(), otherData.getWarnCount(), otherData
						.getErrorCount(), otherData.getInfoCount()));
		}

		return nodes;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("alerts/statistics/query")
	public Statistic getStatisticByTag(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("tag") String tag) {
		 
		// tag为null--〉全部
		// tag为":"-->没有标签的资源统计
		// tag不包含冒号-->有标签key的资源统计
		String key = null;
		String value = null;
		if (tag != null && tag.length() > 0) {
			int index = tag.indexOf(":");
			if (index == 0 && tag.length() == 1) {
				key = "";
				value = "";
			} else if (index != -1) {
				key = tag.substring(0, index);
				if (tag.length() > (index + 1))
					value = tag.substring(index + 1);
				else
					value = "";
			} else {
				// 查询枝干对应的数据
				key = tag;
			}
		}

		TagResourceData data = ServiceManager.getInstance().getOverviewService().getTagResourceData(tenantId, key, value);
		if (key == null) {
			if(isZH)
				tag = "全部";
			else
				tag = "All";
		} else if (key.length() == 0) {
			if(isZH)
				tag = "其他";
			else
				tag = "Other";
		} else {
			if (value == null) {
				tag = key;
			} else if (value.length() == 0)
				if(isZH)
				tag = key + ":其他";
				else
					tag = key + ":Other";
			else
				tag = key + ":" + value;
		}
		Statistic statistic = new Statistic(tag, data.getResourceCount(), data.getWarnCount(), data.getErrorCount(), data.getInfoCount());
		return statistic;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("alerts/statistics/stacks/tag_keys")
	public List<TagKey> getTagKeyList(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
		List<String> keys = ServiceManager.getInstance().getOverviewService().getOverviewTagKeyList(tenantId);

		boolean isContainOther = keys.remove("");
		List<TagKey> allKeys = new ArrayList<TagKey>();

		// 全部
		if(isZH)
			allKeys.add(new TagKey("全部", null));
		else
			allKeys.add(new TagKey("All", null));
		// keys排序
		Comparator<Object> collator = Collator.getInstance(Locale.CHINA);
		Collections.sort(keys, collator);
		for (String key : keys) {
			allKeys.add(new TagKey(key, key));
		}
		// 其他
		if (isContainOther)
			if(isZH)
				allKeys.add(new TagKey("其他", ":"));
			else
				allKeys.add(new TagKey("Other", ":"));
		return allKeys;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("alerts/statistics/stacks/query")
	public List<Statistic> getStatisticsByTagKey(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("key") String key) {
		 
		// key为null-->全部
		// key为""-->其他
		List<TagResourceData> datas = ServiceManager.getInstance().getOverviewService()
				.getTagResourceDataList(tenantId, key);
		List<Statistic> statisticList = new ArrayList<Statistic>();
		for (TagResourceData data : datas) {
			if (data.getKey().length() == 0) {
				if(isZH)
					statisticList.add(new Statistic("其他", data.getResourceCount(), data.getWarnCount(), data.getErrorCount(), data.getInfoCount()));
				else
					statisticList.add(new Statistic("Other", data.getResourceCount(), data.getWarnCount(), data.getErrorCount(), data.getInfoCount()));
			} else if (data.getValue().length() == 0) {
				if(isZH)
					statisticList.add(new Statistic(data.getKey() + ":其他", data.getResourceCount(), data.getWarnCount(), data
						.getErrorCount(), data.getInfoCount()));
				else
					statisticList.add(new Statistic(data.getKey() + ":Oher", data.getResourceCount(), data.getWarnCount(), data
							.getErrorCount(),data.getInfoCount()));
			} else {
				statisticList.add(new Statistic(data.getKey() + ":" + data.getValue(), data.getResourceCount(), data
						.getWarnCount(), data.getErrorCount(),data.getInfoCount()));
			}
		}
		return statisticList;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("events/query")
	public PageUnrecoveredEvent getUnrecoveredEvents(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
			@QueryParam("page_index") @DefaultValue("1") int currentPage,
			@QueryParam("page_size") @DefaultValue("20") int pageSize, @QueryParam("key") String key,
			@QueryParam("search_value") String searchValue, @QueryParam("sort") String sort) {
		 
		return ServiceManager.getInstance().getEventService()
				.getUnrecoveredEvents(tenantId, currentPage, pageSize, key, searchValue, sort);
	}

}
