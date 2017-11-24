package uyun.bat.datastore.api.entity;

public enum AlertStatus {
	OK(0, "正常"), WARNING(1, "告警"), CRITICAL(2, "紧急");
	private int id;
	private String name;

	private AlertStatus(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static AlertStatus checkById(int id) {
		for (AlertStatus value : AlertStatus.values()) {
			if (value.getId() == id)
				return value;
		}
		return null;
	}

	public static AlertStatus checkByName(String name) {
		for (AlertStatus value : AlertStatus.values()) {
			if (value.getName().equals(name))
				return value;
		}
		return null;
	}
}
