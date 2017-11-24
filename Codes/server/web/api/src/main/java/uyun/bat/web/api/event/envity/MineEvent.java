package uyun.bat.web.api.event.envity;

import java.util.Date;
import java.util.List;

public class MineEvent {

    private int pageSize;
    private int currentPage;
    private List<EventVO> rows;
    private int total;
    private int totalPage;
    private Date beginTime;
    private Date endTime;

    private long diffTime;
    private EventMetaVO metas;

    public MineEvent(int pageSize, int currentPage, List<EventVO> rows, int total, int totalPage, Date beginTime, Date endTime) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.rows = rows;
        this.total = total;
        this.totalPage = totalPage;
        this.beginTime = beginTime;
        this.endTime = endTime;
    }

    public MineEvent(int pageSize, int currentPage, List<EventVO> rows, int total, int totalPage,EventMetaVO metas) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.rows = rows;
        this.total = total;
        this.totalPage = totalPage;
        this.metas=metas;
    }

    public EventMetaVO getMetas() {
        return metas;
    }

    public void setMetas(EventMetaVO metas) {
        this.metas = metas;
    }

    public long getDiffTime() {
        return diffTime;
    }

    public void setDiffTime(long diffTime) {
        this.diffTime = diffTime;
    }

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

    public int getTotalPage() {
        totalPage=total%pageSize==0?totalPage=total/pageSize:(totalPage=total/pageSize+1);
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public List<EventVO> getRows() {
        return rows;
    }

    public void setRows(List<EventVO> rows) {
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

}
