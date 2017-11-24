package uyun.bat.web.api.event.envity;

import java.util.Date;

public class EventGraphBuildVO {
    private int total;
    private Date time;
    private EventAlertVO alerts;

    public EventGraphBuildVO(int total, Date time, EventAlertVO alerts) {
        this.total = total;
        this.time = time;
        this.alerts = alerts;
    }

    public EventGraphBuildVO() {
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public EventAlertVO getAlerts() {
        return alerts;
    }

    public void setAlerts(EventAlertVO alerts) {
        this.alerts = alerts;
    }
}
