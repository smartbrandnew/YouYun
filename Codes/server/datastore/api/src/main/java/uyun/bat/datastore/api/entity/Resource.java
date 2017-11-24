package uyun.bat.datastore.api.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.common.tag.util.TagUtil;
import uyun.bat.datastore.api.util.StringUtils;

public class Resource implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private Date modified;
	private String hostname;
	private String ipaddr;
	private List<String> apps = new ArrayList<String>();
	private List<ResourceApp> appNames = new ArrayList<ResourceApp>();
	/**
	 * 资源总标签<br>
	 * agent标签 + openapi标签 + 用户自定义标签
	 */
	private List<ResourceTag> tags = new ArrayList<ResourceTag>();
	private ResourceType type;
	private String describtion;
	private String agentId;
	private String tenantId;
	private String resourceTypeName;
	private OnlineStatus onlineStatus;
	private AlertStatus alertStatus;
	private String onlineStatusName;
	private String alertStatusName;

	private Date lastCollectTime;
	private Date createTime;
	private String os;

	/**
	 * store内置标签
	 */
	private List<String> storeBuiltinTags = new ArrayList<>();

	/**
	 * agent标签
	 */
	private List<String> resTags = new ArrayList<String>();
	/**
	 * 用户自定义标签
	 */
	private List<String> userTags = new ArrayList<String>();
	/**
	 * openapi标签
	 */
	private List<String> agentlessTags = new ArrayList<String>();

	public Resource() {
		super();
	}

	public Resource(String id, Date modified, String hostname, String ipaddr, ResourceType type, String describtion,
			String agentId, String tenantId, List<String> apps, OnlineStatus onlineStatus, AlertStatus alertStatus,
			Date lastCollectTime, Date createTime, String os, List<String> resTags, List<String> userTags,
			List<String> agentlessTags) {
		this.id = id;
		this.modified = modified;
		this.hostname = hostname;
		this.ipaddr = ipaddr;
		this.type = type;
		this.describtion = describtion;
		this.agentId = agentId;
		this.tenantId = tenantId;

		setApps(apps);

		this.resourceTypeName = type.getName();
		this.onlineStatus = onlineStatus;
		this.alertStatus = alertStatus;
		
		this.lastCollectTime = lastCollectTime;

		this.createTime = createTime;
		this.os = os;

		this.resTags = resTags;
		this.userTags = userTags;
		this.agentlessTags = agentlessTags;

	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public OnlineStatus getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(OnlineStatus onlineStatus) {
		this.onlineStatus = onlineStatus;
		if (onlineStatus != null) {
			this.onlineStatusName = onlineStatus.getName();
		}
	}

	public Date getLastCollectTime() {
		return lastCollectTime;
	}

	public void setLastCollectTime(Date lastCollectTime) {
		this.lastCollectTime = lastCollectTime;
	}

	public AlertStatus getAlertStatus() {
		return alertStatus;
	}

	public void setAlertStatus(AlertStatus alertStatus) {
		this.alertStatus = alertStatus;
		if (alertStatus != null) {
			this.alertStatusName = alertStatus.getName();
		}
	}

	public void setOnlineStatusName(String onlineStatusName) {
		this.onlineStatus = OnlineStatus.checkByName(onlineStatusName);
		this.onlineStatusName = onlineStatusName;
	}

	public void setAlertStatusName(String alertStatusName) {
		this.alertStatus = AlertStatus.checkByName(alertStatusName);
		this.alertStatusName = alertStatusName;
	}

	public String getOnlineStatusName() {
		if (onlineStatus != null) {
			return onlineStatus.getName();
		}
		return onlineStatusName;
	}

	public String getAlertStatusName() {
		if (alertStatus != null) {
			return alertStatus.getName();
		}
		return alertStatusName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getIpaddr() {
		return ipaddr;
	}

	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}

	public List<ResourceApp> getAppNames() {
		appNames.clear();
		for (String app : apps) {
			if (StringUtils.isNotNullAndTrimBlank(app))
				this.appNames.add(new ResourceApp(id, tenantId, app));
		}
		return appNames;
	}

	public List<String> getApps() {
		if (apps == null)
			return new ArrayList<String>();
		return apps;
	}

	public void setApps(List<String> apps) {
		Set<String> set = new HashSet<String>();
		set.addAll(apps);
		this.apps.clear();
		this.apps.addAll(set);
	}

	/**
	 * 获取总的资源标签数据并对 resTags, userTags, agentlessTags, storeBuiltinTag 去重<br>
	 * agent标签 + openapi标签 + 用户自定义标签 + 统一资源库内置标签<br>
	 * <b>相关标签会重复且可能不包含host与ip标签</b>
	 */
	public List<ResourceTag> getTags() {
		this.tags.clear();

		List<String> tagList = getResTagsAll();

		for (String str : tagList) {
			Tag tag = TagUtil.string2Tag(str);
			if (tag == null) {
				continue;
			}
			this.tags.add(new ResourceTag(id, tag.getKey(), tag.getValue(), tenantId));
		}
		return tags;
	}

	/**
	 * 用于替换旧版标签的 getResTags
	 * @return resTags + userTags + agentlessTags + storeBuiltinTag
     */
	public List<String> getResTagsAll() {
		resTags = TagUtil.rmDuplicateTag(resTags);
		userTags = TagUtil.rmDuplicateTag(userTags);
		agentlessTags = TagUtil.rmDuplicateTag(agentlessTags);
		storeBuiltinTags = TagUtil.rmDuplicateTag(storeBuiltinTags);

		Set<String> tagSet = new HashSet<String>();
		if (resTags != null) {
			tagSet.addAll(resTags);
		}
		if (agentlessTags != null) {
			tagSet.addAll(agentlessTags);
		}
		if (userTags != null) {
			tagSet.addAll(userTags);
		}
		if (storeBuiltinTags != null) {
			tagSet.addAll(storeBuiltinTags);
		}
		return new ArrayList<String>(tagSet);
	}

	public ResourceType getType() {
		if (StringUtils.isNotNullAndTrimBlank(resourceTypeName) && type == null) {
			this.type = ResourceType.checkByName(resourceTypeName);
		}
		return type;
	}

	public void setType(ResourceType type) {
		this.type = type;
		this.resourceTypeName = type.getName();
	}

	public String getDescribtion() {
		return describtion;
	}

	public void setDescribtion(String describtion) {
		this.describtion = describtion;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getResourceTypeName() {
		return resourceTypeName;
	}

	public void setResourceTypeName(String resourceTypeName) {
		this.resourceTypeName = resourceTypeName;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public List<String> getStoreBuiltinTags() {
		return storeBuiltinTags;
	}

	public void setStoreBuiltinTags(List<String> storeBuiltinTags) {
		this.storeBuiltinTags = storeBuiltinTags;
	}

	/**
	 * 按照之前的逻辑 getResTags 将会组装所有资源 tag
	 * 使用 getResTagsAll 替换
     */
	public List<String> getResTags() {
		return resTags;
	}

	public void setResTags(List<String> resTags) {
		this.resTags = resTags;
	}

	public List<String> getUserTags() {
		return userTags;
	}

	public void setUserTags(List<String> userTags) {
		this.userTags = userTags;
	}

	public List<String> getAgentlessTags() {
		return agentlessTags;
	}

	public void setAgentlessTags(List<String> agentlessTags) {
		this.agentlessTags = agentlessTags;
	}

	@Override
	public String toString() {
		return "Resource [id=" + id + ", modified=" + modified + ", hostname=" + hostname + ", ipaddr=" + ipaddr
				+ ", resTags=" + resTags + ", type=" + getType() + ", describtion=" + describtion + ", agentId=" + agentId
				+ ", tenantId=" + tenantId + ",  apps=" + apps + ", onlineStatus=" + getOnlineStatus() + ", createTime="
				+ createTime + ", os=" + os + ", userTags=" + userTags + ", agentlessTags=" + agentlessTags
				+ ", lastCollectTime=" + lastCollectTime + ", appNames=" + getAppNames() + ", alertStatus=" + getAlertStatus()
				+ ", storeBuiltinTag=" + storeBuiltinTags + "]";
	}
}
