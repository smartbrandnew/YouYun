package uyun.bat.agent.impl.autosync.entity;

public enum Event {
	UPGRADE_PREPARE("upgradePrepare"), UPGRADE_SUCCESSFUL("upgradeSuccessful"), UPGRADE_FAILED("upgradeFailed");

	private String id;

	private Event(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public static Event checkById(String id) {
		Event event = getById(id);
		if (event == null)
			throw new IllegalArgumentException("Unknow event type：" + id);
		return event;
	}

	public static Event getById(String id) {
		for (Event et : values())
			if (et.getId().equalsIgnoreCase(id))
				return et;
		return null;
	}
}
