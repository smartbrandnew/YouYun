package uyun.bat.datastore.api.entity;

import java.util.Arrays;

public class Checkperiod {
	private String state;
	private String[] tags;
	private long firstTime;
	private long lastTime;
	private String value;
	private String priorValue;
	private String descr;
	private int count;

	public Checkperiod() {
	}

	public Checkperiod(String state, String[] tags, long firstTime, long lastTime, String value, String priorValue, int count, String descr) {
		this.state = state;
		this.tags = tags;
		this.firstTime = firstTime;
		this.lastTime = lastTime;
		this.value = value;
		this.priorValue = priorValue;
		this.count = count;
		this.descr = descr;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getState() {
		return state;
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

	public void setState(String state) {
		this.state = state;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
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

	@Override
	public String toString() {
		return "Checkperiod{" +
				"state='" + state + '\'' +
				", tags=" + Arrays.toString(tags) +
				", firstTime=" + firstTime +
				", lastTime=" + lastTime +
				", value='" + value + '\'' +
				", priorValue='" + priorValue + '\'' +
				", count=" + count +
				'}';
	}
}
