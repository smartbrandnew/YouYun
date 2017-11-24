package uyun.bat.datastore.entity;

import java.util.ArrayList;
import java.util.List;

import uyun.bat.datastore.api.entity.OnlineStatus;

public class FilterQuery {
	private String tenantId;
	private List<String> filters = new ArrayList<String>();
	private int pageNo;
	private int pageSize;
	private String sortBy;
	private String orderBy;
	private String groupBy;
	private OnlineStatus onlineStat;
	private String onlineStatus;

	public FilterQuery(String tenantId, List<String> filters, int pageNo, int pageSize, String groupBy) {
		this.tenantId = tenantId;
		this.filters = filters;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.groupBy = groupBy;
	}

	public FilterQuery(String tenantId, List<String> filters, int pageNo, int pageSize, OnlineStatus onlineStat) {
		super();
		this.tenantId = tenantId;
		this.filters = filters;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.onlineStat = onlineStat;
		if (onlineStat != null)
		this.onlineStatus = onlineStat.getName();
	}

	public FilterQuery(String tenantId, List<String> filters, int pageNo, int pageSize, String sortBy, String orderBy,
			String groupBy, OnlineStatus onlineStat) {
		this.tenantId = tenantId;
		this.filters = filters;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.sortBy = sortBy;
		this.orderBy = orderBy;
		this.groupBy = groupBy;
		this.onlineStat = onlineStat;
		if (onlineStat != null)
			this.onlineStatus = onlineStat.getName();
	}

	public FilterQuery(String tenantId, List<String> filters, int pageNo, int pageSize, String sortBy, String orderBy,
			OnlineStatus onlineStat) {
		this.tenantId = tenantId;
		this.filters = filters;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.sortBy = sortBy;
		this.orderBy = orderBy;
		this.onlineStat = onlineStat;
		if (onlineStat != null)
			this.onlineStatus = onlineStat.getName();
	}

	public FilterQuery(String tenantId, List<String> filters, int pageNo, int pageSize) {
		this.tenantId = tenantId;
		this.filters = filters;
		this.pageNo = pageNo;
		this.pageSize = pageSize;
	}

	public FilterQuery(String tenantId, List<String> filters, OnlineStatus onlineStat) {
		this.tenantId = tenantId;
		this.filters = filters;
		this.onlineStat = onlineStat;
	}


	public OnlineStatus getOnlineStat() {
		return onlineStat;
	}

	public void setOnlineStat(OnlineStatus onlineStat) {
		this.onlineStat = onlineStat;
	}

	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

	public String getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(String groupBy) {
		this.groupBy = groupBy;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getTenantId() {
		return tenantId;
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

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public List<String> getFilters() {
		return filters;
	}

	public void setFilters(List<String> filters) {
		this.filters = filters;
	}

}
