package uyun.bat.datastore.api.entity;

/**
 * 状态指标 状态
 */
public enum StateStatus {
    OK(0, "正常"), WARNING(1, "告警"), CRITICAL(2, "紧急"),UNKNOWN(3,"UNKNOWN");
    private int id;
    private String name;

    StateStatus(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
