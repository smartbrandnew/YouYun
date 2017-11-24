package uyun.bat.gateway.agent.entity;

/**
 * 状态指标数据点
 */
public class CheckpointVO {

	private String host_id;
	private String state;
	private String[] tags;
	private long timestamp = System.currentTimeMillis();
	private String value;

	public CheckpointVO() {
	}

	public CheckpointVO(String id, String state, String[] tags, long timestamp, String value) {
		this.host_id = id;
		this.state = state;
		this.tags = tags;
		this.timestamp = timestamp;
		this.value = value;
	}

	/**
	 * 状态指标名称
	 * 
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
	 * 
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
	 * 
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
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 设备host_id
	 * 
	 * @return
	 */
	public String getHost_id() {
		return host_id;
	}

	public void setHost_id(String host_id) {
		this.host_id = host_id;
	}
}
