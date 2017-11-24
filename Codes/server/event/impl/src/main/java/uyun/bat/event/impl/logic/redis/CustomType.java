package uyun.bat.event.impl.logic.redis;

/**
 * Created by lilm on 17-4-13.
 */
public enum CustomType {
    UPDATE("res-update"), DEL("res-del");

    CustomType (String type) {
        this.type = type;
    }

    String type;

    public String getType() {
        return type;
    }

}
