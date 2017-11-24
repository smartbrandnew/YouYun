package uyun.bat.datastore.logic.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uyun.bat.common.tag.util.TagUtil;
import uyun.bat.datastore.api.entity.AlertStatus;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceType;
import uyun.bat.datastore.api.exception.Illegalargumentexception;
import uyun.bat.datastore.api.util.StringUtils;

/**
 * redis缓存数据模型工具类
 */
public abstract class RedisDataModelUtil {
	/**
	 * 转化为Redis缓存对象
	 */
	public static Map<String, String> generateResourceRedisHash(Resource resource) {
		Map<String, String> map = new HashMap<String, String>();
		if (resource.getModified() != null)
			map.put("modified", String.valueOf(resource.getModified().getTime()));
		if (resource.getHostname() != null)
			map.put("hostname", resource.getHostname());
		String resourceTypeName = resource.getResourceTypeName();
		if (!StringUtils.isNotNullAndTrimBlank(resourceTypeName))
			resourceTypeName = "计算机设备";
		map.put("type", resourceTypeName);
		if (resource.getDescribtion() != null)
			map.put("describtion", resource.getDescribtion());
		if (resource.getAgentId() != null)
			map.put("agentId", resource.getAgentId());
		if (!StringUtils.isNotNullAndTrimBlank(resource.getTenantId()))
			throw new Illegalargumentexception("tenantId can not be empty or null in resource");
		map.put("tenantId", resource.getTenantId());
		map.put("apps", apps2String(resource.getApps()));
		String onlineStatusName = resource.getOnlineStatus().getName();
		if (!StringUtils.isNotNullAndTrimBlank(onlineStatusName))
			onlineStatusName = "在线";
		map.put("onlineStatus", onlineStatusName);
		String alertStatusName = resource.getAlertStatus().getName();
		if (!StringUtils.isNotNullAndTrimBlank(alertStatusName))
			alertStatusName = "正常";
		map.put("alertStatus", alertStatusName);
		if (resource.getLastCollectTime() != null)
			map.put("lastCollectTime", String.valueOf(resource.getLastCollectTime().getTime()));
		if (resource.getCreateTime() != null)
			map.put("createTime", String.valueOf(resource.getCreateTime().getTime()));
		if (resource.getIpaddr() != null)
			map.put("ipaddr", resource.getIpaddr());
		if (resource.getOs() != null)
			map.put("os", resource.getOs());
		map.put("tags", TagUtil.list2String(resource.getResTags()));
		map.put("userTags", TagUtil.list2String(resource.getUserTags()));
		map.put("agentlessTags", TagUtil.list2String(resource.getAgentlessTags()));
		return map;
	}

	private static String apps2String(List<String> apps) {
		StringBuilder builder = new StringBuilder();
		if (apps != null && apps.size() > 0) {
			for (String app : apps) {
				if (StringUtils.isNotNullAndTrimBlank(app)) {
					builder.append(app);
					builder.append(TagUtil.TAGS_SEPARATOR);
				}
			}
			String str = builder.toString();
			str = str.substring(0, str.lastIndexOf(TagUtil.TAGS_SEPARATOR));
			return str;
		}
		return "";
	}

	public static Resource generateResource(String id, Map<String, String> attritutes) {
		if (attritutes.size() > 0) {
			Resource resource = new Resource();
			resource.setId(id);
			resource.setTenantId(attritutes.get("tenantId"));
			resource.setType(ResourceType.checkByName(attritutes.get("type")));
			resource.setAgentId(attritutes.get("agentId"));
			resource.setAlertStatus(AlertStatus.checkByName(attritutes.get("alertStatus")));
			resource.setCreateTime(new Date(Long.parseLong(attritutes.get("createTime"))));
			resource.setDescribtion(attritutes.get("describtion"));
			resource.setHostname(attritutes.get("hostname"));
			resource.setIpaddr(attritutes.get("ipaddr"));
			resource.setLastCollectTime(new Date(Long.parseLong(attritutes.get("lastCollectTime"))));
			resource.setModified(new Date(Long.parseLong(attritutes.get("modified"))));
			resource.setOnlineStatus(OnlineStatus.checkByName(attritutes.get("onlineStatus")));
			resource.setApps(toApps(attritutes.get("apps")));
			resource.setOs(attritutes.get("os"));
			resource.setResTags(TagUtil.string2List(attritutes.get("tags")));
			resource.setUserTags(TagUtil.string2List(attritutes.get("userTags")));
			resource.setAgentlessTags(TagUtil.string2List(attritutes.get("agentlessTags")));
			return resource;
		}
		return null;
	}

	private static List<String> toApps(String apps) {
		if (StringUtils.isNotNullAndTrimBlank(apps)) {
			String[] str = apps.split(TagUtil.TAGS_SEPARATOR);
			return new ArrayList<String>(Arrays.asList(str));
		}
		return new ArrayList<String>();
	}
}
