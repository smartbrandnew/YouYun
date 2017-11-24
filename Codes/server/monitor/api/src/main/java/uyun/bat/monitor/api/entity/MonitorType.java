package uyun.bat.monitor.api.entity;

public enum MonitorType {
	METRIC("metric", (short) 1, "指标"), EVENT("event", (short) 2, "事件"), HOST("host", (short) 3, "主机"), APP("app",
			(short) 4, "应用");
	private String code;
	private short value;
	private String name;

	private MonitorType(String code, short value, String name) {
		this.code = code;
		this.value = value;
		this.name = name;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static MonitorType checkByCode(String code) {
		for (MonitorType type : MonitorType.values()) {
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		throw new IllegalArgumentException("The current monitor type does not exist");
	}

	public static MonitorType checkByValue(short value) {
		for (MonitorType type : MonitorType.values()) {
			if (type.getValue() == value) {
				return type;
			}
		}
		throw new IllegalArgumentException("The current monitor type does not exist");
	}

}
