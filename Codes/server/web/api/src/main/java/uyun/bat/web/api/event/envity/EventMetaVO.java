package uyun.bat.web.api.event.envity;

import java.util.List;

public class EventMetaVO {
    private int currentPage;
    private int total;
    private List<Integer> meta;

    public EventMetaVO(int total, List<Integer> meta) {
        this.total = total;
        this.meta = meta;
    }

    public EventMetaVO(int currentPage, int total, List<Integer> meta) {
        this.currentPage = currentPage;
        this.total = total;
        this.meta = meta;
    }

    public EventMetaVO() {
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }


    public List<Integer> getMeta() {
        return meta;
    }

    public void setMeta(List<Integer> meta) {
        this.meta = meta;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
}
