package com.broada.carrier.monitor.server.api.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 监测任务实体类，对应一个监测任务
 * 
 * @author Jiangjw
 */
public class MonitorTask implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String name;
	private String typeId;
	private String nodeId;
	private String resourceId;
	private String policyCode;
	private String methodCode;
	private boolean enabled;
	private String parameter;
	private String description;
	private Date modified;
	private String host;
	private String tags;
	private String ip;

	public MonitorTask() {
		this.enabled = true;
	}

	public MonitorTask(MonitorTask copy) {
		this(copy.id, copy.name, copy.typeId, copy.nodeId, copy.resourceId, copy.policyCode, copy.methodCode,
				copy.enabled, copy.parameter, copy.description, copy.modified, copy.host, copy.tags,copy.ip);
	}

	public MonitorTask(String id) {
		this.id = id;
	}

	public MonitorTask(String id, String name, String typeId, String nodeId, String resourceId, String policyCode,
			String methodCode, boolean enabled, String parameter, String description, Date modified, String host,
			String tags,String ip) {
		this.id = id;
		this.name = name;
		this.typeId = typeId;
		this.nodeId = nodeId;
		this.resourceId = resourceId;
		this.policyCode = policyCode;
		this.methodCode = methodCode;
		this.enabled = enabled;
		setParameter(parameter);
		this.description = description;
		this.modified = modified;
		this.host = host;
		this.tags = tags;
		this.ip=ip;
	}

	public String getPolicyCode() {
		return policyCode;
	}

	public void setPolicyCode(String policyCode) {
		this.policyCode = policyCode;
	}

	public String getMethodCode() {
		return methodCode;
	}

	public void setMethodCode(String methodCode) {
		this.methodCode = methodCode;
	}

	/**
	 * 修改时间戳
	 * 
	 * @return
	 */
	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
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

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(Object obj) {
		MonitorTask other = (MonitorTask) obj;
		return this.getId() == other.getId();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@JsonIgnore
	public <T> T getParameterObject(Class<T> cls) {
		return SerializeUtil.decodeJson(getParameter(), cls);
	}

	public void setParameterObject(Object param) {
		this.parameter = SerializeUtil.encodeJson(param);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public String toString() {
		return "MonitorTask [id=" + id + ", name=" + name + ", typeId=" + typeId + ", nodeId=" + nodeId
				+ ", resourceId=" + resourceId + ", policyCode=" + policyCode + ", methodCode=" + methodCode
				+ ", enabled=" + enabled + ", parameter=" + parameter + ", description=" + description + ", modified="
				+ modified + ", host=" + host + ", tags=" + tags + ", ip=" + ip + "]";
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public List<String> getTagList() {
		List<String> list = new ArrayList<String>();
		String str = getTags();
		if (str != null && str.trim().length() > 0) {
			String arrs[] = str.split(";");
			list.addAll(Arrays.asList(arrs));
		}
		return list;
	}

}
