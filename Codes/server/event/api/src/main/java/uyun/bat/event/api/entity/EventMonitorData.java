package uyun.bat.event.api.entity;

import java.sql.Timestamp;

/**
 * 事件监测器所需要的根据group by资源ID获取事件的数量
 */
public class EventMonitorData {

    private String resId;
    private int count;
    private Timestamp latestTime;

    public Timestamp getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(Timestamp latestTime) {
        this.latestTime = latestTime;
    }

    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
