package uyun.bat.monitor.api.entity;

import java.io.Serializable;

/**
 * 监测器自愈字段
 */
public class AutoRecoveryParams implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String params;
    private String time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
