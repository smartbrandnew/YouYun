package uyun.bat.monitor.core.util;

public class ArbiterConstants {

    /**
     * 主机类型
     */
    public static final int HOST_TYPE=0;

    /**
     * 应用类型
     */
    public static final int APP_TYPE=1;

    /**
     * 应用平台配置项名称字符串（需拼接）
     */
    public static final String CONFIG_NAME="uyun-monitor:";

    /**
     * 紧急事件
     */
    public static final short EVENT_TYPE_ERROR=50;

    /**
     * 告警事件
     */
    public static final short EVENT_TYPE_WARINING=20;

    /**
     * 恢复事件
     */
    public static final short EVENT_TYPE_OK = 0;
}
