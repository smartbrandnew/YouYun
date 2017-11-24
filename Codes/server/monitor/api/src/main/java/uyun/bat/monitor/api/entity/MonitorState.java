package uyun.bat.monitor.api.entity;

/**
 * 监测器状态
 */
public enum MonitorState {
	OK("ok", (short) 2, "正常"), INFO(Options.INFO, (short) 4, "提醒"),WARNING(Options.WARNING, (short) 5, "警告"), ERROR(Options.ALERT, (short) 8, "错误");
	private String code;
	private short value;
	private String cname;

	private MonitorState(String code, short value, String cname) {
		this.code = code;
		this.value = value;
		this.cname = cname;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public short getValue() {
		return value;
	}

	public void setValue(short value) {
		this.value = value;
	}

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	public static MonitorState checkByCode(String code) {
		for (MonitorState state : MonitorState.values()) {
			if (state.getCode().equals(code)) {
				return state;
			}
		}
		throw new IllegalArgumentException("The current monitor type does not exist");
	}

	public static MonitorState checkByValue(short value) {
		for (MonitorState state : MonitorState.values()) {
			if (state.getValue() == value) {
				return state;
			}
		}
		throw new IllegalArgumentException("The current monitor type does not exist");
	}
}
