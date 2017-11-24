package uyun.bat.web.api.monitor.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lilm on 17-7-6.
 */
public class PaginationUser {

    private int currentPage = 1;
    private int pageSize = 20;
    private int totalPage = 0;
    private long total = 0;
    private List<UserInfo> records = new ArrayList<UserInfo>();

    public PaginationUser() {
    }

    public PaginationUser(int currentPage, int pageSize, int totalPage, long total, List<UserInfo> records) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPage = totalPage;
        this.total = total;
        this.records = records;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<UserInfo> getRecords() {
        return records;
    }

    public void setRecords(List<UserInfo> records) {
        this.records = records;
    }
}
