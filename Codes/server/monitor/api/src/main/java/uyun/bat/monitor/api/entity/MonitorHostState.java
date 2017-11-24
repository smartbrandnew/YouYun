package uyun.bat.monitor.api.entity;

public class MonitorHostState {
    private String state;
    private long duration;
    private String descr;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }
}
