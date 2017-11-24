package uyun.bat.event.api.entity;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.Date;
import java.util.List;

public class PageEvent {
	// 每页条数
	private int pageSize = 10;
	// 当前第几页
	private int currentPage = 1;
	// 查询集合
	private List<Event> rows;
	// 总记录数
	private int total;
	//总页数
	private int totalPage;

	private Date beginTime;

	private Date endTime;

	private EventMeta metas;

	public Date getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Date beginTime) {
		this.beginTime = beginTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public EventMeta getMetas() {
		return metas;
	}

	public void setMetas(EventMeta metas) {
		this.metas = metas;
	}

	public int getTotalPage() {
		totalPage=total%pageSize==0?totalPage=total/pageSize:(totalPage=total/pageSize+1);
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public PageEvent() {
	}

	public PageEvent(PageEvent page) {
		this.pageSize = page.pageSize;
		this.rows = page.rows;
		this.currentPage = page.currentPage;
		this.total = page.total;
		this.beginTime=page.beginTime;
		this.endTime=page.endTime;
	}

	public List<Event> getRows() {
		return rows;
	}

	public void setRows(List<Event> rows) {
		this.rows = rows;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	@JsonIgnore
	public int getFirstResult() {
		return ((currentPage - 1) * pageSize);
	}
}
