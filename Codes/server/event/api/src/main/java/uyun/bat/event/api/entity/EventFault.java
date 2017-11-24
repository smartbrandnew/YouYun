package uyun.bat.event.api.entity;

import java.io.Serializable;

public class EventFault  implements Serializable {
    private String faultId;
    private long firstRelateTime;
    private long relateCount;
    private boolean recover;

	public EventFault(String faultId, long firstRelateTime, long relateCount, boolean recover) {
		this.faultId = faultId;
		this.firstRelateTime = firstRelateTime;
		this.relateCount = relateCount;
		this.recover = recover;
	}

    public EventFault() {
    }

    public String getFaultId() {
        return faultId;
    }

    public void setFaultId(String faultId) {
        this.faultId = faultId;
    }

    public long getFirstRelateTime() {
        return firstRelateTime;
    }

    public void setFirstRelateTime(long firstRelateTime) {
        this.firstRelateTime = firstRelateTime;
    }

    public long getRelateCount() {
        return relateCount;
    }

    public void setRelateCount(long relateCount) {
        this.relateCount = relateCount;
    }

    public boolean isRecover() {
        return recover;
    }

    public void setRecover(boolean recover) {
        this.recover = recover;
    }
}
