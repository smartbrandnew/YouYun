package uyun.bat.web.api.monitor.entity;


import java.util.List;

public class MineMonitorHost {
    private int currentPage;
    private int count;
    private List<MonitorHostVO> hosts;
    private long now;

    public MineMonitorHost() {
    }

    public MineMonitorHost(int currentPage, int count, List<MonitorHostVO> hosts,long now) {
        this.currentPage = currentPage;
        this.count = count;
        this.hosts = hosts;
        this.now=now;
    }

    public long getNow() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<MonitorHostVO> getHosts() {
        return hosts;
    }

    public void setHosts(List<MonitorHostVO> hosts) {
        this.hosts = hosts;
    }
}
