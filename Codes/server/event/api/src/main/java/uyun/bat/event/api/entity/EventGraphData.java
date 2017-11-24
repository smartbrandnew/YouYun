package uyun.bat.event.api.entity;

import java.util.Date;
import java.util.List;

public class EventGraphData {

    private Date beginTime;

    private Date endTime;

    private long diffTime;

    private List<EventGraphBuild> graphs;


    public EventGraphData() {
    }

    public EventGraphData(Date beginTime, Date endTime, List<EventGraphBuild> graphs, int totalSuccess, int totalInfo, int totalWarning, int totalCritical, long diffTime) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.graphs = graphs;
        this.diffTime=diffTime;
    }

    public long getDiffTime() {
        return diffTime;
    }

    public void setDiffTime(long diffTime) {
        this.diffTime = diffTime;
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

    public List<EventGraphBuild> getGraphs() {
        return graphs;
    }

    public void setGraphs(List<EventGraphBuild> graphs) {
        this.graphs = graphs;
    }
}


