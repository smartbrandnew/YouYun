package uyun.bat.gateway.agent.entity.showentity;

/**
 * 给show返回网络设备延时
 */
public class TopQuery {
    private String name;
    private Double value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public TopQuery() {
    }

    public TopQuery(String name, Double value) {
        this.name = name;
        this.value = value;
    }
}
