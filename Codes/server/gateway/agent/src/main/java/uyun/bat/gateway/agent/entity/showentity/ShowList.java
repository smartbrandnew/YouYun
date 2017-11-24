package uyun.bat.gateway.agent.entity.showentity;

import java.util.List;

/**
 * 给show返回资源应用的告警数和总数列表
 */
public class ShowList {
    private int errCode = 200;
    private List<Object> data;

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }

    public ShowList() {
        super();
    }

    public ShowList(List<Object> data) {
        this.data = data;
    }
}
