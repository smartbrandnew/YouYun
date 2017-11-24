package uyun.bat.datastore.api.entity;

import java.util.Arrays;

/**
 * 状态指标数据点
 */
public class Checkpoint {
	private String state;
	private String[] tags;
	private long timestamp;
	private String value;
	private String descr;

	public Checkpoint() {
	}

	public Checkpoint(String state, long timestamp, String value, String...tags) {
		this.state = state;
		this.tags = tags;
		this.timestamp = timestamp;
		this.value = value;
	}

	public Checkpoint(String state, long timestamp, String value, String descr, String...tags) {
		this.state = state;
		this.tags = tags;
		this.timestamp = timestamp;
		this.value = value;
		this.descr = descr;
	}

	/**
	 * 本次变更记录描述
	 * @return
	 */
	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * 状态指标名称
	 * @return
	 */
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	/**
	 * 标签集合
	 * @return
	 */
	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	/**
	 * 时间
	 * @return
	 */
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * 值
	 * @return
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "Checkpoint{" +
				"state='" + state + '\'' +
				", tags=" + Arrays.toString(tags) +
				", timestamp=" + timestamp +
				", value='" + value + '\'' +
				'}';
	}
}
