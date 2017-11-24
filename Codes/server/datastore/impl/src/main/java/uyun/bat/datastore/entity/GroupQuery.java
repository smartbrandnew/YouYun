package uyun.bat.datastore.entity;

import java.util.List;

public class GroupQuery {
	private String tenantId;
	private String groupBy;
	private List<String> resIds;
	private int pageNo;
	private int pageSize;

	public GroupQuery(String tenantId, String groupBy, List<String> resIds, int pageNo, int pageSize) {
		this.tenantId = tenantId;
		this.groupBy = groupBy;
		this.resIds = resIds;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	public List<String> getResIds() {
		return resIds;
	}

	public void setResIds(List<String> resIds) {
		this.resIds = resIds;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

}
