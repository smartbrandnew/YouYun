package uyun.bat.datastore.api.entity;

public enum OnlineStatus {
	ONLINE(0, "在线"), OFFLINE(1, "离线");
	private int id;
	private String name;

	private OnlineStatus(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static OnlineStatus checkById(int id) {
		for (OnlineStatus status : OnlineStatus.values()) {
			if (id == status.getId()) {
				return status;
			}
		}
		return null;
	}

	public static OnlineStatus checkByName(String name) {
		for (OnlineStatus status : OnlineStatus.values()) {
			if (status.getName().equals(name)) {
				return status;
			}
		}
		return null;
	}
}
