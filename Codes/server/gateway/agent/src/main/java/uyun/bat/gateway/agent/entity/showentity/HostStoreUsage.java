package uyun.bat.gateway.agent.entity.showentity;

/**
 * 给show返回节点存储7天的使用率
 */
public class HostStoreUsage {
    private String time;
    private Double value;
    private String unit;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public HostStoreUsage() {
    }

    public HostStoreUsage(String time, Double value, String unit) {
        this.time = time;
        this.value = value;
        this.unit = unit;
    }
}
