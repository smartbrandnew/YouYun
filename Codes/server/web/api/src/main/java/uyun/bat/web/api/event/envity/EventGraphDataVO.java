package uyun.bat.web.api.event.envity;

import java.util.Date;
import java.util.List;

public class EventGraphDataVO {

    private Date beginTime;

    private Date endTime;

    private long diffTime;

    private List<EventGraphBuildVO> graphs;

    public EventGraphDataVO(Date beginTime, Date endTime, long diffTime, List<EventGraphBuildVO> graphs) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.diffTime = diffTime;
        this.graphs = graphs;
    }

    public EventGraphDataVO() {
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public long getDiffTime() {
        return diffTime;
    }

    public void setDiffTime(long diffTime) {
        this.diffTime = diffTime;
    }

    public List<EventGraphBuildVO> getGraphs() {
        return graphs;
    }

    public void setGraphs(List<EventGraphBuildVO> graphs) {
        this.graphs = graphs;
    }
}
