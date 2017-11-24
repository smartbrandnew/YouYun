package uyun.bat.event.impl.mq;

public abstract class MQManager {

    private static MQManager instance = new MQManager() {
    };

    public static MQManager getInstance() {
        return instance;
    }

    private EventMQService eventMQService;

    public EventMQService getEventMQService() {
        return eventMQService;
    }

    public void setEventMQService(EventMQService eventMQService) {
        this.eventMQService = eventMQService;
    }
}
