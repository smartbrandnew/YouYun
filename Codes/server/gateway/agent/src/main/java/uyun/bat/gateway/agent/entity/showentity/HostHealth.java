package uyun.bat.gateway.agent.entity.showentity;

import java.util.List;
import java.util.Map;

/**
 * 给show返回资源的健康度
 */
public class HostHealth {
    private int errCode = 200;
    private List<Map<String, Object>> data;

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public HostHealth() {
        super();
    }

}
