package uyun.bat.monitor.core.mq;

public abstract class MQManager {
    private static MQManager instance = new MQManager() {
    };

    public static MQManager getInstance() {
        return instance;
    }

    private StateMQService stateMQService;

    private MonitorMQService monitorMQService;

    public StateMQService getStateMQService() {
        return stateMQService;
    }

    public void setStateMQService(StateMQService stateMQService) {
        this.stateMQService = stateMQService;
    }

    public MonitorMQService getMonitorMQService() {
        return monitorMQService;
    }

    public void setMonitorMQService(MonitorMQService monitorMQService) {
        this.monitorMQService = monitorMQService;
    }
}
