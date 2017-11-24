package uyun.bat.agent.api.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lilm on 17-5-11.
 * 监控的资源验证状态
 */
public enum HostCheckStatus {
    UNCHECKED(0, "UNCHECKED", "未验证"), CHECKED(1, "CHECKED", "已验证"),
    VERIFYING(2, "VERIFYING", "验证中"), FAILED(-1, "FAILED", "验证失败");

    int code;
    String name;
    String cn;

    HostCheckStatus(int code, String name, String cn) {
        this.code = code;
        this.name = name;
        this.cn = cn;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public static HostCheckStatus checkCode(Integer code) {
        if (code == null) {
            return UNCHECKED;
        }
        for (HostCheckStatus hostCheckStatus : HostCheckStatus.values()) {
            if (hostCheckStatus.getCode() == code) {
                return hostCheckStatus;
            }
        }
        return UNCHECKED;
    }

    public static HostCheckStatus[] checkName(String[] nameArr) {
        if (nameArr == null || nameArr.length <= 0) {
            return HostCheckStatus.values();
        }
        List<HostCheckStatus> list = new ArrayList<HostCheckStatus>();
        for (HostCheckStatus hostCheckStatus : HostCheckStatus.values()) {
            for (String name : nameArr) {
                if (hostCheckStatus.getName().equals(name)) {
                    list.add(hostCheckStatus);
                }
            }
        }
        HostCheckStatus[] arr = new HostCheckStatus[list.size()];
        return list.toArray(arr);
    }

    /**
     * 检查是否需要agent验证
     * @return
     */
    public static boolean checkVerifying(Integer code) {
        if (code == null) {
            // 没有设置过状态则需用户点击验证按钮修改状态
            return false;
        }
        return HostCheckStatus.VERIFYING.getCode() == code;
    }
}
