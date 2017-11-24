package uyun.bat.event.api.entity;

import java.util.Date;

public class EventGraphBuild {
    private int total;
    private Date time;
    private EventAlert alerts;

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

    public EventAlert getAlerts() {
        return alerts;
    }

    public void setAlerts(EventAlert alerts) {
        this.alerts = alerts;
    }
}
