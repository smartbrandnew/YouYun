package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 监测项类型
 * 
 * @author Jiangjw
 */
public class MonitorTargetType implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String ROOT_NODE_ID = "BaseDevice";
	public static final String ROOT_RESOURCE_ID = "AppPlatform";
	public static final String ROOT_BIZAPPSYSTEM_ID = "AppPlatform/BizSystem";
	private String id;
	private String name;
	private String parentId;
	private int sortIndex;
	private String path;
	private String image64;
	private String image32;
	private String image16;
	private MonitorTargetTypeBusiness[] businesses;

	public MonitorTargetType() {
	}

	public MonitorTargetType(String id, String name, String parentId, int sortIndex, String path, String image64,
			String image32, String image16, MonitorTargetTypeBusiness[] businesses) {
		this.id = id;
		this.name = name;
		this.parentId = parentId;
		this.sortIndex = sortIndex;
		this.path = path;
		this.image64 = image64;
		this.image32 = image32;
		this.image16 = image16;
		this.businesses = businesses;
	}

	public String getImage64() {
		return image64;
	}

	public void setImage64(String image64) {
		this.image64 = image64;
	}

	public String getImage32() {
		return image32;
	}

	public void setImage32(String image32) {
		this.image32 = image32;
	}

	public String getImage16() {
		return image16;
	}

	public void setImage16(String image16) {
		this.image16 = image16;
	}

	public MonitorTargetTypeBusiness[] getBusinesses() {
		return businesses;
	}

	public void setBusinesses(MonitorTargetTypeBusiness[] businesses) {
		this.businesses = businesses;
	}

	/**
	 * 监测项类型路径
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * 排序号
	 * 
	 * @return
	 */
	public int getSortIndex() {
		return sortIndex;
	}

	public void setSortIndex(int sortIndex) {
		this.sortIndex = sortIndex;
	}

	/**
	 * 是否是节点类型
	 * 
	 * @return
	 */
	@JsonIgnore
	public boolean isNode() {
		return isNode(getPath());
	}

	public static boolean isNode(String path) {
		if (path == null)
			return false;
		return path.startsWith(ROOT_NODE_ID);
	}

	public static boolean isResource(String path) {
		if (path == null)
			return false;
		return path.startsWith(ROOT_RESOURCE_ID);
	}

	/**
	 * 是否是资源类型
	 * 
	 * @return
	 */
	@JsonIgnore
	public boolean isResource() {
		return isResource(getPath());
	}

	public boolean isSubType(MonitorTargetType subType) {
		boolean start = subType.getPath().startsWith(this.getPath());
		if (!start)
			return false;

		if (subType.getPath().length() > this.getPath().length())
			return subType.getPath().charAt(this.getPath().length()) == '/';
		else
			return true;
	}

	public static String getCodeByPath(String path) {
		String[] codes = path.split("/");
		return codes[codes.length - 1];
	}

	@JsonIgnore
	public boolean isPMDB() {
		for (MonitorTargetTypeBusiness business : businesses)
			if (business == MonitorTargetTypeBusiness.PMDB)
				return true;
		return false;
	}

	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), getId());
	}
}
