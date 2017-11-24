package uyun.bat.datastore.api.entity;

public enum MetricType {
	gauge(0, "测量值","gauge"), counter(1, "计数值","counter"),rate(2,"比率","rate");
	private int id;
	private String name;
	private String code;

	private MetricType(int id, String name, String code) {
		this.id = id;
		this.name = name;
		this.code = code;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static MetricType checkByName(String name) {
		for (MetricType type : MetricType.values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}

	public static MetricType checkById(int id) {
		for (MetricType type : MetricType.values()) {
			if (type.getId() == id) {
				return type;
			}
		}
		return null;
	}

	public String getCode() {
		return code;
	}

	public static MetricType checkByCode(String code) {
		for (MetricType type : MetricType.values()) {
			if (type.getCode().equals(code)) {
				return type;
			}
		}
		return null;
	}
}
