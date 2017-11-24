package uyun.bat.web.api.event.envity;

public class EventAlertVO {
    private int success;
    private int info;
    private int warnning;
    private int critical;

    public EventAlertVO(int success, int info, int warnning, int critical) {
        this.success = success;
        this.info = info;
        this.warnning = warnning;
        this.critical = critical;
    }

    public EventAlertVO() {
    }

    public int getInfo() {
        return info;
    }

    public void setInfo(int info) {
        this.info = info;
    }


    public int getWarnning() {
        return warnning;
    }

    public void setWarnning(int warnning) {
        this.warnning = warnning;
    }

    public int getCritical() {
        return critical;
    }

    public void setCritical(int critical) {
        this.critical = critical;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }
}
