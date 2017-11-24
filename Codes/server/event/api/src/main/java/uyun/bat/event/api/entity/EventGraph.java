package uyun.bat.event.api.entity;

/**
 * Created by Administrator on 2016/4/28.
 */
public class EventGraph {

    private String graphDate;
    private Integer count;
    private Short serverity;

    public Short getServerity() {
        return serverity;
    }

    public void setServerity(Short serverity) {
        this.serverity = serverity;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public void setGraphDate(String graphDate) {
        this.graphDate = graphDate;
    }

    public String getGraphDate() {
        return graphDate;
    }


}
