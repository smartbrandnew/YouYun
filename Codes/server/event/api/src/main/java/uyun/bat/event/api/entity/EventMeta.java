package uyun.bat.event.api.entity;

import java.util.List;

public class EventMeta {
    private int currentPage;
    private int total;
    private List<Integer> meta;

    public EventMeta(int total, List<Integer> meta) {
        this.total = total;
        this.meta = meta;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public EventMeta() {
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public List<Integer> getMeta() {
        return meta;
    }

    public void setMeta(List<Integer> meta) {
        this.meta = meta;
    }
}
