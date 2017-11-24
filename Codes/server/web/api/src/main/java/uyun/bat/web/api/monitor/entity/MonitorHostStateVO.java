package uyun.bat.web.api.monitor.entity;

public class MonitorHostStateVO {
    private String state;
    private long duration;
    private String descr;

    public MonitorHostStateVO() {
    }

    public MonitorHostStateVO(String state, long duration, String descr) {
        this.state = state;
        this.duration = duration;
        this.descr = descr;
    }

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
