package com.broada.carrier.monitor.server.api.entity;

/**
 * 监测器类型实体类
 * 
 * @author Jiangjw
 */
public class MonitorType implements Comparable<MonitorType> {
	private String groupId;
	private String id;
	private String name;
	private String description;
	private String configer;
	private String monitor;
	private int sortIndex;
	private String[] targetTypeIds;
	private String[] methodTypeIds;

	public MonitorType() {
	}

	
	public MonitorType(String groupId, String id, String name, String description, String configer, String monitor,
			int sortIndex, String[] targetTypeIds, String[] methodTypeIds) {
		this.groupId = groupId;
		this.id = id;
		this.name = name;
		this.description = description;
		this.configer = configer;
		this.monitor = monitor;
		this.sortIndex = sortIndex;
		this.targetTypeIds = targetTypeIds;
		this.methodTypeIds = methodTypeIds;
	}


	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
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

	/**
	 * 类型说明
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 配置界面类
	 * 
	 * @return
	 */
	public String getConfiger() {
		return configer;
	}

	public void setConfiger(String configer) {
		this.configer = configer;
	}

	/**
	 * 监测实现类
	 * 
	 * @return
	 */
	public String getMonitor() {
		return monitor;
	}

	public void setMonitor(String monitor) {
		this.monitor = monitor;
	}

	/**
	 * 排序
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
	 * 可以用于哪些监测项类型
	 * 
	 * @return
	 */
	public String[] getTargetTypeIds() {
		return targetTypeIds;
	}

	public void setTargetTypeIds(String[] targetTypeIds) {
		this.targetTypeIds = targetTypeIds;
	}

	public String[] getMethodTypeIds() {
		return methodTypeIds;
	}

	public void setMethodTypeIds(String[] methodTypeIds) {
		this.methodTypeIds = methodTypeIds;
	}

	@Override
	public String toString() {
		return String.format("%s[id: %s name: %s]", getClass().getSimpleName(), getId(), getName());
	}

	@Override
	public int compareTo(MonitorType o) {
		if (this.getSortIndex() < o.getSortIndex())
			return 1;
		else if (this.getSortIndex() == o.getSortIndex())
			return 0;
		else
			return -1;
	}

	public boolean retNeedMethod() {
		return getMethodTypeIds() != null && getMethodTypeIds().length > 0;
	}
}
