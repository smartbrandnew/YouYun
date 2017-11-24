package uyun.bat.web.api.event.envity;

import java.util.List;

public class EventFaultPageInfoVO {
    private int eventIndex;
    private List<Integer> pageServerity;

    public EventFaultPageInfoVO() {
    }

    public EventFaultPageInfoVO(int eventIndex, List<Integer> pageServerity) {
        this.eventIndex = eventIndex;
        this.pageServerity = pageServerity;
    }

    public int getEventIndex() {
        return eventIndex;
    }

    public void setEventIndex(int eventIndex) {
        this.eventIndex = eventIndex;
    }

    public List<Integer> getPageServerity() {
        return pageServerity;
    }

    public void setPageServerity(List<Integer> pageServerity) {
        this.pageServerity = pageServerity;
    }
}
