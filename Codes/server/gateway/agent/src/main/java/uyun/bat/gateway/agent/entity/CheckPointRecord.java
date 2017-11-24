package uyun.bat.gateway.agent.entity;

public class CheckPointRecord {
	private long first_time;
	private long last_time;
	private String value;
	private int count;

	public long getFirst_time() {
		return first_time;
	}

	public void setFirst_time(long first_time) {
		this.first_time = first_time;
	}

	public long getLast_time() {
		return last_time;
	}

	public void setLast_time(long last_time) {
		this.last_time = last_time;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public CheckPointRecord(long first_time, long last_time, String value, int count) {
		super();
		this.first_time = first_time;
		this.last_time = last_time;
		this.value = value;
		this.count = count;
	}

	public CheckPointRecord() {
		super();
	}

}
