package uyun.bat.dashboard.api.entity;

import java.util.Date;
import java.util.List;

public class Dashboard {
	private String id;
	private String name;
	private List<String> dashwindowIdList;
	private String type;
	private Boolean template;
	private Date modified;
	private String userId;
	private String tenantId;
	private String descr;
	private Date createTime;
	private Boolean isResource;

	public Boolean getIsResource() {
		return isResource;
	}

	public void setIsResource(Boolean isResource) {
		this.isResource = isResource;
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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<String> getDashwindowIdList() {
		return dashwindowIdList;
	}

	public void setDashwindowIdList(List<String> dashwindowIdList) {
		this.dashwindowIdList = dashwindowIdList;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public Boolean getTemplate() {
		return template;
	}

	public void setTemplate(Boolean template) {
		this.template = template;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
