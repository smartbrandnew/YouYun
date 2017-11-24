package uyun.bat.web.api.monitor.entity;

public class UserInfo {
	private String id;
	private String name;
	private boolean selected = false;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public UserInfo(String id, String name, boolean isSelected) {
		this.id = id;
		this.name = name;
		this.selected = isSelected;
	}

	public UserInfo() {
	}
}
