package uyun.bat.datastore.entity;

public class CheckpointRecord {
	private String stateId;
	private String objectId;
	private long firstTime;
	private long lastTime;
	private String value;
	private String priorValue;
	private int count;
	private String descr;

	public CheckpointRecord() {
	}

	public CheckpointRecord(String stateId, String objectId, long time, String value, String priorValue, String descr) {
		this.stateId = stateId;
		this.objectId = objectId;
		this.firstTime = time;
		this.lastTime = time;
		this.value = value;
		this.priorValue = priorValue;
		this.count = 1;
		this.descr = descr;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getStateId() {
		return stateId;
	}

	public void setStateId(String stateId) {
		this.stateId = stateId;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public long getFirstTime() {
		return firstTime;
	}

	public void setFirstTime(long firstTime) {
		this.firstTime = firstTime;
	}

	public long getLastTime() {
		return lastTime;
	}

	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getPriorValue() {
		return priorValue;
	}

	public void setPriorValue(String priorValue) {
		this.priorValue = priorValue;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "CheckpointRecord{" +
				"stateId='" + stateId + '\'' +
				", objectId='" + objectId + '\'' +
				", firstTime=" + firstTime +
				", lastTime=" + lastTime +
				", value='" + value + '\'' +
				", priorValue='" + priorValue + '\'' +
				", count=" + count +
				", descr='" + descr + '\'' +
				'}';
	}
}
