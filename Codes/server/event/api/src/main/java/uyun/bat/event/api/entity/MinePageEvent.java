package uyun.bat.event.api.entity;

import java.util.List;

public class MinePageEvent {

    private List<Event> events;

    private int count;

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
